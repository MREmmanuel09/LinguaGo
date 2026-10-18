package dev.linguago.core.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.util.Log
import com.k2fsa.sherpa.onnx.OfflineTts
import com.k2fsa.sherpa.onnx.OfflineTtsConfig
import com.k2fsa.sherpa.onnx.OfflineTtsModelConfig
import com.k2fsa.sherpa.onnx.OfflineTtsVitsModelConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.linguago.core.domain.model.Language
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * TTS offline Piper via sherpa-onnx (ADR-003/ADR-010/ADR-020, Fase 2.2).
 * Carga los paquetes `vits-piper-<voz>` dejados por `fetchVoiceModels`
 * (assets), los instala en filesDir de forma atómica la primera vez,
 * verifica SHA-256 (ModelIntegrity) y sintetiza. El warmup ([warmUp]) corre
 * siempre en `Dispatchers.IO` con Mutex por idioma y estado observable
 * ([stateFor]); [isReadyFor] solo lee memoria. Sin voz instalada degrada y
 * [DefaultTtsEngine] cae al TTS del sistema.
 */
@Singleton
public class PiperTtsEngine
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) : TtsEngine {
        private val lanes = ConcurrentHashMap<Language, NativeSerialLane<OfflineTts>>()
        private val voices = ConcurrentHashMap<Language, PiperVoice>()
        private val lifecycle = Mutex()
        private val warmup = WarmupStateRegistry()

        override fun isReadyFor(language: Language): Boolean = warmup.isReady(language)

        override fun stateFor(language: Language): StateFlow<AudioEngineState> = warmup.stateOf(language)

        override suspend fun warmUp(language: Language) {
            warmup.warmUpOnce(language) {
                withContext(Dispatchers.IO) { warmUpBlocking(language) }
            }
        }

        override suspend fun speak(
            text: String,
            language: Language,
            speed: Float,
        ) {
            if (!isReadyFor(language)) warmUp(language)
            lane(language).use { engine ->
                withContext(Dispatchers.IO) {
                    currentCoroutineContext().ensureActive()
                    val audio = engine.generate(text = text, sid = 0, speed = speed)
                    currentCoroutineContext().ensureActive()
                    playBlocking(audio.samples, audio.sampleRate)
                }
            }
        }

        /** Verifica, extrae e instancia la voz en IO; publica el estado. */
        private suspend fun warmUpBlocking(language: Language) {
            val state = warmup.stateOf(language)
            state.value = AudioEngineState.Verifying
            val entry =
                readManifest().firstOrNull { entry ->
                    entry.engine == PIPER &&
                        entry.language == language.isoCode &&
                        entry.sha256.isNotBlank()
                }
            if (entry == null) {
                Log.w(TAG, "sin voz piper verificada para ${language.isoCode}")
                state.value = AudioEngineState.Degraded("sin voz piper para ${language.isoCode}")
                return
            }
            val installed = verifiedVoice(entry)
            if (installed != null) {
                finishWarmup(language, installed)
                return
            }
            state.value = AudioEngineState.Extracting
            val extracted = extractVoice(entry)
            if (extracted == null) {
                Log.w(TAG, "no se pudo instalar ${entry.path}")
                state.value = AudioEngineState.Degraded("instalación de ${entry.path} falló")
                return
            }
            finishWarmup(language, extracted)
        }

        /** Instancia el motor nativo y publica Ready/Degraded. */
        private suspend fun finishWarmup(
            language: Language,
            voice: PiperVoice,
        ) {
            val state = warmup.stateOf(language)
            state.value = AudioEngineState.Loading
            val engine = loadEngine(voice)
            if (engine == null) {
                state.value = AudioEngineState.Degraded("carga de ${voice.id} falló")
                return
            }
            lifecycle.withLock {
                if (!lane(language).install(engine)) {
                    state.value = AudioEngineState.Degraded("motor TTS liberado durante la carga")
                    return
                }
                voices[language] = voice
                state.value = AudioEngineState.Ready
            }
        }

        /**
         * Libera los motores JNI cacheados tras esperar a que terminen las
         * síntesis activas (ADR-022). El próximo [warmUp] vuelve a cargarlos.
         */
        public suspend fun release() {
            withContext(Dispatchers.IO) {
                lifecycle.withLock {
                    for (language in lanes.keys.toList()) {
                        lanes.remove(language)?.close()
                        voices.remove(language)
                        warmup.reset(language)
                    }
                }
            }
        }

        private fun lane(language: Language): NativeSerialLane<OfflineTts> =
            lanes.computeIfAbsent(language) { NativeSerialLane { engine -> engine.release() } }

        /** Voz instalada en filesDir con onnx verificado por SHA-256. */
        private fun verifiedVoice(entry: VoiceEntry): PiperVoice? {
            val dir = File(context.filesDir, entry.path)
            return if (voiceFilesValid(dir, entry)) PiperVoice(dir = dir, id = entry.id) else null
        }

        private fun voiceFilesValid(
            dir: File,
            entry: VoiceEntry,
        ): Boolean =
            hasVoiceFiles(dir, entry.id) &&
                ModelIntegrity
                    .sha256Hex(File(dir, "${entry.id}.onnx"))
                    .equals(entry.sha256, ignoreCase = true)

        /** Extrae `tts/<id>` a staging y lo publica con rename atómico. */
        private fun extractVoice(entry: VoiceEntry): PiperVoice? {
            val installed =
                AtomicInstall.install(
                    stagingDir = File(context.filesDir, "${entry.path}.staging"),
                    targetDir = File(context.filesDir, entry.path),
                    copy = { dir -> copyAssetTree(entry.path, dir) },
                    verify = { dir -> voiceFilesValid(dir, entry) },
                ) ?: return null
            return PiperVoice(dir = installed, id = entry.id)
        }

        /** Instanciación nativa (JNI, pesada): solo desde el warmup en IO. */
        private fun loadEngine(voice: PiperVoice): OfflineTts? =
            runCatching { OfflineTts(config = buildConfig(voice)) }
                .onFailure { error -> Log.w(TAG, "no se pudo cargar voz ${voice.id}", error) }
                .getOrNull()

        private fun buildConfig(voice: PiperVoice): OfflineTtsConfig =
            OfflineTtsConfig(
                model =
                    OfflineTtsModelConfig(
                        vits =
                            OfflineTtsVitsModelConfig(
                                model = voice.onnxFile.absolutePath,
                                tokens = voice.tokensFile.absolutePath,
                                dataDir = voice.dataDir.absolutePath,
                            ),
                        numThreads = NUM_THREADS,
                        debug = false,
                        provider = "cpu",
                    ),
            )

        /** Reproduce muestras float bloqueando hasta terminar (I1/I3). */
        private suspend fun playBlocking(
            samples: FloatArray,
            sampleRate: Int,
        ) {
            val chunkSamples = maxOf(MIN_CHUNK_SAMPLES, sampleRate / CHUNKS_PER_SECOND)
            val track =
                AudioTrack
                    .Builder()
                    .setAudioAttributes(
                        AudioAttributes
                            .Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                            .build(),
                    ).setAudioFormat(
                        AudioFormat
                            .Builder()
                            .setSampleRate(sampleRate)
                            .setEncoding(AudioFormat.ENCODING_PCM_FLOAT)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build(),
                    ).setBufferSizeInBytes(minBufferSize(sampleRate))
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()

            try {
                track.play()
                var offset = 0
                while (offset < samples.size) {
                    currentCoroutineContext().ensureActive()
                    val chunk = minOf(chunkSamples, samples.size - offset)
                    val written = track.write(samples, offset, chunk, AudioTrack.WRITE_BLOCKING)
                    if (written <= 0) return
                    offset += written
                }
                while (track.playbackHeadPosition < samples.size) {
                    currentCoroutineContext().ensureActive()
                    Thread.sleep(POLL_MILLIS)
                }
            } finally {
                runCatching { track.stop() }
                runCatching { track.release() }
            }
        }

        private fun minBufferSize(sampleRate: Int): Int =
            AudioTrack
                .getMinBufferSize(
                    sampleRate,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_FLOAT,
                ).coerceAtLeast(MIN_BUFFER_BYTES)

        /** Copia recursiva (dir `espeak-ng-data` incluida). */
        private fun copyAssetTree(
            assetPath: String,
            target: File,
        ) {
            target.mkdirs()
            for (name in context.assets.list(assetPath).orEmpty()) {
                val childAsset = "$assetPath/$name"
                val childFile = File(target, name)
                if (context.assets.list(childAsset).isNullOrEmpty()) {
                    childFile.parentFile?.mkdirs()
                    context.assets.open(childAsset).use { input ->
                        childFile.outputStream().use { output -> input.copyTo(output) }
                    }
                } else {
                    copyAssetTree(childAsset, childFile)
                }
            }
        }

        private fun hasVoiceFiles(
            dir: File,
            id: String,
        ): Boolean =
            File(dir, "$id.onnx").isFile &&
                File(dir, TOKENS_FILE).isFile &&
                File(dir, ESPEAK_DIR).isDirectory

        /** Lee `manifest.json` de assets; vacío si falta o es inválido. */
        private fun readManifest(): List<VoiceEntry> =
            runCatching {
                context.assets.open(VOICE_MANIFEST_ASSET).use { input ->
                    val root = JSONObject(input.bufferedReader().readText())
                    val array = root.optJSONArray("tts") ?: JSONArray()
                    (0 until array.length()).map { index ->
                        val item = array.getJSONObject(index)
                        VoiceEntry(
                            id = item.getString("id"),
                            engine = item.getString("engine"),
                            language = item.getString("language"),
                            path = item.getString("path"),
                            sha256 = item.optString("sha256"),
                        )
                    }
                }
            }.getOrDefault(emptyList())

        private companion object {
            const val PIPER = "piper"
            const val TOKENS_FILE = "tokens.txt"
            const val ESPEAK_DIR = "espeak-ng-data"
            const val VOICE_MANIFEST_ASSET = "manifest.json"
            const val NUM_THREADS = 2
            const val MIN_BUFFER_BYTES = 8192
            const val MIN_CHUNK_SAMPLES = 4096
            const val CHUNKS_PER_SECOND = 8
            const val POLL_MILLIS = 50L
            const val TAG = "PiperTtsEngine"
        }
    }

/** Paquete Piper instalado: dir con `<id>.onnx` + tokens + espeak-ng-data. */
private data class PiperVoice(
    val dir: File,
    val id: String,
) {
    val onnxFile: File = File(dir, "$id.onnx")
    val tokensFile: File = File(dir, "tokens.txt")
    val dataDir: File = File(dir, "espeak-ng-data")
}

/** Entrada del manifest de voces (models/manifest.json). */
private data class VoiceEntry(
    val id: String,
    val engine: String,
    val language: String,
    val path: String,
    val sha256: String,
)

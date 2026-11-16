package dev.linguago.core.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.util.Log
import com.k2fsa.sherpa.onnx.GeneratedAudio
import com.k2fsa.sherpa.onnx.GenerationConfig
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
 * TTS offline Piper via sherpa-onnx 2.0 (ADR-003/ADR-010/ADR-020/ADR-057).
 * Sin espeak-ng en runtime: las voces usan fonemas IPA precalculados en
 * `phonemes.bin` (generados en build por tools/tts/prepare_voices.py) y un
 * lexicón de respaldo. Los paquetes se instalan en filesDir de forma atómica,
 * se verifican por SHA-256 (onnx + datos fonéticos) y solo entonces se cargan.
 * [canSpeak] indica cobertura fonética del texto conocido; si no la hay,
 * [DefaultTtsEngine] degrada al TTS del sistema sin perder la función.
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

        /** `true` si la voz Piper puede pronunciar [text] sin fallback del sistema. */
        public fun canSpeak(
            language: Language,
            text: String,
        ): Boolean = isReadyFor(language) && voices[language]?.phonemes?.canSpeak(text) == true

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
            val voice = voices[language] ?: return
            lane(language).use { engine ->
                withContext(Dispatchers.IO) {
                    currentCoroutineContext().ensureActive()
                    val audio = synthesize(engine, voice, text, speed)
                    currentCoroutineContext().ensureActive()
                    if (audio != null) playBlocking(audio.samples, audio.sampleRate)
                }
            }
        }

        private fun synthesize(
            engine: OfflineTts,
            voice: PiperVoice,
            text: String,
            speed: Float,
        ): GeneratedAudio? {
            val sentences = voice.phonemes.sentencesFor(text)
            if (sentences != null) {
                val config =
                    GenerationConfig(
                        speed = speed,
                        phonemeCodepoints = sentences,
                    )
                return runCatching { engine.generateWithConfig("", config) }
                    .onFailure { error -> Log.w(TAG, "generateWithConfig falló para ${voice.id}", error) }
                    .getOrNull()
            }
            // Fallback local vía lexicón (útil si apareciera texto fuera del
            // precálculo en desarrollo); la ruta de producción usa canSpeak().
            return runCatching { engine.generate(text = VoicePhonemeData.normalizeKey(text), sid = 0, speed = speed) }
                .onFailure { error -> Log.w(TAG, "generate por lexicón falló para ${voice.id}", error) }
                .getOrNull()
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

        /** Voz instalada en filesDir con archivos verificados por SHA-256. */
        private fun verifiedVoice(entry: VoiceEntry): PiperVoice? {
            val dir = File(context.filesDir, entry.path)
            if (!voiceFilesValid(dir, entry)) return null
            val phonemes = VoicePhonemeData.load(File(dir, PHONEMES_FILE)) ?: return null
            return PiperVoice(dir = dir, id = entry.id, phonemes = phonemes)
        }

        private fun voiceFilesValid(
            dir: File,
            entry: VoiceEntry,
        ): Boolean {
            if (!hasVoiceFiles(dir, entry.id)) return false
            val onnxHash = ModelIntegrity.sha256Hex(File(dir, "${entry.id}.onnx"))
            if (!onnxHash.equals(entry.sha256, ignoreCase = true)) return false
            return entry.files.all { file ->
                val target = File(context.filesDir, file.path)
                target.isFile && ModelIntegrity.sha256Hex(target).equals(file.sha256, ignoreCase = true)
            }
        }

        /** Extrae `tts/<id>` de assets e instala atómicamente. */
        private fun extractVoice(entry: VoiceEntry): PiperVoice? {
            val installed =
                AtomicInstall.install(
                    stagingDir = File(context.filesDir, "${entry.path}.staging"),
                    targetDir = File(context.filesDir, entry.path),
                    copy = { dir -> copyAssetTree(entry.path, dir) },
                    verify = { dir -> voiceFilesValid(dir, entry) },
                ) ?: return null
            val phonemes = VoicePhonemeData.load(File(installed, PHONEMES_FILE)) ?: return null
            return PiperVoice(dir = installed, id = entry.id, phonemes = phonemes)
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
                                lexicon = voice.lexiconFile.absolutePath,
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
            if (samples.isEmpty()) return
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

        /** Copia recursiva de un árbol de assets a [target]. */
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
                File(dir, PHONEMES_FILE).isFile &&
                File(dir, LEXICON_FILE).isFile

        /** Lee `manifest.json` de assets; vacío si falta o es inválido. */
        private fun readManifest(): List<VoiceEntry> =
            runCatching {
                context.assets.open(VOICE_MANIFEST_ASSET).use { input ->
                    val root = JSONObject(input.bufferedReader().readText())
                    val array = root.optJSONArray("tts") ?: JSONArray()
                    (0 until array.length()).map { index ->
                        val item = array.getJSONObject(index)
                        val files = mutableListOf<VoiceFile>()
                        val filesArray = item.optJSONArray("files") ?: JSONArray()
                        for (f in 0 until filesArray.length()) {
                            val file = filesArray.getJSONObject(f)
                            files +=
                                VoiceFile(
                                    path = file.getString("path"),
                                    sha256 = file.optString("sha256"),
                                )
                        }
                        VoiceEntry(
                            id = item.getString("id"),
                            engine = item.getString("engine"),
                            language = item.getString("language"),
                            path = item.getString("path"),
                            sha256 = item.optString("sha256"),
                            files = files,
                        )
                    }
                }
            }.getOrDefault(emptyList())

        private companion object {
            const val PIPER = "piper"
            const val TOKENS_FILE = "tokens.txt"
            const val PHONEMES_FILE = "phonemes.bin"
            const val LEXICON_FILE = "lexicon.txt"
            const val VOICE_MANIFEST_ASSET = "manifest.json"
            const val NUM_THREADS = 2
            const val MIN_BUFFER_BYTES = 8192
            const val MIN_CHUNK_SAMPLES = 4096
            const val CHUNKS_PER_SECOND = 8
            const val POLL_MILLIS = 50L
            const val TAG = "PiperTtsEngine"
        }
    }

/** Paquete Piper instalado con fonemas precalculados (ADR-057). */
private data class PiperVoice(
    val dir: File,
    val id: String,
    val phonemes: VoicePhonemeData,
) {
    val onnxFile: File = File(dir, "$id.onnx")
    val tokensFile: File = File(dir, "tokens.txt")
    val lexiconFile: File = File(dir, "lexicon.txt")
}

private data class VoiceFile(
    val path: String,
    val sha256: String,
)

/** Entrada del manifest de voces (models/manifest.json). */
private data class VoiceEntry(
    val id: String,
    val engine: String,
    val language: String,
    val path: String,
    val sha256: String,
    val files: List<VoiceFile>,
)

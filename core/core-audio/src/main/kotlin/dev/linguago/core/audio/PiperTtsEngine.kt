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
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * TTS offline Piper via sherpa-onnx (ADR-003/ADR-010, Fase 2.2). Carga los
 * paquetes `vits-piper-<voz>` dejados por `fetchVoiceModels` (assets), los
 * instala en filesDir la primera vez, verifica SHA-256 (ModelIntegrity) y
 * sintetiza. Sin voz instalada queda no listo y [DefaultTtsEngine] cae al
 * TTS del sistema.
 */
@Singleton
public class PiperTtsEngine
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) : TtsEngine {
        private val engines = ConcurrentHashMap<Language, OfflineTts>()
        private val voices = ConcurrentHashMap<Language, PiperVoice>()
        private val ioLock = Any()

        override fun isReadyFor(language: Language): Boolean = locateVoice(language) != null

        override suspend fun speak(
            text: String,
            language: Language,
        ) {
            val voice = voices[language] ?: locateVoice(language) ?: return
            withContext(Dispatchers.IO) {
                val engine = engineFor(language, voice) ?: return@withContext
                val audio = engine.generate(text = text, sid = 0, speed = SPEAK_SPEED)
                playBlocking(audio.samples, audio.sampleRate)
            }
        }

        /** Pesado (segundos la primera vez): siempre en IO; protegido por [ioLock]. */
        private fun engineFor(
            language: Language,
            voice: PiperVoice,
        ): OfflineTts? =
            engines.getOrPut(language) {
                synchronized(ioLock) {
                    runCatching { OfflineTts(config = buildConfig(voice)) }.getOrNull()
                }
            }

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
                track.stop()
                track.release()
            }
        }

        private fun minBufferSize(sampleRate: Int): Int =
            AudioTrack
                .getMinBufferSize(
                    sampleRate,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_FLOAT,
                ).coerceAtLeast(MIN_BUFFER_BYTES)

        /** Localiza la voz instalada (copiada desde assets y verificada). */
        private fun locateVoice(language: Language): PiperVoice? {
            val cached = voices[language]
            if (cached != null) return cached
            val entry =
                readManifest().firstOrNull { entry ->
                    entry.engine == PIPER &&
                        entry.language == language.isoCode &&
                        entry.sha256.isNotBlank()
                } ?: run {
                    if (readManifest().isEmpty()) {
                        Log.w(TAG, "manifest.json no legible o sin entradas tts")
                    } else {
                        Log.w(TAG, "sin voz piper para ${language.isoCode}")
                    }
                    return null
                }
            val installed =
                installedDir(entry)
                    ?: installFromAssets(entry)
                    ?: run {
                        Log.w(TAG, "no se pudo instalar ${entry.path}")
                        return null
                    }
            if (!ModelIntegrity
                    .sha256Hex(File(installed, "${entry.id}.onnx"))
                    .equals(entry.sha256, ignoreCase = true)
            ) {
                Log.w(TAG, "sha256 desigual para ${entry.id}")
                return null
            }
            val voice =
                PiperVoice(
                    dir = installed,
                    id = entry.id,
                )
            voices[language] = voice
            return voice
        }

        /** Dir en filesDir con onnx + tokens + espeak-ng-data ya instalados. */
        private fun installedDir(entry: VoiceEntry): File? {
            val dir = File(context.filesDir, entry.path)
            return dir.takeIf { hasVoiceFiles(it, entry.id) }
        }

        /** Extrae el paquete `tts/<id>` de assets hacia filesDir (una vez). */
        private fun installFromAssets(entry: VoiceEntry): File? =
            try {
                val hasAssets = context.assets.list(entry.path)?.isNotEmpty() == true
                if (!hasAssets) {
                    Log.w(TAG, "assets.list('${entry.path}') vacío")
                    null
                } else {
                    val outDir = File(context.filesDir, entry.path)
                    outDir.deleteRecursively()
                    copyAssetTree(entry.path, outDir)
                    if (hasVoiceFiles(outDir, entry.id)) outDir else null
                }
            } catch (error: Exception) {
                Log.w(TAG, "install ${entry.path} falló", error)
                null
            }

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
            const val SPEAK_SPEED = 1.0f
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

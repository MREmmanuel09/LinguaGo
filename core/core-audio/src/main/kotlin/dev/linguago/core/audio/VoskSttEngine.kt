package dev.linguago.core.audio

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.linguago.core.domain.model.Language
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import org.vosk.Model
import org.vosk.Recognizer
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * STT offline con Vosk Small (ADR-003, Fase 2.3). Los paquetes `vosk-*`
 * los deja `fetchVoiceModels` (assets); se instalan en filesDir la primera
 * vez y se verifican por archivo (SHA-256, ADR-006: `files[].sha256`).
 * Sin modelo verificado queda no listo y SpeakRepeat degrada (I3: el
 * ejercicio nunca se bloquea).
 */
@Singleton
public class VoskSttEngine
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) : SttEngine {
        private val models = ConcurrentHashMap<Language, Model>()
        private val ioLock = Any()

        override fun isReadyFor(language: Language): Boolean = locateModel(language) != null

        override suspend fun transcribe(language: Language): TranscriptionResult {
            val model = models[language] ?: locateModel(language) ?: return EMPTY_RESULT
            return withContext(Dispatchers.IO) { recordOnce(model) }
        }

        /** Captura por ~6 s el micrófono (16 kHz mono PCM) y transcribe. */
        @SuppressLint("MissingPermission")
        private suspend fun recordOnce(model: Model): TranscriptionResult {
            val minBuffer =
                AudioRecord
                    .getMinBufferSize(
                        SAMPLE_RATE_HZ,
                        AudioFormat.CHANNEL_IN_MONO,
                        AudioFormat.ENCODING_PCM_16BIT,
                    ).coerceAtLeast(CHUNK_BYTES)
            val recognizer = Recognizer(model, SAMPLE_RATE_HZ.toFloat())
            val recorder =
                AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    SAMPLE_RATE_HZ,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    minBuffer,
                )
            val buffer = ByteArray(CHUNK_BYTES)
            var text = ""
            var finalHit = false
            try {
                recorder.startRecording()
                val deadline = System.nanoTime() + MAX_CAPTURE_NANOS
                while (!finalHit && System.nanoTime() < deadline) {
                    currentCoroutineContext().ensureActive()
                    val read = recorder.read(buffer, 0, buffer.size)
                    if (read > 0) {
                        if (recognizer.acceptWaveForm(buffer, read)) {
                            text = JSONObject(recognizer.result).optString("text", "")
                            if (text.isNotBlank()) {
                                finalHit = true
                            }
                        }
                        if (!finalHit) {
                            val partial = JSONObject(recognizer.partialResult).optString("partial", "")
                            if (partial.isNotBlank()) text = partial
                        }
                    }
                }
                if (text.isBlank()) {
                    text = JSONObject(recognizer.finalResult).optString("text", "")
                }
            } finally {
                runCatching { recorder.stop() }
                runCatching { recorder.release() }
                runCatching { recognizer.close() }
            }
            val confidence = if (text.isBlank()) MEANINGLESS_CONFIDENCE else CONFIDENT
            return TranscriptionResult(text = text.trim(), confidence = confidence)
        }

        /** Modelo instalado y verificado para el idioma; carga pesada en IO. */
        private fun locateModel(language: Language): Model? {
            val entry = readManifestStt(language) ?: return null
            val verified = verifiedDir(entry) ?: installFromAssets(entry) ?: return null
            models[language]?.let { return it }
            return synchronized(ioLock) {
                models[language]
                    ?: runCatching { Model(verified.absolutePath) }
                        .onFailure { error ->
                            Log.w(TAG, "no se pudo cargar modelo STT para ${language.isoCode}", error)
                        }.getOrNull()
                        ?.also { model -> models[language] = model }
            }
        }

        /** filesDir con todos los archivos del manifest presentes y con hash OK. */
        private fun verifiedDir(entry: SttEntry): File? {
            if (entry.files.isEmpty()) return null
            val dir = File(context.filesDir, entry.dir)
            if (!dir.isDirectory) return null
            val allVerified =
                entry.files.all { expected ->
                    val candidate = File(context.filesDir, expected.path)
                    candidate.isFile &&
                        ModelIntegrity
                            .sha256Hex(candidate)
                            .equals(expected.sha256, ignoreCase = true)
                }
            return if (allVerified) dir else null
        }

        /** Extrae el paquete `stt/<id>` de assets hacia filesDir (una vez). */
        private fun installFromAssets(entry: SttEntry): File? =
            runCatching {
                val hasAssets = context.assets.list(entry.dir)?.isNotEmpty() == true
                if (!hasAssets) return@runCatching null
                val outDir = File(context.filesDir, entry.dir)
                outDir.deleteRecursively()
                copyAssetTree(entry.dir, outDir)
                outDir
            }.getOrNull()

        /** Copia recursiva (vosk zip: conf/, am/, graph/ incluidos). */
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
                    copyAssetTree(assetPath = childAsset, target = childFile)
                }
            }
        }

        private fun readManifestStt(language: Language): SttEntry? =
            runCatching {
                context.assets
                    .open(VOICE_MANIFEST_ASSET)
                    .use { input ->
                        val root = JSONObject(input.bufferedReader().readText())
                        val array = root.optJSONArray("stt") ?: JSONArray()
                        (0 until array.length()).mapNotNull { index ->
                            val item = array.getJSONObject(index)
                            if (item.optString("engine") != VOSK) return@mapNotNull null
                            if (item.optString("language") != language.isoCode) return@mapNotNull null
                            val filesArray = item.optJSONArray("files") ?: JSONArray()
                            val files =
                                (0 until filesArray.length()).map { fileIndex ->
                                    val file = filesArray.getJSONObject(fileIndex)
                                    SttFile(
                                        path = file.getString("path"),
                                        sha256 = file.getString("sha256"),
                                    )
                                }
                            SttEntry(
                                dir = item.getString("path"),
                                language = item.getString("language"),
                                files = files,
                            )
                        }
                    }.firstOrNull { entry -> entry.files.isNotEmpty() }
            }.getOrNull()

        private data class SttEntry(
            val dir: String,
            val language: String,
            val files: List<SttFile>,
        )

        private data class SttFile(
            val path: String,
            val sha256: String,
        )

        private companion object {
            const val TAG = "VoskSttEngine"
            const val VOSK = "vosk"
            const val VOICE_MANIFEST_ASSET = "manifest.json"
            const val SAMPLE_RATE_HZ = 16000
            const val CHUNK_BYTES = 8192
            const val MAX_CAPTURE_NANOS = 6_000_000_000L
            const val CONFIDENT = 1f
            const val MEANINGLESS_CONFIDENCE = 0f
            val EMPTY_RESULT = TranscriptionResult(text = "", confidence = 0f)
        }
    }

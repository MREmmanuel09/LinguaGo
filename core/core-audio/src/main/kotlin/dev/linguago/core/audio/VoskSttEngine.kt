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
import kotlinx.coroutines.flow.StateFlow
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
 * STT offline con Vosk Small (ADR-003/ADR-020, Fase 2.3). Los paquetes
 * `vosk-*` los deja `fetchVoiceModels` (assets); se instalan en filesDir de
 * forma atómica la primera vez y se verifican por archivo (SHA-256,
 * ADR-006: `files[].sha256`). El warmup ([warmUp]) carga `org.vosk.Model`
 * (JNA) en `Dispatchers.IO` con Mutex por idioma y estado observable
 * ([stateFor]); [isReadyFor] solo lee memoria. Sin modelo verificado degrada
 * (I3: el ejercicio nunca se bloquea).
 */
@Singleton
public class VoskSttEngine
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) : SttEngine {
        private val models = ConcurrentHashMap<Language, Model>()
        private val warmup = WarmupStateRegistry()

        override fun isReadyFor(language: Language): Boolean = warmup.isReady(language)

        override fun stateFor(language: Language): StateFlow<AudioEngineState> = warmup.stateOf(language)

        override suspend fun warmUp(language: Language) {
            warmup.warmUpOnce(language) {
                withContext(Dispatchers.IO) { warmUpBlocking(language) }
            }
        }

        override suspend fun transcribe(language: Language): TranscriptionResult {
            if (!isReadyFor(language)) warmUp(language)
            val model = models[language] ?: return EMPTY_RESULT
            return withContext(Dispatchers.IO) { recordOnce(model) }
        }

        /** Verifica, extrae y carga el modelo en IO; publica el estado. */
        private fun warmUpBlocking(language: Language) {
            val state = warmup.stateOf(language)
            state.value = AudioEngineState.Verifying
            val entry = readManifestStt(language)
            if (entry == null) {
                Log.w(TAG, "sin modelo vosk para ${language.isoCode}")
                state.value = AudioEngineState.Degraded("sin modelo vosk para ${language.isoCode}")
                return
            }
            val installed = verifiedDir(entry)
            val dir =
                if (installed != null) {
                    installed
                } else {
                    state.value = AudioEngineState.Extracting
                    extractModel(entry)
                }
            if (dir == null) {
                Log.w(TAG, "no se pudo instalar ${entry.dir}")
                state.value = AudioEngineState.Degraded("instalación de ${entry.dir} falló")
                return
            }
            state.value = AudioEngineState.Loading
            val model =
                runCatching { Model(dir.absolutePath) }
                    .onFailure { error ->
                        Log.w(TAG, "no se pudo cargar modelo STT para ${language.isoCode}", error)
                    }.getOrNull()
            if (model == null) {
                state.value = AudioEngineState.Degraded("carga de ${entry.dir} falló")
                return
            }
            models[language] = model
            state.value = AudioEngineState.Ready
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

        /** filesDir con todos los archivos del manifest presentes y con hash OK. */
        private fun verifiedDir(entry: SttEntry): File? {
            if (entry.files.isEmpty()) return null
            val dir = File(context.filesDir, entry.dir)
            if (!dir.isDirectory) return null
            return dir.takeIf { filesValid(it, entry) }
        }

        private fun filesValid(
            dir: File,
            entry: SttEntry,
        ): Boolean =
            entry.files.all { expected ->
                val candidate = File(dir, expected.path.removePrefix("${entry.dir}/"))
                candidate.isFile &&
                    ModelIntegrity
                        .sha256Hex(candidate)
                        .equals(expected.sha256, ignoreCase = true)
            }

        /** Extrae `stt/<id>` a staging y lo publica con rename atómico. */
        private fun extractModel(entry: SttEntry): File? =
            AtomicInstall.install(
                stagingDir = File(context.filesDir, "${entry.dir}.staging"),
                targetDir = File(context.filesDir, entry.dir),
                copy = { dir -> copyAssetTree(entry.dir, dir) },
                verify = { dir -> filesValid(dir, entry) },
            )

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

package dev.linguago.core.audio

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/** Espacio ocupado por las voces y modelos extraídos en `filesDir` (ADR-051). */
public data class VoiceStorageInfo(
    public val ttsBytes: Long,
    public val sttBytes: Long,
) {
    public val totalBytes: Long get() = ttsBytes + sttBytes
}

/**
 * Gestor del ciclo de vida del almacenamiento de voz (ADR-051): consulta el
 * espacio extraído y permite liberarlo desde Ajustes. Al liberar se cierran
 * primero los motores nativos; la próxima reproducción vuelve a extraer los
 * assets sin red (I1) y sin bloquear el aprendizaje (I3).
 */
@Singleton
public class VoiceStorageManager
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
        private val piper: PiperTtsEngine,
        private val vosk: VoskSttEngine,
    ) {
        /** Uso actual de `filesDir/tts` y `filesDir/stt`. */
        public suspend fun usage(): VoiceStorageInfo =
            withContext(Dispatchers.IO) {
                VoiceStorageInfo(
                    ttsBytes = directorySize(File(context.filesDir, TTS_DIR)),
                    sttBytes = directorySize(File(context.filesDir, STT_DIR)),
                )
            }

        /** Libera las copias extraídas; los assets empaquetados permanecen intactos. */
        public suspend fun clearExtractedCache(): VoiceStorageInfo {
            piper.release()
            vosk.release()
            withContext(Dispatchers.IO) {
                File(context.filesDir, TTS_DIR).deleteRecursively()
                File(context.filesDir, STT_DIR).deleteRecursively()
            }
            return usage()
        }

        private fun directorySize(dir: File): Long =
            if (!dir.isDirectory) {
                0L
            } else {
                dir.walkTopDown().filter { it.isFile }.sumOf { it.length() }
            }

        private companion object {
            const val TTS_DIR = "tts"
            const val STT_DIR = "stt"
        }
    }

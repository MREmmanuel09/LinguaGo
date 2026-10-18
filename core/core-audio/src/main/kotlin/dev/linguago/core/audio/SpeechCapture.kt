package dev.linguago.core.audio

import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlin.math.log10
import kotlin.math.max
import kotlin.math.sqrt

/** Actualización de la captura en vivo para el visualizador de onda (ADR-020). */
public data class SpeechCaptureUpdate(
    /** Energía RMS en dBFS (≤ 0; el silencio cae al piso [SpeechCapture.SILENCE_DB]). */
    val rmsDb: Float,
    /** Último texto parcial de Vosk ("" mientras no haya). */
    val partialText: String,
    /** Texto final cuando la captura terminó; `null` mientras está en curso. */
    val finalText: String? = null,
)

/** Fuente de PCM 16-bit mono; el bucle reutiliza un único buffer de lectura. */
internal interface PcmSource {
    fun start()

    /** Llena [buffer] y devuelve bytes leídos; negativo indica error fatal. */
    fun read(buffer: ByteArray): Int

    fun release()
}

/**
 * Fachada de `org.vosk.Recognizer` (JNA) que resuelve el JSON fuera del
 * bucle: la captura solo maneja texto y así es testeable en JVM con fakes.
 */
internal interface StreamRecognizer {
    fun acceptWaveForm(
        buffer: ByteArray,
        length: Int,
    ): Boolean

    fun resultText(): String

    fun partialText(): String

    fun finalText(): String

    fun close()
}

/**
 * Bucle de captura streaming (ADR-020): lee chunks de ~50 ms (1600 bytes a
 * 16 kHz mono 16-bit), calcula la energía RMS sin asignaciones por frame y
 * emite un [SpeechCaptureUpdate] por lectura. Termina con el endpoint de
 * Vosk, al agotar [maxCaptureNanos] o por cancelación cooperativa; en todos
 * los casos libera la fuente y cierra el recognizer en `finally`.
 */
internal class SpeechCapture(
    private val source: PcmSource,
    private val recognizer: StreamRecognizer,
    private val nanoTime: () -> Long,
    private val maxCaptureNanos: Long = DEFAULT_MAX_CAPTURE_NANOS,
) {
    /**
     * Captura hasta el endpoint/plazo y devuelve el texto final; la última
     * emisión lleva [SpeechCaptureUpdate.finalText] no nulo.
     */
    suspend fun run(emit: suspend (SpeechCaptureUpdate) -> Unit): String {
        val buffer = ByteArray(STREAM_CHUNK_BYTES)
        var text = ""
        var partial = ""
        var endpointHit = false
        var readFailed = false
        var rmsDb = SILENCE_DB
        source.start()
        try {
            val deadline = nanoTime() + maxCaptureNanos
            while (!endpointHit && !readFailed && nanoTime() < deadline) {
                currentCoroutineContext().ensureActive()
                val read = source.read(buffer)
                if (read < 0) {
                    readFailed = true
                } else if (read > 0) {
                    rmsDb = rmsDbOf(buffer, read)
                    if (recognizer.acceptWaveForm(buffer, read)) {
                        text = recognizer.resultText()
                        if (text.isNotBlank()) endpointHit = true
                    }
                    if (!endpointHit) partial = recognizer.partialText()
                    emit(SpeechCaptureUpdate(rmsDb = rmsDb, partialText = partial))
                }
            }
            if (!endpointHit) text = partial.ifBlank { recognizer.finalText() }
            emit(SpeechCaptureUpdate(rmsDb = rmsDb, partialText = partial, finalText = text))
            return text
        } finally {
            source.release()
            recognizer.close()
        }
    }

    /** RMS en dBFS sobre PCM 16-bit little-endian, sin asignaciones al heap. */
    private fun rmsDbOf(
        buffer: ByteArray,
        length: Int,
    ): Float {
        var sum = 0L
        var index = 0
        while (index + 1 < length) {
            val sample = (buffer[index].toInt() and 0xFF) or (buffer[index + 1].toInt() shl 8)
            sum += sample.toLong() * sample
            index += 2
        }
        val samples = length / 2
        if (samples == 0) return SILENCE_DB
        val rms = sqrt(sum.toDouble() / samples)
        if (rms <= 0.0) return SILENCE_DB
        val db = DBFS_FACTOR * log10(rms / PCM_FULL_SCALE)
        return max(SILENCE_DB, db.toFloat())
    }

    internal companion object {
        /** Piso de silencio del visualizador (dBFS). */
        const val SILENCE_DB = -60f

        /** Lectura de 1600 bytes ≈ 50 ms a 16 kHz mono 16-bit. */
        const val STREAM_CHUNK_BYTES = 1600
        const val DEFAULT_MAX_CAPTURE_NANOS = 6_000_000_000L
        private const val DBFS_FACTOR = 20.0
        private const val PCM_FULL_SCALE = 32768.0
    }
}

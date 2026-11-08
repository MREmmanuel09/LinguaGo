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
 * emite un [SpeechCaptureUpdate] por lectura. Termina al agotar
 * [maxCaptureNanos] o por cancelación cooperativa; en todos los casos libera
 * la fuente y cierra el recognizer en `finally`.
 *
 * Tolerancia al endpoint prematuro de Vosk (ADR-043): el endpoint corta en
 * pausas breves y deja frases truncadas. Con [minCaptureNanos], todo
 * endpoint con texto que llegue antes de ese umbral no corta la captura: su
 * enunciado se concatena y se sigue escuchando; uno posterior abre una
 * ventana de gracia de [endpointGraceNanos]. Si dentro de la gracia llega
 * otro enunciado se concatena y la gracia se reinicia; si expira sin
 * nuevo audio, la captura termina con el texto acumulado.
 */
internal class SpeechCapture(
    private val createSource: () -> PcmSource,
    private val createRecognizer: () -> StreamRecognizer,
    private val nanoTime: () -> Long,
    private val maxCaptureNanos: Long = DEFAULT_MAX_CAPTURE_NANOS,
    private val minCaptureNanos: Long = DEFAULT_MIN_CAPTURE_NANOS,
    private val endpointGraceNanos: Long = DEFAULT_ENDPOINT_GRACE_NANOS,
) {
    constructor(
        source: PcmSource,
        recognizer: StreamRecognizer,
        nanoTime: () -> Long,
        maxCaptureNanos: Long = DEFAULT_MAX_CAPTURE_NANOS,
        minCaptureNanos: Long = DEFAULT_MIN_CAPTURE_NANOS,
        endpointGraceNanos: Long = DEFAULT_ENDPOINT_GRACE_NANOS,
    ) : this({ source }, { recognizer }, nanoTime, maxCaptureNanos, minCaptureNanos, endpointGraceNanos)

    /**
     * Captura hasta la expiración de la gracia del endpoint, el plazo o la
     * cancelación, y devuelve el texto final; la última emisión lleva
     * [SpeechCaptureUpdate.finalText] no nulo.
     */
    suspend fun run(emit: suspend (SpeechCaptureUpdate) -> Unit): String {
        val buffer = ByteArray(STREAM_CHUNK_BYTES)
        var text = ""
        var partial = ""
        var lastEndpointAt: Long? = null
        var rmsDb = SILENCE_DB
        val source = createSource()
        var recognizer: StreamRecognizer? = null
        try {
            val activeRecognizer = createRecognizer()
            recognizer = activeRecognizer
            source.start()
            val startedAt = nanoTime()
            val deadline = startedAt + maxCaptureNanos
            val minUntil = startedAt + minCaptureNanos
            var readFailed = false
            var graceDone = false
            while (!readFailed && !graceDone && nanoTime() < deadline) {
                currentCoroutineContext().ensureActive()
                val now = nanoTime()
                val endpointAt = lastEndpointAt
                if (endpointAt != null && now - endpointAt >= endpointGraceNanos) {
                    graceDone = true
                } else {
                    val read = source.read(buffer)
                    if (read < 0) {
                        readFailed = true
                    } else if (read > 0) {
                        rmsDb = rmsDbOf(buffer, read)
                        val finalized = activeRecognizer.acceptWaveForm(buffer, read)
                        val hypothesis = if (finalized) activeRecognizer.resultText() else ""
                        if (hypothesis.isNotBlank()) {
                            text = merge(text, hypothesis)
                        }
                        if (finalized && hypothesis.isNotBlank() && now >= minUntil) {
                            lastEndpointAt = now
                        }
                        partial = activeRecognizer.partialText()
                        emit(SpeechCaptureUpdate(rmsDb = rmsDb, partialText = partial))
                    }
                }
            }
            text = merge(text, partial.ifBlank { activeRecognizer.finalText() })
            emit(SpeechCaptureUpdate(rmsDb = rmsDb, partialText = partial, finalText = text))
            return text
        } finally {
            runCatching { source.release() }
            runCatching { recognizer?.close() }
        }
    }

    /**
     * Concatena enunciados sucesivos sin duplicar el que ya termina igual:
     * el `finalResult` de Vosk repite el último enunciado reconocido.
     */
    private fun merge(
        previous: String,
        next: String,
    ): String =
        when {
            next.isBlank() -> previous
            previous.endsWith(next) -> previous
            else -> if (previous.isBlank()) next else "$previous $next"
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

        /** Tope absoluto de captura: 10 s (2.0.1; antes eran 6 s). */
        const val DEFAULT_MAX_CAPTURE_NANOS = 10_000_000_000L

        /** Un endpoint con texto antes de esto es prematuro y no corta. */
        const val DEFAULT_MIN_CAPTURE_NANOS = 1_500_000_000L

        /** Escucha extra tras un enunciado para capturar su continuación. */
        const val DEFAULT_ENDPOINT_GRACE_NANOS = 1_200_000_000L
        private const val DBFS_FACTOR = 20.0
        private const val PCM_FULL_SCALE = 32768.0
    }
}

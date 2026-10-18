package dev.linguago.core.audio

import dev.linguago.core.domain.model.Language
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/**
 * Contrato TTS (ADR-003, RFC §4). Toda implementación (Piper/sherpa-onnx,
 * Kokoro, Supertonic o el TTS del sistema como fallback accesible) se
 * adapta a esta interfaz; la app no conoce el motor concreto (I1: offline,
 * la voz no depende de red).
 */
interface TtsEngine {
    /** Indica si el motor está listo para el idioma (consulta en memoria, ADR-020). */
    fun isReadyFor(language: Language): Boolean

    /** Estado observable del warmup del motor para [language] (ADR-020). */
    fun stateFor(language: Language): StateFlow<AudioEngineState>

    /** Pre-calienta el motor en `Dispatchers.IO` (idempotente y concurrente). */
    suspend fun warmUp(language: Language)

    /** Sintetiza y reproduce [text] en [language] a [speed] (1.0 = normal, 0.75 = lento). */
    suspend fun speak(
        text: String,
        language: Language,
        speed: Float = 1.0f,
    )
}

/**
 * Contrato STT (ADR-003, RFC §4). Implementaciones: Vosk Small (default),
 * Vosk Large (premium descargable), Whisper (alternativa).
 */
interface SttEngine {
    /** Indica si el motor está listo para el idioma (consulta en memoria, ADR-020). */
    fun isReadyFor(language: Language): Boolean

    /** Estado observable del warmup del motor para [language] (ADR-020). */
    fun stateFor(language: Language): StateFlow<AudioEngineState>

    /** Pre-calienta el motor en `Dispatchers.IO` (idempotente y concurrente). */
    suspend fun warmUp(language: Language)

    /**
     * Captura en vivo (ADR-020): emite energía RMS cada ~50 ms y los
     * parciales de Vosk; la última emisión trae el texto final. El flujo
     * cierra micrófono y recognizer al completarse o cancelarse.
     */
    fun observeTranscription(language: Language): Flow<SpeechCaptureUpdate>

    /** Reconoce el habla del usuario y devuelve la transcripción. */
    suspend fun transcribe(language: Language): TranscriptionResult
}

/** Resultado de una transcripción (normalización de acentos incluida). */
data class TranscriptionResult(
    val text: String,
    val confidence: Float,
)

package dev.linguago.core.audio

import dev.linguago.core.domain.model.Language

/**
 * Contrato TTS (ADR-003, RFC §4). Toda implementación (Piper/sherpa-onnx,
 * Kokoro, Supertonic o el TTS del sistema como fallback accesible) se
 * adapta a esta interfaz; la app no conoce el motor concreto (I1: offline,
 * la voz no depende de red).
 */
interface TtsEngine {
    /** Indica si el motor está preparado para el idioma dado. */
    fun isReadyFor(language: Language): Boolean

    /** Sintetiza y reproduce [text] en [language]; cancelable por token. */
    suspend fun speak(
        text: String,
        language: Language,
    )
}

/**
 * Contrato STT (ADR-003, RFC §4). Implementaciones: Vosk Small (default),
 * Vosk Large (premium descargable), Whisper (alternativa).
 */
interface SttEngine {
    /** Indica si el motor está preparado para el idioma dado. */
    fun isReadyFor(language: Language): Boolean

    /** Reconoce el habla del usuario y devuelve la transcripción. */
    suspend fun transcribe(language: Language): TranscriptionResult
}

/** Resultado de una transcripción (normalización de acentos incluida). */
data class TranscriptionResult(
    val text: String,
    val confidence: Float,
)

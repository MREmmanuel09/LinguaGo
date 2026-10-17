package dev.linguago.core.audio

import dev.linguago.core.domain.model.Language
import javax.inject.Inject

/**
 * Compone TTS (ADR-010, Fase 2.2): Piper offline si la voz está instalada y
 * verificada; si no, el TTS del sistema (siempre disponible, único fallback
 * para japonés). Estrategia transparente para las features.
 */
public class DefaultTtsEngine
    @Inject
    constructor(
        private val piper: PiperTtsEngine,
        private val system: SystemTtsEngine,
    ) : TtsEngine {
        override fun isReadyFor(language: Language): Boolean = piper.isReadyFor(language) || system.isReadyFor(language)

        override suspend fun speak(
            text: String,
            language: Language,
        ) {
            if (piper.isReadyFor(language)) {
                piper.speak(text, language)
            } else {
                system.speak(text, language)
            }
        }
    }

package dev.linguago.core.audio

import dev.linguago.core.domain.model.Language
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

/**
 * Compone TTS (ADR-010/ADR-057): Piper offline cuando la voz está instalada,
 * verificada y tiene fonemas precalculados para el texto; si no, el TTS del
 * sistema (siempre disponible y único fallback para japonés o textos sin
 * cobertura). [stateFor] refleja el warmup del motor offline; [isReadyFor]
 * incluye el fallback del sistema (sin I/O, ADR-020).
 */
public class DefaultTtsEngine
    @Inject
    constructor(
        private val piper: PiperTtsEngine,
        private val system: SystemTtsEngine,
    ) : TtsEngine {
        override fun isReadyFor(language: Language): Boolean = piper.isReadyFor(language) || system.isReadyFor(language)

        override fun stateFor(language: Language): StateFlow<AudioEngineState> = piper.stateFor(language)

        override suspend fun warmUp(language: Language) {
            piper.warmUp(language)
            system.warmUp(language)
        }

        override suspend fun speak(
            text: String,
            language: Language,
            speed: Float,
        ) {
            if (!piper.isReadyFor(language)) {
                piper.warmUp(language)
            }
            if (piper.canSpeak(language, text)) {
                piper.speak(text, language, speed)
            } else {
                system.speak(text, language, speed)
            }
        }
    }

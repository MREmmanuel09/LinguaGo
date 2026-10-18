package dev.linguago.core.audio

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.linguago.core.domain.model.Language
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

/**
 * TTS del sistema (ADR-003/010): TextToSpeech de Android con selección
 * de voz por idioma. Es el motor disponible en dispositivo por defecto;
 * Piper/sherpa-onnx lo sustituirá cuando los modelos existan (degradación
 * documentada en README, los ejercicios no se bloqueán sin voz).
 */
@Singleton
public class SystemTtsEngine
    @Inject
    constructor(
        @ApplicationContext context: Context,
    ) : TtsEngine {
        private val utteranceIds = AtomicInteger(0)
        private val warmup = WarmupStateRegistry()

        @Volatile
        private var initialized: Boolean = false

        private val tts =
            TextToSpeech(context) { status ->
                initialized = status == TextToSpeech.SUCCESS
            }

        override fun isReadyFor(language: Language): Boolean =
            initialized && tts.isLanguageAvailable(language.toLocale()) >= TextToSpeech.LANG_AVAILABLE

        override fun stateFor(language: Language): StateFlow<AudioEngineState> = warmup.stateOf(language)

        override suspend fun warmUp(language: Language) {
            warmup.warmUpOnce(language) {
                warmup.stateOf(language).value =
                    if (isReadyFor(language)) {
                        AudioEngineState.Ready
                    } else {
                        AudioEngineState.Degraded("voz del sistema no disponible para ${language.isoCode}")
                    }
            }
        }

        override suspend fun speak(
            text: String,
            language: Language,
        ) {
            require(initialized) { "SystemTtsEngine no está inicializado" }
            if (tts.setLanguage(language.toLocale()) == TextToSpeech.LANG_NOT_SUPPORTED) return

            suspendCancellableCoroutine { continuation ->
                val utteranceId = "linguago-${utteranceIds.incrementAndGet()}"
                tts.setOnUtteranceProgressListener(
                    object : UtteranceProgressListener() {
                        override fun onDone(utteranceId: String?) {
                            continuation.resume(Unit)
                        }

                        override fun onError(utteranceId: String?) {
                            // I7: la lección no se bloquea si la voz falla.
                            continuation.resume(Unit)
                        }

                        override fun onStart(utteranceId: String?) = Unit
                    },
                )
                continuation.invokeOnCancellation { tts.stop() }
                tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
            }
        }
    }

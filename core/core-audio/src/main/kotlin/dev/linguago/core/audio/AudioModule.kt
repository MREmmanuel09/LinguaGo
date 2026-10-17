package dev.linguago.core.audio

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Audio (Fase 2): TTS con Piper (sherpa-onnx) y fallback al sistema;
 * STT con Vosk Small. Los motores se intercambian aquí (Strategy, ADR-003)
 * sin tocar features.
 */
@Module
@InstallIn(SingletonComponent::class)
internal abstract class AudioModule {
    @Binds
    internal abstract fun bindsTtsEngine(impl: DefaultTtsEngine): TtsEngine

    @Binds
    internal abstract fun bindsSttEngine(impl: VoskSttEngine): SttEngine
}

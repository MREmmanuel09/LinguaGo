package dev.linguago.core.audio

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Audio (Fase 2): la app habla con la voz del sistema por defecto;
 * los motores con modelos (Piper/sherpa-onnx) se intercambian aquí
 * (Strategy, ADR-003) sin tocar features.
 */
@Module
@InstallIn(SingletonComponent::class)
internal abstract class AudioModule {
    @Binds
    internal abstract fun bindsTtsEngine(impl: SystemTtsEngine): TtsEngine
}

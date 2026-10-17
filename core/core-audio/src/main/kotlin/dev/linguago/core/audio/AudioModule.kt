package dev.linguago.core.audio

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Audio (Fase 2): la app habla con Piper offline (sherpa-onnx) cuando la voz
 * del idioma está instalada y verificada; si no, con el TTS del sistema.
 * Los motores se intercambian aquí (Strategy, ADR-003) sin tocar features.
 */
@Module
@InstallIn(SingletonComponent::class)
internal abstract class AudioModule {
    @Binds
    internal abstract fun bindsTtsEngine(impl: DefaultTtsEngine): TtsEngine
}

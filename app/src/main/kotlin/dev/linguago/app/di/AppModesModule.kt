package dev.linguago.app.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.linguago.app.BuildConfig
import javax.inject.Named
import javax.inject.Singleton

/**
 * Interruptor del proveedor de laboratorio (ADR-011): activo solo en
 * builds debug para poder probar el flujo auth → sync de extremo a
 * extremo sin credenciales reales.
 */
@Module
@InstallIn(SingletonComponent::class)
internal object AppModesModule {
    @Provides
    @Singleton
    @Named(AppModesModule.DEV_CLOUD_ENABLED)
    public fun providesDevCloudEnabled(): Boolean = BuildConfig.DEBUG

    public const val DEV_CLOUD_ENABLED: String = "dev_cloud_enabled"
}

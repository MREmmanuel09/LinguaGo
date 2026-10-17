package dev.linguago.app.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import dev.linguago.core.auth.DevCloudProvider
import dev.linguago.core.sync.CloudProvider
import dev.linguago.core.sync.CloudProviderKey
import javax.inject.Singleton

/**
 * Registro del proveedor de laboratorio (ADR-011) solo en builds debug:
 * entra al mapa `CloudProvider` que DelegatingCloudProvider consume.
 */
@Module
@InstallIn(SingletonComponent::class)
internal object DebugCloudProviderModule {
    @Provides
    @IntoMap
    @Singleton
    @CloudProviderKey("dev_local")
    public fun providesDevLocal(provider: DevCloudProvider): CloudProvider = provider
}

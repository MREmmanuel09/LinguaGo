package dev.linguago.core.sync.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.linguago.core.sync.CloudProvider
import dev.linguago.core.sync.NoopCloudProvider
import javax.inject.Singleton

/**
 * Wiring de sync (Fase 3, ADR-013): vincula el placeholder NoopCloudProvider
 * como proveedor por defecto. En Fase 4 (ADR-011) este binding se cambia por
 * Google Drive/OneDrive sin tocar SyncEngine ni la UI (Strategy).
 */
@Module
@InstallIn(SingletonComponent::class)
public abstract class SyncModule {
    @Binds
    @Singleton
    public abstract fun bindsCloudProvider(impl: NoopCloudProvider): CloudProvider
}

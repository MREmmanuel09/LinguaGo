package dev.linguago.core.sync.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.linguago.core.sync.CloudProvider
import dev.linguago.core.sync.DelegatingCloudProvider
import javax.inject.Singleton

/**
 * Wiring de sync (Fase 4, ADR-011): el `CloudProvider` expuesto a
 * SyncEngine es `DelegatingCloudProvider`, que elige el proveedor real
 * vinculado del mapa (`google_drive` de core-auth, `dev_local` en debug)
 * o degrada a NoopCloudProvider (app 100% offline, I3).
 */
@Module
@InstallIn(SingletonComponent::class)
public abstract class SyncModule {
    @Binds
    @Singleton
    public abstract fun bindsCloudProvider(impl: DelegatingCloudProvider): CloudProvider
}

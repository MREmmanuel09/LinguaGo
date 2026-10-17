package dev.linguago.core.auth.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.linguago.core.auth.TokenReader
import dev.linguago.core.auth.TokenStore
import javax.inject.Singleton

/**
 * Bindings de auth (ADR-011): el `TokenStore` persistente es el `TokenReader`
 * que consumen el motor OAuth y el proveedor de Drive.
 */
@Module
@InstallIn(SingletonComponent::class)
public abstract class AuthBindsModule {
    @Binds
    @Singleton
    public abstract fun bindsTokenReader(impl: TokenStore): TokenReader
}

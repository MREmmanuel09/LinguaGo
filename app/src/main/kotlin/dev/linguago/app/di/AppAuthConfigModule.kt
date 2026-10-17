package dev.linguago.app.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.linguago.app.BuildConfig
import dev.linguago.core.auth.OAuthConfigSource
import javax.inject.Singleton

/**
 * Configuración OAuth de la app (ADR-011): el client id llega por gradle
 * property (`googleClientId` en local.properties o línea de comandos) y se
 * compila en BuildConfig; sin él, el proveedor real queda deshabilitado en
 * la UI y la app completa funciona offline (I3).
 */
@Module
@InstallIn(SingletonComponent::class)
internal object AppAuthConfigModule {
    @Provides
    @Singleton
    public fun providesAuthConfig(): OAuthConfigSource =
        object : OAuthConfigSource {
            override val clientId: String? =
                BuildConfig.GOOGLE_CLIENT_ID.takeIf { it.isNotBlank() }
        }
}

package dev.linguago.app.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.linguago.app.BuildConfig
import dev.linguago.core.auth.OAuthConfigSource
import javax.inject.Singleton

/**
 * Configuración OAuth de la app (ADR-011): client id y redirect se
 * compilan desde gradle properties (`googleClientId`, `oauthRedirectScheme`,
 * `oauthRedirectUri`); sin client id, el proveedor real queda deshabilitado
 * en la UI y la app completa funciona offline (I3).
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

            override val redirectScheme: String =
                BuildConfig.OAUTH_REDIRECT_SCHEME
                    .ifBlank { "dev.linguago.oauth" }

            override val redirectUri: String =
                BuildConfig.OAUTH_REDIRECT_URI
                    .ifBlank { "$redirectScheme://callback" }
        }
}

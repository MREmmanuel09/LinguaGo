package dev.linguago.core.auth.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import dev.linguago.core.auth.GoogleDriveCloudProvider
import dev.linguago.core.auth.KeystoreCryptoService
import dev.linguago.core.common.CryptoService
import dev.linguago.core.common.NoOpCryptoService
import dev.linguago.core.sync.CloudProvider
import dev.linguago.core.sync.CloudProviderKey
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import java.security.SecureRandom
import java.time.Duration
import javax.inject.Singleton

/**
 * Wiring DI de auth (ADR-011): red (OkHttp), JSON del codec de tokens y el
 * proveedor real registrado en el mapa de `CloudProvider` (Strategy).
 * El proveedor de laboratorio (`dev_local`) se registra solo en debug, via
 * el módulo de variante del app.
 */
@Module
@InstallIn(SingletonComponent::class)
public object AuthModule {
    @Provides
    @Singleton
    public fun okHttp(): OkHttpClient =
        OkHttpClient
            .Builder()
            .connectTimeout(Duration.ofSeconds(30))
            .readTimeout(Duration.ofSeconds(60))
            .build()

    @Provides
    @Singleton
    public fun secureRandom(): SecureRandom = SecureRandom()

    @Provides
    @Singleton
    public fun cryptoService(): CryptoService =
        try {
            KeystoreCryptoService()
        } catch (error: Exception) {
            NoOpCryptoService()
        }

    @Provides
    @Singleton
    public fun json(): Json =
        Json {
            encodeDefaults = false
            ignoreUnknownKeys = false
            isLenient = false
        }

    /** Registra Google Drive en el mapa de proveedores (CloudProviderKey). */
    @Provides
    @IntoMap
    @Singleton
    @CloudProviderKey("google_drive")
    public fun googleDriveProvider(provider: GoogleDriveCloudProvider): CloudProvider = provider
}

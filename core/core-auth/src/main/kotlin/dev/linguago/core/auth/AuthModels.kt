package dev.linguago.core.auth

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Tokens OAuth del usuario (ADR-011). Se persisten en un DataStore del
 * directorio privado de la app (UID del dispositivo); el payload no contiene
 * datos de progreso, solo credenciales de sesión.
 */
@Serializable
public data class StoredTokens(
    @SerialName("access_token") public val accessToken: String,
    @SerialName("refresh_token") public val refreshToken: String,
    @SerialName("expires_at_millis") public val expiresAtMillis: Long,
    @SerialName("account") public val accountLabel: String? = null,
) {
    public fun isExpiredAt(epochMillis: Long): Boolean = expiresAtMillis <= epochMillis + EXPIRATION_MARGIN_MILLIS
}

/** Colchón antes de expirar para no mandar tokens a punto de vencer. */
private const val EXPIRATION_MARGIN_MILLIS: Long = 60_000L

/** Redirect recibido en el callback (code para canjear o error del IdP). */
public data class AuthRedirect(
    public val code: String? = null,
    public val error: String? = null,
)

/** Datos para abrir el navegador con el flujo interactivo. */
public data class OAuthLaunch(
    /** URL completa (client_id, PKCE, state, redirect). */
    public val url: String,
    /** Verificador PKCE que se consume con el `code` del redirect. */
    public val codeVerifier: String,
)

/** Estado de vinculación por proveedor, observado por la UI (I3). */
public data class AuthState(
    /** Proveedor real OAuth configurado y con/b sin sesión. */
    public val googleConfigured: Boolean = false,
    public val googleLinked: Boolean = false,
    /** Proveedor de laboratorio (compilado solo en debug). */
    public val devEnabled: Boolean = false,
    public val devLinked: Boolean = false,
)

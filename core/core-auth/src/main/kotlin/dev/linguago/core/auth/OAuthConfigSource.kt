package dev.linguago.core.auth

/**
 * Configuración del proveedor de identidad (ADR-011). Se inyecta desde el
 * app module (BuildConfig/gradle property): un APK sin credenciales
 * configuradas simplemente no ofrece el proveedor real. La app completa
 * funciona 100% offline sin login (I3).
 */
public interface OAuthConfigSource {
    /** Client id OAuth público; `null` ⇒ proveedor sin configurar. */
    public val clientId: String?

    /** Esquema del redirect custom; debe coincidir con el intent-filter. */
    public val redirectScheme: String
        get() = "dev.linguago.oauth"

    /** Host del redirect custom. */
    public val redirectHost: String
        get() = "callback"

    /** Alcance solicitado: solo el sandbox privado de la app en Drive. */
    public val scope: String
        get() = "https://www.googleapis.com/auth/drive.appdata openid profile"

    /** Endpoint de autorización (los tests lo redirigen a MockWebServer). */
    public val authorizeUrl: String
        get() = "https://accounts.google.com/o/oauth2/v2/auth"

    /** Endpoint de intercambio/refresh de tokens. */
    public val tokenUrl: String
        get() = "https://oauth2.googleapis.com/token"

    /** Base de la Drive REST API v3. */
    public val driveApiBase: String
        get() = "https://www.googleapis.com"

    /** Nombre del snapshot dentro de `appDataFolder`. */
    public val snapshotFileName: String
        get() = "linguago-snapshot.json"
}

package dev.linguago.core.auth

import dev.linguago.core.common.DispatcherProvider
import kotlinx.coroutines.withContext
import java.security.SecureRandom
import java.util.Base64
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Generador PKCE (RFC 7636). Se instancia por navegador-lanzamiento; el
 * `verifier` vive solo en memoria de la sesión (OAuthSession) y viaja al
 * endpoint de token junto con el `code` del redirect.
 */
@Singleton
public class PkceGenerator
    @Inject
    constructor(
        private val secureRandom: SecureRandom,
        private val dispatchers: DispatcherProvider,
    ) {
        /** Genera (verifier, challenge) con 64 bytes de entropía. */
        public suspend fun generate(): Pair<String, String> =
            withContext(dispatchers.io) {
                val bytes = ByteArray(VERIFIER_BYTES)
                secureRandom.nextBytes(bytes)
                val verifier = base64Url(bytes)
                val challenge =
                    base64Url(
                        java.security.MessageDigest
                            .getInstance("SHA-256")
                            .digest(verifier.toByteArray(Charsets.US_ASCII)),
                    )
                verifier to challenge
            }

        private fun base64Url(bytes: ByteArray): String = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)

        private companion object {
            const val VERIFIER_BYTES = 64
        }
    }

package dev.linguago.core.auth

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Sesión OAuth en curso (ADR-011): el ViewModel arma el launch, la actividad
 * de callback (app module) resuelve el redirect, y el repositorio consume el
 * desenlace. Solo existe una sesión OAuth a la vez.
 */
@Singleton
public class OAuthSession
    @Inject
    constructor() {
        private var deferred: CompletableDeferred<AuthRedirect> = CompletableDeferred()
        private var verifier: String? = null

        /** Inicia una sesión nueva: guarda el verificador y el wait pendiente. */
        public fun start(launch: OAuthLaunch) {
            verifier = launch.codeVerifier
            deferred = CompletableDeferred()
        }

        /** Llamado desde OAuthCallbackActivity. Ignora redirects de sesiones viejas. */
        public fun resolve(redirect: AuthRedirect?) {
            deferred.complete(redirect ?: AuthRedirect(error = "sin_redirect"))
        }

        /**
         * Devuelve el redirect resuelto (máximo de 10 min de navegador
         * abierto) o `null` si no llegó nada usable. El verificador PKCE
         * queda disponible para `consumeVerifier()` al hacer el canje.
         */
        public suspend fun await(): AuthRedirect? {
            val redirect =
                withTimeoutOrNull(SESSION_TIMEOUT_MILLIS) {
                    deferred.await()
                } ?: run {
                    deferred = CompletableDeferred()
                    return null
                }
            return when {
                redirect.error != null || redirect.code == null -> null
                else -> redirect
            }
        }

        /** Verificador PKCE ligado a esta sesión (null si ya se consumió). */
        public fun consumeVerifier(): String? {
            val value = verifier
            verifier = null
            return value
        }

        private companion object {
            const val SESSION_TIMEOUT_MILLIS: Long = 10 * 60 * 1000L
        }
    }

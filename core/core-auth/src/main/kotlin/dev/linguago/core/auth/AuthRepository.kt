package dev.linguago.core.auth

import dev.linguago.core.common.DispatcherProvider
import dev.linguago.core.common.LinguaError
import dev.linguago.core.common.Result
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

/**
 * Repositorio de sesión (ADR-011): orquesta vinculación/desvinculación con
 * el proveedor real (Google Drive vía OAuth/PKCE) y el proveedor de lab
 * (solo debug). Invariante I3: el login es opcional; desvincular nunca
 * borra progreso local (eso lo respeta el merge monótono, ADR-013).
 */
@Singleton
public class AuthRepository
    @Inject
    constructor(
        private val tokenStore: TokenStore,
        private val engine: OAuthEngine,
        private val config: OAuthConfigSource,
        private val session: OAuthSession,
        private val devProvider: DevCloudProvider,
        @Named(DEV_ENABLED_DISPATCH) private val devEnabled: Boolean,
        dispatchers: DispatcherProvider,
    ) {
        private val scope = CoroutineScope(SupervisorJob() + dispatchers.io)

        /** Semáforo que invalida el estado derivado (link/unlink al vuelo). */
        private val invalidations = MutableStateFlow(0L)

        /** Estado de sesión por proveedor, listo para la UI. */
        public val state: StateFlow<AuthState> =
            combine(tokenStore.state, invalidations) { tokens, _ ->
                AuthState(
                    googleConfigured = !config.clientId.isNullOrBlank(),
                    googleLinked = tokens != null,
                    devEnabled = devEnabled,
                    devLinked = devEnabled && devProvider.isLinked(),
                )
            }.stateIn(
                scope = scope,
                started = SharingStarted.Eagerly,
                initialValue = AuthState(),
            )

        /**
         * Flujo interactivo de vinculación (paso 1 de 2): genera la URL de
         * autorización y deja la sesión lista; la UI abre el navegador con
         * `ACTION_VIEW` y, al regresar, OAuthCallbackActivity resuelve el
         * redirect en la misma sesión.
         */
        public suspend fun startGoogleLink(): Result<OAuthLaunch> =
            when {
                config.clientId.isNullOrBlank() -> Result.error(LinguaError.NotConfigured)
                tokenStore.hasTokens() -> Result.error(LinguaError.NotLinked)
                else ->
                    when (val launch = engine.createLaunch()) {
                        is Result.Error -> launch
                        is Result.Success -> {
                            session.start(launch.value)
                            Result.success(launch.value)
                        }
                    }
            }

        /**
         * Paso 2 de 2: espera el redirect del navegador, canjea el `code`
         * por tokens y persiste la sesión (DataStore, ADR-011).
         */
        public suspend fun completeGoogleLink(): Result<Unit> =
            when (val redirect = session.await()) {
                null -> Result.error(LinguaError.NotLinked)
                else ->
                    when {
                        redirect.code.isNullOrBlank() -> Result.error(LinguaError.NotLinked)
                        else ->
                            when (val verifier = session.consumeVerifier()) {
                                null -> Result.error(LinguaError.Unknown)
                                else ->
                                    when (val tokens = engine.exchange(redirect.code, verifier)) {
                                        is Result.Error -> tokens
                                        is Result.Success ->
                                            when (val saved = tokenStore.save(tokens.value)) {
                                                is Result.Error -> saved
                                                is Result.Success -> {
                                                    invalidate()
                                                    Result.success(Unit)
                                                }
                                            }
                                    }
                            }
                    }
            }

        /** Desvincula el proveedor OAuth; borra credenciales, no progreso. */
        public suspend fun unlinkGoogle(): Result<Unit> =
            when (val removed = tokenStore.clear()) {
                is Result.Error -> removed
                is Result.Success -> {
                    invalidate()
                    Result.success(Unit)
                }
            }

        /** Vinculación instantánea del proveedor de laboratorio (debug). */
        public suspend fun linkDevLocal(): Result<Unit> =
            when (devEnabled) {
                false -> Result.error(LinguaError.NotConfigured)
                true -> {
                    devProvider.link(true)
                    invalidate()
                    Result.success(Unit)
                }
            }

        /** Desvinculación instantánea del proveedor de laboratorio (debug). */
        public suspend fun unlinkDevLocal(): Result<Unit> =
            when (devEnabled) {
                false -> Result.error(LinguaError.NotConfigured)
                true -> {
                    devProvider.link(false)
                    invalidate()
                    Result.success(Unit)
                }
            }

        private fun invalidate() {
            invalidations.update { value -> value + 1 }
        }

        private companion object {
            const val DEV_ENABLED_DISPATCH = "dev_cloud_enabled"
        }
    }

package dev.linguago.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.linguago.core.auth.AuthRepository
import dev.linguago.core.auth.AuthState
import dev.linguago.core.common.LinguaError
import dev.linguago.core.common.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Entrada visual de un proveedor de cuenta en AuthScreen. */
public data class AccountEntry(
    public val providerId: String,
    public val title: String,
    public val subtitle: String,
    public val linked: Boolean,
    /** ¿El proveedor está configurado (credenciales de build presentes)? */
    public val configured: Boolean,
)

/** Estado de la pantalla de cuenta (RFC §6, Fase 4). */
public data class AuthUiState(
    public val entries: List<AccountEntry> = emptyList(),
    /** URL del launch OAuth lista para abrir en el navegador (paso 1). */
    public val launchUrl: String? = null,
    public val busy: Boolean = false,
    public val message: String? = null,
)

/**
 * ViewModel de cuenta (ADR-011): vincular Google Drive (OAuth/PKCE en dos
 * pasos) y desvincular. El login es opcional (I3); desvincular nunca borra
 * progreso local (ADR-013).
 */
@HiltViewModel
public class AuthViewModel
    @Inject
    constructor(
        private val authRepository: AuthRepository,
    ) : ViewModel() {
        private val _uiState =
            MutableStateFlow(
                AuthUiState(
                    entries = entriesFor(AuthState()),
                ),
            )
        public val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

        init {
            viewModelScope.launch {
                authRepository.state.collect { state ->
                    _uiState.update { current -> current.withEntries(state) }
                }
            }
        }

        /** Paso 1 de la vinculación Google: pide la URL de autorización. */
        public fun startGoogleLink() {
            runGuarded {
                when (val launch = authRepository.startGoogleLink()) {
                    is Result.Error ->
                        _uiState.update { current -> current.withError(launch.error) }
                    is Result.Success ->
                        _uiState.update { current ->
                            current.copy(launchUrl = launch.value.url)
                        }
                }
            }
        }

        /** La UI abrió el navegador; el callback activa el paso 2. */
        public fun onBrowserOpened() {
            runGuarded {
                val outcome = authRepository.completeGoogleLink()
                _uiState.update { current ->
                    when (outcome) {
                        is Result.Error ->
                            current.withError(outcome.error).copy(launchUrl = null)
                        is Result.Success ->
                            current.copy(
                                launchUrl = null,
                                message = "Cuenta vinculada.",
                            )
                    }
                }
            }
        }

        /** Desvincula Google Drive (no toca el progreso local). */
        public fun unlinkGoogle() {
            runGuarded {
                when (val removed = authRepository.unlinkGoogle()) {
                    is Result.Error -> removed
                    is Result.Success ->
                        _uiState.update { current ->
                            current.copy(message = "Cuenta desvinculada.")
                        }
                }
            }
        }

        /** Vinculación instantánea del proveedor de laboratorio (debug). */
        public fun linkDevLocal() {
            runGuarded {
                authRepository.linkDevLocal()
                _uiState.update { current ->
                    current.copy(message = "Sesión de laboratorio vinculada.")
                }
            }
        }

        /** Desvinculación instantánea del proveedor de laboratorio. */
        public fun unlinkDevLocal() {
            runGuarded {
                authRepository.unlinkDevLocal()
                _uiState.update { current ->
                    current.copy(message = "Sesión de laboratorio desvinculada.")
                }
            }
        }

        /** Descarta el mensaje actual del banner. */
        public fun dismissMessage() {
            _uiState.update { current -> current.copy(message = null) }
        }

        private fun runGuarded(operation: suspend () -> Unit) {
            val state = _uiState.value
            if (state.busy) return
            _uiState.update { current -> current.copy(busy = true, message = null) }
            viewModelScope.launch {
                operation()
                _uiState.update { current -> current.copy(busy = false) }
            }
        }

        private fun AuthUiState.withError(error: LinguaError): AuthUiState =
            copy(
                message =
                    when (error) {
                        LinguaError.NotConfigured ->
                            "Falta configurar el Client ID (docs/DECISIONS.md, ADR-011)."
                        LinguaError.NotLinked -> "No se completó la vinculación."
                        LinguaError.Network -> "Sin conexión con el proveedor de identidad."
                        else -> "No se pudo completar la operación."
                    },
            )

        private fun AuthUiState.withEntries(state: AuthState): AuthUiState = copy(entries = entriesFor(state))
    }

/** Mapa AuthState → tarjetas de proveedores para la UI (sin adivinanza). */
internal fun entriesFor(state: AuthState): List<AccountEntry> =
    buildList {
        add(
            AccountEntry(
                providerId = PROVIDER_GOOGLE,
                title = "Google Drive",
                subtitle =
                    "Copia automática en tu Drive (carpeta privada de la app).",
                linked = state.googleLinked,
                configured = state.googleConfigured,
            ),
        )
        if (state.devEnabled) {
            add(
                AccountEntry(
                    providerId = PROVIDER_DEV,
                    title = "Laboratorio (sin red)",
                    subtitle =
                        "Respaldo de prueba dentro del dispositivo. " +
                            "Útil para validar el flujo sin Google.",
                    linked = state.devLinked,
                    configured = true,
                ),
            )
        }
    }

private const val PROVIDER_GOOGLE = "google_drive"
private const val PROVIDER_DEV = "dev_local"

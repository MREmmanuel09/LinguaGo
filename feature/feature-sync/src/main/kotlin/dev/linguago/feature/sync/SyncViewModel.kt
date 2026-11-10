package dev.linguago.feature.sync

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.linguago.core.auth.AuthRepository
import dev.linguago.core.auth.AuthState
import dev.linguago.core.common.DispatcherProvider
import dev.linguago.core.common.LinguaError
import dev.linguago.core.common.Result
import dev.linguago.core.sync.SyncEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject

/** Estado de la pantalla unificada de respaldo y cuenta (ADR-050). */
data class SyncUiState(
    val providerName: String = "Sin cuenta",
    val linked: Boolean = false,
    val googleConfigured: Boolean = false,
    val googleLinked: Boolean = false,
    val devEnabled: Boolean = false,
    val devLinked: Boolean = false,
    val busy: Boolean = false,
    val message: String? = null,
    /** URL del launch OAuth lista para abrir en el navegador (paso 1). */
    val launchUrl: String? = null,
)

/**
 * ViewModel unificado de respaldo (ADR-050): cuenta opcional de Google Drive,
 * respaldo cifrado en la nube y exportación/importación JSON vía SAF. Ninguna
 * operación bloquea el aprendizaje (I3) y el merge local nunca pierde avance
 * (I4).
 */
@HiltViewModel
class SyncViewModel
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
        private val syncEngine: SyncEngine,
        private val authRepository: AuthRepository,
        private val dispatchers: DispatcherProvider,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(authRepository.state.value.toUiState())
        val uiState: StateFlow<SyncUiState> = _uiState.asStateFlow()

        init {
            viewModelScope.launch {
                authRepository.state.collect { state ->
                    _uiState.update { current -> state.toUiState(current) }
                }
            }
        }

        /** Sube el snapshot del progreso local al proveedor cloud. */
        fun backup() {
            if (!_uiState.value.linked) {
                _uiState.update { it.copy(message = "Vincula una cuenta para respaldar en la nube.") }
                return
            }
            runOperation(successMessage = "Respaldo cifrado completado.") { syncEngine.backup() }
        }

        /** Baja el snapshot remoto y lo restaura con merge monótono (I4). */
        fun restore() {
            if (!_uiState.value.linked) {
                _uiState.update { it.copy(message = "Vincula una cuenta para restaurar desde la nube.") }
                return
            }
            runOperation(successMessage = "Progreso restaurado y fusionado.") { syncEngine.restore() }
        }

        /** Exporta el snapshot v3 a un `.json` elegido por el usuario (SAF). */
        fun exportTo(uri: Uri) {
            runOperation(successMessage = "Copia local exportada.") {
                withContext(dispatchers.io) {
                    try {
                        val payload =
                            when (val encoded = syncEngine.exportSnapshot()) {
                                is Result.Error -> return@withContext encoded
                                is Result.Success -> encoded.value
                            }
                        context.contentResolver.openOutputStream(uri)?.use { stream ->
                            stream.write(payload)
                        } ?: return@withContext Result.error(LinguaError.Unknown)
                        Result.success(Unit)
                    } catch (error: IOException) {
                        Result.error(LinguaError.Unknown)
                    } catch (error: SecurityException) {
                        Result.error(LinguaError.Unknown)
                    }
                }
            }
        }

        /** Importa un `.json` local y lo fusiona sin borrar progreso (I4). */
        fun importFrom(uri: Uri) {
            runOperation(successMessage = "Copia local importada y fusionada.") {
                withContext(dispatchers.io) {
                    try {
                        val payload =
                            context.contentResolver.openInputStream(uri)?.use { stream ->
                                stream.readBytes()
                            } ?: return@withContext Result.error(LinguaError.NotFound)
                        syncEngine.importSnapshot(payload)
                    } catch (error: IOException) {
                        Result.error(LinguaError.Unknown)
                    } catch (error: SecurityException) {
                        Result.error(LinguaError.Unknown)
                    }
                }
            }
        }

        /** Paso 1 de la vinculación Google: solicita la URL de autorización. */
        fun startGoogleLink() {
            runOperation(successMessage = null) {
                when (val launch = authRepository.startGoogleLink()) {
                    is Result.Error -> launch
                    is Result.Success -> {
                        _uiState.update { it.copy(launchUrl = launch.value.url) }
                        Result.success(Unit)
                    }
                }
            }
        }

        /** La UI abrió el navegador; el callback activa el paso 2. */
        fun onBrowserOpened() {
            _uiState.update { it.copy(launchUrl = null) }
            runOperation(successMessage = "Cuenta vinculada.") {
                authRepository.completeGoogleLink()
            }
        }

        /** Desvincula Google Drive (no toca el progreso local). */
        fun unlinkGoogle() {
            runOperation(successMessage = "Cuenta desvinculada.") { authRepository.unlinkGoogle() }
        }

        /** Vinculación instantánea del proveedor de laboratorio (debug). */
        fun linkDevLocal() {
            runOperation(successMessage = "Sesión de laboratorio vinculada.") {
                authRepository.linkDevLocal()
            }
        }

        /** Desvinculación instantánea del proveedor de laboratorio. */
        fun unlinkDevLocal() {
            runOperation(successMessage = "Sesión de laboratorio desvinculada.") {
                authRepository.unlinkDevLocal()
            }
        }

        /** Descarta el mensaje actual del banner. */
        fun dismissMessage() {
            _uiState.update { it.copy(message = null) }
        }

        private fun runOperation(
            successMessage: String?,
            operation: suspend () -> Result<*>,
        ) {
            if (_uiState.value.busy) return
            _uiState.update { it.copy(busy = true, message = null) }
            viewModelScope.launch {
                val result = operation()
                _uiState.update { current ->
                    current.copy(
                        busy = false,
                        message =
                            when (result) {
                                is Result.Success -> successMessage
                                is Result.Error -> result.error.toMessage()
                            },
                    )
                }
            }
        }
    }

private fun AuthState.toUiState(current: SyncUiState = SyncUiState()): SyncUiState {
    val hasCloud = googleLinked || devLinked
    return current.copy(
        providerName =
            when {
                devLinked -> "Laboratorio local"
                googleLinked -> "Google Drive"
                else -> "Sin cuenta"
            },
        linked = hasCloud,
        googleConfigured = googleConfigured,
        googleLinked = googleLinked,
        devEnabled = devEnabled,
        devLinked = devLinked,
    )
}

private fun LinguaError.toMessage(): String =
    when (this) {
        LinguaError.NotConfigured -> "Google Drive no está configurado en esta compilación."
        LinguaError.NotLinked -> "No hay una cuenta o copia disponible."
        LinguaError.NotFound -> "No se encontró la copia solicitada."
        LinguaError.Network -> "Sin conexión con el proveedor de respaldo."
        LinguaError.InvalidContent -> "El archivo no es un respaldo válido de LinguaGo."
        else -> "No se pudo completar la operación."
    }

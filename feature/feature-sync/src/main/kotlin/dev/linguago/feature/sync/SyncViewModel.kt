package dev.linguago.feature.sync

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.linguago.core.common.LinguaError
import dev.linguago.core.common.Result
import dev.linguago.core.sync.CloudProvider
import dev.linguago.core.sync.SyncEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Estado de la pantalla de respaldo (RFC §6, ADR-013). */
data class SyncUiState(
    val providerId: String,
    val linked: Boolean,
    val busy: Boolean = false,
    val message: String? = null,
)

/**
 * ViewModel de respaldo (Fase 3): el motor es real, pero con el proveedor
 * placeholder (Fase 4 conecta Drive/OneDrive, ADR-011) queda deshabilitado.
 * Invariante I3: el respaldo es opcional, nunca bloquea el aprendizaje.
 */
@HiltViewModel
class SyncViewModel
    @Inject
    constructor(
        private val syncEngine: SyncEngine,
        private val cloudProvider: CloudProvider,
    ) : ViewModel() {
        private val _uiState =
            MutableStateFlow(
                SyncUiState(
                    providerId = cloudProvider.id,
                    linked = cloudProvider.isLinked(),
                ),
            )
        val uiState: StateFlow<SyncUiState> = _uiState.asStateFlow()

        /** Re-lee el proveedor activo (link/unlink cambian al vuelo). */
        fun refresh() {
            _uiState.update { current ->
                current.copy(
                    providerId = cloudProvider.id,
                    linked = cloudProvider.isLinked(),
                )
            }
        }

        /** Sube el snapshot del progreso local al proveedor cloud. */
        fun backup() {
            runSync(successMessage = "Respaldo completado.") { syncEngine.backup() }
        }

        /** Baja el snapshot remoto y lo restaura con merge monótono (I4). */
        fun restore() {
            runSync(successMessage = "Progreso restaurado.") { syncEngine.restore() }
        }

        private fun runSync(
            successMessage: String,
            operation: suspend () -> Result<*>,
        ) {
            val state = _uiState.value
            if (state.busy || !state.linked) return
            _uiState.update { it.copy(busy = true, message = null) }
            viewModelScope.launch {
                val result = operation()
                _uiState.update { current ->
                    current.copy(
                        busy = false,
                        message =
                            when (result) {
                                is Result.Success -> successMessage
                                is Result.Error ->
                                    when (result.error) {
                                        LinguaError.NotLinked -> "Sin cuenta cloud vinculada (Fase 4)."
                                        else -> "No se pudo completar el respaldo."
                                    }
                            },
                    )
                }
            }
        }
    }

package dev.linguago.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.linguago.core.domain.model.ThemeMode
import dev.linguago.core.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Estado de ajustes: tema elegido y curso seleccionado (null si aún no hay). */
data class SettingsUiState(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val selectedCourseId: String? = null,
)

/**
 * Ajustes observados y mutables (Fase 1): tema claro/oscuro y curso activo
 * persisten en DataStore vía SettingsRepository.
 */
@HiltViewModel
class SettingsViewModel
    @Inject
    constructor(
        private val settingsRepository: SettingsRepository,
    ) : ViewModel() {
        val uiState: StateFlow<SettingsUiState> =
            combine(
                settingsRepository.themeMode,
                settingsRepository.selectedCourseId,
            ) { themeMode, courseId ->
                SettingsUiState(themeMode = themeMode, selectedCourseId = courseId)
            }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsUiState())

        /** Cambia el tema preferido y lo persiste. */
        fun setTheme(mode: ThemeMode) {
            viewModelScope.launch {
                settingsRepository.updateThemeMode(mode)
            }
        }

        /** Deselecciona el curso para reabrir onboarding ("Cambiar de curso"). */
        fun resetCourse(onCourseReset: () -> Unit) {
            viewModelScope.launch {
                settingsRepository.updateSelectedCourse(null)
                onCourseReset()
            }
        }
    }

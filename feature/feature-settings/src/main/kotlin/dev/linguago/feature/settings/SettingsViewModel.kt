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

/** Estado de ajustes: tema, curso y preferencias sensoriales (ADR-027). */
data class SettingsUiState(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val selectedCourseId: String? = null,
    val soundEffectsEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true,
)

/**
 * Ajustes observados y mutables (Fase 1): tema, curso activo y feedback
 * sensorial persisten en DataStore vía SettingsRepository.
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
                settingsRepository.soundEffectsEnabled,
                settingsRepository.hapticsEnabled,
            ) { themeMode, courseId, soundEnabled, hapticsEnabled ->
                SettingsUiState(
                    themeMode = themeMode,
                    selectedCourseId = courseId,
                    soundEffectsEnabled = soundEnabled,
                    hapticsEnabled = hapticsEnabled,
                )
            }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsUiState())

        /** Cambia el tema preferido y lo persiste. */
        fun setTheme(mode: ThemeMode) {
            viewModelScope.launch {
                settingsRepository.updateThemeMode(mode)
            }
        }

        /** Activa o desactiva los SFX (ADR-027). */
        fun setSoundEffectsEnabled(enabled: Boolean) {
            viewModelScope.launch {
                settingsRepository.updateSoundEffectsEnabled(enabled)
            }
        }

        /** Activa o desactiva la háptica (ADR-027). */
        fun setHapticsEnabled(enabled: Boolean) {
            viewModelScope.launch {
                settingsRepository.updateHapticsEnabled(enabled)
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

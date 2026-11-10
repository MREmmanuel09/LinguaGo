package dev.linguago.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.linguago.core.audio.VoiceStorageManager
import dev.linguago.core.common.Result
import dev.linguago.core.domain.model.ThemeMode
import dev.linguago.core.domain.repository.SettingsRepository
import dev.linguago.core.domain.usecase.GetAvailableCoursesUseCase
import dev.linguago.core.ui.components.CourseSwitchOption
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Estado del gestor de almacenamiento de voz (ADR-051). */
data class VoiceCacheUiState(
    val bytes: Long = 0,
    val clearing: Boolean = false,
    val message: String? = null,
)

/** Estado de ajustes: tema, curso y preferencias sensoriales (ADR-027). */
data class SettingsUiState(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val selectedCourseId: String? = null,
    val soundEffectsEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true,
    val courses: List<CourseSwitchOption> = emptyList(),
    val voiceCacheBytes: Long = 0,
    val clearingVoiceCache: Boolean = false,
    val voiceMessage: String? = null,
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
        private val getAvailableCoursesUseCase: GetAvailableCoursesUseCase,
        private val voiceStorageManager: VoiceStorageManager,
    ) : ViewModel() {
        private val courses = MutableStateFlow<List<CourseSwitchOption>>(emptyList())
        private val voiceCache = MutableStateFlow(VoiceCacheUiState())
        private val courseAndVoice = combine(courses, voiceCache) { available, voice -> available to voice }

        val uiState: StateFlow<SettingsUiState> =
            combine(
                settingsRepository.themeMode,
                settingsRepository.selectedCourseId,
                settingsRepository.soundEffectsEnabled,
                settingsRepository.hapticsEnabled,
                courseAndVoice,
            ) { themeMode, courseId, soundEnabled, hapticsEnabled, (available, voice) ->
                SettingsUiState(
                    themeMode = themeMode,
                    selectedCourseId = courseId,
                    soundEffectsEnabled = soundEnabled,
                    hapticsEnabled = hapticsEnabled,
                    courses = available,
                    voiceCacheBytes = voice.bytes,
                    clearingVoiceCache = voice.clearing,
                    voiceMessage = voice.message,
                )
            }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsUiState())

        init {
            viewModelScope.launch {
                when (val result = getAvailableCoursesUseCase()) {
                    is Result.Success ->
                        courses.value =
                            result.value.map { summary ->
                                CourseSwitchOption(
                                    id = summary.id.value,
                                    title = summary.title,
                                    subtitle = "${languageName(summary.fromLanguage)} → ${languageName(summary.toLanguage)}",
                                )
                            }
                    is Result.Error -> Unit
                }
            }
            refreshVoiceCache()
        }

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

        /** Cambia el curso activo desde el selector rápido (ADR-048). */
        fun selectCourse(courseId: String) {
            viewModelScope.launch {
                settingsRepository.updateSelectedCourse(courseId)
            }
        }

        /** Relee el espacio ocupado por voces extraídas (ADR-051). */
        fun refreshVoiceCache() {
            viewModelScope.launch {
                val info = voiceStorageManager.usage()
                voiceCache.update { current -> current.copy(bytes = info.totalBytes) }
            }
        }

        /** Libera las voces extraídas y conserva los assets empaquetados. */
        fun clearVoiceCache() {
            if (voiceCache.value.clearing) return
            voiceCache.update { current -> current.copy(clearing = true, message = null) }
            viewModelScope.launch {
                val info = voiceStorageManager.clearExtractedCache()
                voiceCache.update { current ->
                    current.copy(
                        bytes = info.totalBytes,
                        clearing = false,
                        message = "Espacio de voces liberado.",
                    )
                }
            }
        }

        /** Descarta el aviso del gestor de voz. */
        fun dismissVoiceMessage() {
            voiceCache.update { current -> current.copy(message = null) }
        }

        private fun languageName(language: dev.linguago.core.domain.model.Language): String =
            when (language.isoCode) {
                "es" -> "Español"
                "en" -> "Inglés"
                "pt" -> "Portugués"
                "fr" -> "Francés"
                "ja" -> "Japonés"
                else -> language.isoCode
            }
    }

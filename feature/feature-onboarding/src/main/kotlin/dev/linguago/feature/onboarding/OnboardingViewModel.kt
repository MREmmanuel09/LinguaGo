package dev.linguago.feature.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.linguago.core.common.Result
import dev.linguago.core.domain.model.CourseSummary
import dev.linguago.core.domain.model.Language
import dev.linguago.core.domain.repository.SettingsRepository
import dev.linguago.core.domain.usecase.GetAvailableCoursesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Datos de un curso para la lista de selección visual. */
data class OnboardingCourseTile(
    val id: String,
    val title: String,
    val subtitle: String,
)

/** Estado de la pantalla de selección de curso. */
data class OnboardingUiState(
    val tiles: List<OnboardingCourseTile> = emptyList(),
    val selectedId: String? = null,
    val loading: Boolean = true,
    val saving: Boolean = false,
)

/**
 * Bienvenida + selección de curso. La lista sale del contenido empaquetado
 * (I1) y la elección persiste en DataStore antes de entrar a la home.
 */
@HiltViewModel
class OnboardingViewModel
    @Inject
    constructor(
        private val getAvailableCoursesUseCase: GetAvailableCoursesUseCase,
        private val settingsRepository: SettingsRepository,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(OnboardingUiState())
        val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

        init {
            loadCourses()
        }

        fun selectCourse(courseId: String) {
            _uiState.update { it.copy(selectedId = courseId) }
        }

        fun confirmSelection(onReady: (courseId: String) -> Unit) {
            val selectedId = _uiState.value.selectedId ?: return
            if (_uiState.value.saving) return
            _uiState.update { it.copy(saving = true) }
            viewModelScope.launch {
                settingsRepository.updateSelectedCourse(selectedId)
                onReady(selectedId)
            }
        }

        private fun loadCourses() {
            viewModelScope.launch {
                when (val result = getAvailableCoursesUseCase()) {
                    is Result.Success ->
                        _uiState.update { state ->
                            state.copy(
                                tiles = result.value.map { it.toTile() },
                                selectedId =
                                    state.selectedId ?: result.value
                                        .firstOrNull()
                                        ?.id
                                        ?.value,
                                loading = false,
                            )
                        }
                    is Result.Error -> _uiState.update { it.copy(loading = false) }
                }
            }
        }
    }

private fun CourseSummary.toTile(): OnboardingCourseTile =
    OnboardingCourseTile(
        id = id.value,
        title = fromLanguage.toDisplayName(),
        subtitle = "${fromLanguage.toDisplayName()} → ${toLanguage.toDisplayName()}",
    )

private fun Language.toDisplayName(): String = LANGUAGE_NAMES[isoCode] ?: isoCode

private val LANGUAGE_NAMES =
    mapOf(
        "es" to "Español",
        "en" to "Inglés",
        "pt" to "Portugués",
        "fr" to "Francés",
        "ja" to "Japonés",
    )

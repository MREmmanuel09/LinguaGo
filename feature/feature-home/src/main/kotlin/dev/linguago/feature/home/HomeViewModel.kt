package dev.linguago.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.linguago.core.common.Result
import dev.linguago.core.domain.model.Course
import dev.linguago.core.domain.model.CourseId
import dev.linguago.core.domain.model.Language
import dev.linguago.core.domain.model.LessonProgress
import dev.linguago.core.domain.repository.ProgressRepository
import dev.linguago.core.domain.repository.ReviewRepository
import dev.linguago.core.domain.repository.SettingsRepository
import dev.linguago.core.domain.usecase.LoadCourseUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Tarjeta visual de una unidad con su avance (x de y lecciones). */
data class UnitTile(
    val unitId: String,
    val title: String,
    val completed: Int,
    val total: Int,
    val nextLessonId: String?,
)

/** Estado observado de la ruta de aprendizaje. */
data class HomeUiState(
    val loading: Boolean = true,
    val hasCourse: Boolean = false,
    val courseId: String? = null,
    val streakDays: Int = 0,
    val totalXp: Int = 0,
    val dueReviews: Int = 0,
    val courseTitle: String? = null,
    val units: List<UnitTile> = emptyList(),
)

/**
 * Home reactiva (RFC §2.2): observa racha y progreso (Room) y carga el
 * curso seleccionado (assets, I1). Se reconstruye sola cuando el usuario
 * cambia de curso o completa lecciones.
 */
@HiltViewModel
class HomeViewModel
    @Inject
    constructor(
        private val loadCourseUseCase: LoadCourseUseCase,
        private val progressRepository: ProgressRepository,
        private val settingsRepository: SettingsRepository,
        private val reviewRepository: ReviewRepository,
    ) : ViewModel() {
        private val course = MutableStateFlow<Course?>(null)
        private val loading = MutableStateFlow(true)
        private val dueCount = MutableStateFlow(0)
        private var dueJob: Job? = null

        val uiState: StateFlow<HomeUiState> =
            combine(
                progressRepository.observeStreak(),
                progressRepository.observeLessonProgress(),
                loading,
                course,
                dueCount,
            ) { streak, lessonProgress, isLoading, courseValue, dueReviews ->
                HomeUiState(
                    loading = isLoading,
                    hasCourse = courseValue != null,
                    courseId = courseValue?.id?.value,
                    streakDays = streak.current,
                    totalXp = streak.totalXp,
                    dueReviews = dueReviews,
                    courseTitle = courseValue?.let { title(it) },
                    units = courseValue?.let { value -> buildTiles(value, lessonProgress) } ?: emptyList(),
                )
            }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeUiState())

        init {
            viewModelScope.launch {
                settingsRepository.selectedCourseId.collect { courseId ->
                    if (courseId != null) {
                        loadCourse(courseId)
                    } else {
                        course.value = null
                        loading.value = false
                    }
                }
            }
            viewModelScope.launch {
                settingsRepository.selectedCourseId.collect { courseId -> observeReviews(courseId) }
            }
        }

        private fun observeReviews(courseId: String?) {
            dueJob?.cancel()
            if (courseId == null) {
                dueCount.value = 0
                return
            }
            dueJob =
                viewModelScope.launch {
                    reviewRepository.observeDueCount(CourseId(courseId)).collect { due ->
                        dueCount.value = due
                    }
                }
        }

        private suspend fun loadCourse(courseId: String) {
            loading.value = true
            course.value =
                when (val result = loadCourseUseCase(CourseId(courseId))) {
                    is Result.Success -> result.value
                    is Result.Error -> null
                }
            loading.value = false
        }
    }

private fun title(course: Course): String = "${languageName(course.fromLanguage)} → ${languageName(course.toLanguage)}"

private fun languageName(language: Language): String = LANGUAGE_NAMES[language.isoCode] ?: language.isoCode

/** Avance por unidad sobre el conjunto de lecciones completadas. */
private fun buildTiles(
    course: Course,
    lessonProgress: List<LessonProgress>,
): List<UnitTile> {
    val completedIds =
        lessonProgress
            .filter { progress -> progress.completed }
            .map { progress -> progress.lessonId.value }
            .toSet()
    return course.levels
        .flatMap { it.units }
        .map { unit ->
            val lessons = unit.lessons
            val completed = lessons.count { lesson -> lesson.id.value in completedIds }
            val nextLessonId =
                (lessons.firstOrNull { lesson -> lesson.id.value !in completedIds } ?: lessons.firstOrNull())
                    ?.id
                    ?.value
            UnitTile(
                unitId = unit.id.value,
                title = unit.title,
                completed = completed,
                total = lessons.size,
                nextLessonId = nextLessonId,
            )
        }
}

private val LANGUAGE_NAMES =
    mapOf(
        "es" to "Español",
        "en" to "Inglés",
        "pt" to "Portugués",
        "fr" to "Francés",
        "ja" to "Japonés",
    )

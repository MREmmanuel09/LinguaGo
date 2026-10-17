package dev.linguago.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.linguago.core.common.Result
import dev.linguago.core.domain.model.Course
import dev.linguago.core.domain.model.CourseId
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

/** Estado del perfil: racha, XP, lecciones y repaso espaciado. */
public data class ProfileUiState(
    val streakDays: Int = 0,
    val totalXp: Int = 0,
    val lessonsCompleted: Int = 0,
    val lessonsTotal: Int = 0,
    val longestStreak: Int = 0,
    val reviewCards: Int = 0,
    val reviewDue: Int = 0,
)

/**
 * Perfil observado (RFC §2.2): vive sobre ProgressRepository y
 * ReviewRepository (Room), por lo que se actualiza solo al completar
 * lecciones, sumar XP o repasar tarjetas.
 */
@HiltViewModel
public class ProfileViewModel
    @Inject
    constructor(
        private val loadCourseUseCase: LoadCourseUseCase,
        progressRepository: ProgressRepository,
        private val reviewRepository: ReviewRepository,
        private val settingsRepository: SettingsRepository,
    ) : ViewModel() {
        private val course = MutableStateFlow<Course?>(null)
        private val reviewCards = MutableStateFlow(0)
        private val reviewDue = MutableStateFlow(0)
        private var statsJob: Job? = null

        public val uiState: StateFlow<ProfileUiState> =
            combine(
                progressRepository.observeStreak(),
                progressRepository.observeLessonProgress(),
                course,
                reviewCards,
                reviewDue,
            ) { streak, lessonProgress, courseValue, cards, due ->
                ProfileUiState(
                    streakDays = streak.current,
                    longestStreak = streak.longest,
                    totalXp = streak.totalXp,
                    lessonsCompleted = lessonProgress.count { progress -> progress.completed },
                    lessonsTotal =
                        courseValue
                            ?.levels
                            ?.flatMap { level -> level.units }
                            ?.sumOf { unit -> unit.lessons.size } ?: 0,
                    reviewCards = cards,
                    reviewDue = due,
                )
            }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ProfileUiState())

        init {
            viewModelScope.launch {
                settingsRepository.selectedCourseId.collect { courseId -> observeCourse(courseId) }
            }
        }

        private fun observeCourse(courseId: String?) {
            statsJob?.cancel()
            if (courseId == null) {
                course.value = null
                reviewCards.value = 0
                reviewDue.value = 0
                return
            }
            viewModelScope.launch {
                when (val result = loadCourseUseCase(CourseId(courseId))) {
                    is Result.Success -> course.value = result.value
                    is Result.Error -> course.value = null
                }
            }
            statsJob =
                viewModelScope.launch {
                    launch {
                        reviewRepository.observeTotalCount(CourseId(courseId)).collect { total ->
                            reviewCards.value = total
                        }
                    }
                    launch {
                        reviewRepository.observeDueCount(CourseId(courseId)).collect { due ->
                            reviewDue.value = due
                        }
                    }
                }
        }
    }

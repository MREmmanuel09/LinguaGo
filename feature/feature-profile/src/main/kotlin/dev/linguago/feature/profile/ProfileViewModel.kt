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
import java.time.LocalDate
import javax.inject.Inject

/** Estado del perfil: racha, XP, lecciones, repaso y métricas reales. */
public data class ProfileUiState(
    val streakDays: Int = 0,
    val totalXp: Int = 0,
    val lessonsCompleted: Int = 0,
    val lessonsTotal: Int = 0,
    val longestStreak: Int = 0,
    val reviewCards: Int = 0,
    val reviewDue: Int = 0,
    /** Intentos de producción oral libre del curso activo (ADR-050). */
    val speakingAttempts: Int = 0,
    /** Porcentaje de intentos orales que superaron el grader. */
    val speakingPassRatePercent: Int = 0,
    /** Mejor fluidez observada en palabras por minuto. */
    val bestSpeakingWpm: Int = 0,
    /** Media de la mejor precisión de primer intento (0..100). */
    val averageScorePercent: Int = 0,
    /** Actividad de los últimos 28 días, ordenada de antiguo a hoy. */
    val activityDays: List<ActivityDayUi> = emptyList(),
    /** Avance por etapa CEFR A1–C2 del curso activo (ADR-051). */
    val cefrProgress: List<CefrProgressUi> = emptyList(),
    /** Nivel visual derivado del XP (ADR-035). */
    val level: Int = 1,
    /** Avance 0..1 hacia el siguiente nivel visual (ADR-035). */
    val xpIntoLevel: Float = 0f,
)

/**
 * Perfil observado (RFC §2.2): vive sobre ProgressRepository y
 * ReviewRepository (Room), por lo que se actualiza solo al completar
 * lecciones, sumar XP, repasar tarjetas o registrar voz.
 */
@HiltViewModel
public class ProfileViewModel
    @Inject
    constructor(
        private val loadCourseUseCase: LoadCourseUseCase,
        private val progressRepository: ProgressRepository,
        private val reviewRepository: ReviewRepository,
        private val settingsRepository: SettingsRepository,
    ) : ViewModel() {
        private val course = MutableStateFlow<Course?>(null)
        private val reviewCards = MutableStateFlow(0)
        private val reviewDue = MutableStateFlow(0)
        private var statsJob: Job? = null

        private val courseAndActivity =
            combine(
                course,
                progressRepository.observeDailyActivity(),
            ) { courseValue, activity -> courseValue to activity }

        public val uiState: StateFlow<ProfileUiState> =
            combine(
                progressRepository.observeStreak(),
                progressRepository.observeLessonProgress(),
                courseAndActivity,
                reviewCards,
                reviewDue,
            ) { streak, lessonProgress, (courseValue, activity), cards, due ->
                val activeLessonIds =
                    courseValue
                        ?.levels
                        ?.flatMap { level -> level.units }
                        ?.flatMap { unit -> unit.lessons }
                        ?.map { lesson -> lesson.id.value }
                        ?.toSet()
                        .orEmpty()
                val courseProgress =
                    lessonProgress.filter { progress ->
                        progress.lessonId.value in activeLessonIds
                    }
                val oral = ProfileStats.oralMetrics(courseProgress)
                ProfileUiState(
                    streakDays = streak.current,
                    longestStreak = streak.longest,
                    totalXp = streak.totalXp,
                    lessonsCompleted = courseProgress.count { it.completed },
                    lessonsTotal = activeLessonIds.size,
                    reviewCards = cards,
                    reviewDue = due,
                    speakingAttempts = oral.attempts,
                    speakingPassRatePercent = oral.passRatePercent,
                    bestSpeakingWpm = oral.bestWpm,
                    averageScorePercent = ProfileStats.averageBestScore(courseProgress),
                    activityDays = ProfileStats.activityWindow(activity, LocalDate.now()),
                    cefrProgress = ProfileStats.cefrProgress(courseValue, lessonProgress),
                    level = ProfileStats.levelFor(streak.totalXp),
                    xpIntoLevel = ProfileStats.xpIntoLevelFor(streak.totalXp),
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

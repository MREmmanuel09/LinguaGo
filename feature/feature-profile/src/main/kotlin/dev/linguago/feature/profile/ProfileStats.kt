package dev.linguago.feature.profile

import dev.linguago.core.domain.model.CefrLevel
import dev.linguago.core.domain.model.Course
import dev.linguago.core.domain.model.DailyActivity
import dev.linguago.core.domain.model.LessonProgress
import java.time.LocalDate
import kotlin.math.roundToInt

/** Cálculos de nivel visual del perfil (ADR-035); no se persisten. */
internal object ProfileStats {
    const val XP_PER_LEVEL: Int = 100
    const val ACTIVITY_WINDOW_DAYS: Int = 28

    /** Nivel visual derivado del XP total (mismo criterio que el camino). */
    fun levelFor(totalXp: Int): Int = totalXp.coerceAtLeast(0) / XP_PER_LEVEL + 1

    /** Avance (0..1) hacia el siguiente nivel visual. */
    fun xpIntoLevelFor(totalXp: Int): Float {
        val safe = totalXp.coerceAtLeast(0)
        return (safe % XP_PER_LEVEL) / XP_PER_LEVEL.toFloat()
    }

    /** Métricas de producción oral del curso activo (ADR-050). */
    fun oralMetrics(progress: List<LessonProgress>): OralMetrics {
        val attempts = progress.sumOf { it.speakingAttempts }
        val passed = progress.sumOf { it.speakingPassedAttempts }
        val bestWpm = progress.maxOfOrNull { it.speakingBestWpm } ?: 0f
        return OralMetrics(
            attempts = attempts,
            passed = passed,
            passRatePercent = if (attempts > 0) (passed * 100.0 / attempts).roundToInt() else 0,
            bestWpm = bestWpm.roundToInt(),
        )
    }

    /** Media de la mejor precisión por lección completada (0..100). */
    fun averageBestScore(progress: List<LessonProgress>): Int {
        val scores = progress.filter { it.completed && it.bestScorePercent > 0 }.map { it.bestScorePercent }
        return if (scores.isEmpty()) 0 else scores.average().roundToInt()
    }

    /** Progreso real por etapa CEFR del curso activo, incluidas las vacías. */
    fun cefrProgress(
        course: Course?,
        progress: List<LessonProgress>,
    ): List<CefrProgressUi> {
        val completedIds = progress.filter { it.completed }.map { it.lessonId.value }.toSet()
        val lessons =
            course
                ?.levels
                ?.flatMap { level -> level.units }
                .orEmpty()
                .flatMap { unit -> unit.lessons }
        return CefrLevel.entries.map { cefr ->
            val stageLessons = lessons.filter { lesson -> lesson.cefrLevel == cefr }
            CefrProgressUi(
                level = cefr,
                completed = stageLessons.count { lesson -> lesson.id.value in completedIds },
                total = stageLessons.size,
            )
        }
    }

    /** Ventana de actividad diaria terminada en [today], con días vacíos. */
    fun activityWindow(
        activity: List<DailyActivity>,
        today: LocalDate,
        days: Int = ACTIVITY_WINDOW_DAYS,
    ): List<ActivityDayUi> {
        val safeDays = days.coerceAtLeast(1)
        val byDay = activity.associateBy { it.epochDay }
        val todayEpoch = today.toEpochDay()
        return (safeDays - 1 downTo 0).map { offset ->
            val epochDay = todayEpoch - offset
            ActivityDayUi(
                epochDay = epochDay,
                active = byDay[epochDay]?.isActive == true,
                isToday = offset == 0,
            )
        }
    }
}

/** Métricas agregadas de voz del curso activo. */
internal data class OralMetrics(
    val attempts: Int,
    val passed: Int,
    val passRatePercent: Int,
    val bestWpm: Int,
)

/** Celda del calendario local de 28 días. */
public data class ActivityDayUi(
    public val epochDay: Long,
    public val active: Boolean,
    public val isToday: Boolean,
)

/** Progreso de una etapa CEFR A1–C2 del curso activo. */
public data class CefrProgressUi(
    public val level: CefrLevel,
    public val completed: Int,
    public val total: Int,
)

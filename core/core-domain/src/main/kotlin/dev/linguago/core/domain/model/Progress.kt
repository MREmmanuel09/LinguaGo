package dev.linguago.core.domain.model

import java.time.Instant
import java.time.LocalDate

/**
 * Estado de progreso del usuario (RFC §3.1 y ADR-005). Todo estado mutable
 * y persistible vive bajo este paquete; el contenido (Course…) es inmutable.
 */
public data class StreakState(
    val current: Int,
    val longest: Int,
    val lastDate: LocalDate,
    val totalXp: Int,
) {
    public companion object {
        /** Estado semilla de un usuario nuevo: sin racha, sin XP. */
        public fun zero(): StreakState =
            StreakState(
                current = 0,
                longest = 0,
                lastDate = LocalDate.ofEpochDay(0),
                totalXp = 0,
            )
    }
}

public data class LessonProgress(
    val lessonId: LessonId,
    val completed: Boolean,
    val xp: Int,
    val attempts: Int,
    /** Intentos de producción oral libre registrados en la lección. */
    val speakingAttempts: Int = 0,
    /** Intentos orales que superaron el grader heurístico. */
    val speakingPassedAttempts: Int = 0,
    /** Mejor fluidez observada (palabras por minuto). */
    val speakingBestWpm: Float = 0f,
    /** Última puntuación heurística de producción oral (0..1). */
    val speakingLastScore: Float = 0f,
    /** Mejor precisión de primer intento en la lección (0..100). */
    val bestScorePercent: Int = 0,
    /** Precisión de primer intento de la última finalización (0..100). */
    val lastScorePercent: Int = 0,
)

/** Actividad agregada de un día local (Room v5, ADR-050). */
public data class DailyActivity(
    val epochDay: Long,
    val xpEarned: Int,
    val lessonsCompleted: Int,
    val reviewsCompleted: Int,
) {
    /** `true` si hubo cualquier práctica ese día. */
    public val isActive: Boolean
        get() = xpEarned > 0 || lessonsCompleted > 0 || reviewsCompleted > 0
}

/**
 * XP por respuesta correcta acumulada en la sesión. Se persiste una sola vez
 * al completar la lección (ADR-025); nunca se escribe por respuesta.
 */
public const val XP_PER_CORRECT_ANSWER: Int = 10

/** Bono único al abrir el cofre de una unidad completada (ADR-048). */
public const val XP_PER_CHEST: Int = 20

/**
 * Estado FSRS de una tarjeta de vocabulario (ADR-016, Fase 5).
 * `stability` = días para decaer a la retención objetivo; `difficulty` =
 * 1..10. La tarjeta se identifica por la pareja (courseId, word).
 */
public data class WordProgress(
    val word: String,
    val translation: String,
    val courseId: String,
    val reviewCount: Int,
    val lastReview: Instant,
    val nextReview: Instant,
    val stability: Float,
    val difficulty: Float,
    /** Nota cultural del modismo, persistida para el reverso (ADR-050). */
    val culturalNote: String? = null,
)

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
)

/**
 * XP por respuesta correcta acumulada en la sesión. Se persiste una sola vez
 * al completar la lección (ADR-025); nunca se escribe por respuesta.
 */
public const val XP_PER_CORRECT_ANSWER: Int = 10

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
)

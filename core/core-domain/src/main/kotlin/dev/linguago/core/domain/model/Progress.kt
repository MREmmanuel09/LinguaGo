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
)

public data class LessonProgress(
    val lessonId: LessonId,
    val completed: Boolean,
    val xp: Int,
    val attempts: Int,
)

public data class WordProgress(
    val word: String,
    val reviewCount: Int,
    val nextReview: Instant,
    val easeFactor: Float,
)

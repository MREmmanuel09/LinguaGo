package dev.linguago.core.domain.repository

import dev.linguago.core.common.Result
import dev.linguago.core.domain.logic.ReviewRating
import dev.linguago.core.domain.model.Course
import dev.linguago.core.domain.model.CourseId
import dev.linguago.core.domain.model.Lesson
import dev.linguago.core.domain.model.WordProgress
import kotlinx.coroutines.flow.Flow
import java.time.Instant

/**
 * Contrato del repaso espaciado (ADR-016, Fase 5): recolecta vocabulario al
 * completar lecciones, expone las tarjetas vencidas y califica con FSRS.
 * Nunca borra tarjetas ni historial (I4).
 */
public interface ReviewRepository {
    /** Registra el vocabulario de una lección completada; devuelve cuántas nuevas. */
    public suspend fun collectFromLesson(
        course: Course,
        lesson: Lesson,
        now: Instant = Instant.now(),
    ): Result<Int>

    /** Tarjetas vencidas del curso activo (reactivo, para Home). */
    public fun observeDueCount(courseId: CourseId): Flow<Int>

    /** Cola de repaso: tarjetas vencidas ordenadas por vencimiento. */
    public suspend fun due(
        courseId: CourseId,
        limit: Int,
    ): Result<List<WordProgress>>

    /** Califica una tarjeta y persiste su nuevo estado FSRS. */
    public suspend fun grade(
        courseId: CourseId,
        word: String,
        rating: ReviewRating,
        now: Instant = Instant.now(),
    ): Result<WordProgress>
}

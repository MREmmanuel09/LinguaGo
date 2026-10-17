package dev.linguago.core.domain.repository

import dev.linguago.core.common.Result
import dev.linguago.core.domain.model.Course
import dev.linguago.core.domain.model.CourseId
import dev.linguago.core.domain.model.LessonId
import dev.linguago.core.domain.model.LessonProgress
import dev.linguago.core.domain.model.StreakState
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/**
 * Contratos de acceso a datos (I4: progreso intocable, ADR-005). Las
 * implementaciones viven en core-data (Room/DataStore) y core-content
 * (curso), el dominio solo describe operaciones en lenguaje de negocio.
 */
public interface CourseRepository {
    /** Carga completa de un curso desde los assets empaquetados. */
    public suspend fun loadCourse(courseId: CourseId): Result<Course>
}

public interface ProgressRepository {
    /** Observa el estado de racha como flujo reactivo (patrón Observer). */
    public fun observeStreak(): Flow<StreakState>

    /** Aplica la regla de racha para el día indicado y devuelve la racha resultante. */
    public suspend fun applyDailyActivity(today: LocalDate): Result<StreakState>

    /** Añade XP al total del usuario. */
    public suspend fun addXp(amount: Int): Result<StreakState>

    /** Persiste la finalización de una lección con la XP conseguida. */
    public suspend fun markLessonCompleted(
        lessonId: LessonId,
        xp: Int,
    ): Result<Unit>

    /** Observa el progreso de las lecciones completadas hasta hoy. */
    public fun observeLessonProgress(): Flow<List<LessonProgress>>
}

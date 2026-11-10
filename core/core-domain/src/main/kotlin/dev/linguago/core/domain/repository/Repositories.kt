package dev.linguago.core.domain.repository

import dev.linguago.core.common.Result
import dev.linguago.core.domain.model.Course
import dev.linguago.core.domain.model.CourseId
import dev.linguago.core.domain.model.DailyActivity
import dev.linguago.core.domain.model.LessonId
import dev.linguago.core.domain.model.LessonProgress
import dev.linguago.core.domain.model.StreakState
import dev.linguago.core.domain.model.SyncSnapshot
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

    /** Persiste la finalización de una lección con la XP y la nota conseguidas. */
    public suspend fun markLessonCompleted(
        lessonId: LessonId,
        xp: Int,
        scorePercent: Int = 0,
    ): Result<Unit>

    /** Observa el progreso de las lecciones completadas hasta hoy. */
    public fun observeLessonProgress(): Flow<List<LessonProgress>>

    /** Observa la actividad diaria de los últimos [days] días (Room v5). */
    public fun observeDailyActivity(days: Int = 28): Flow<List<DailyActivity>>

    /** Suma actividad local del día sin tocar la racha ni el XP total. */
    public suspend fun recordDailyActivity(
        today: LocalDate,
        xpEarned: Int = 0,
        lessonsCompleted: Int = 0,
        reviewsCompleted: Int = 0,
    ): Result<Unit>

    /** Registra una respuesta oral libre en la lección (ADR-050). */
    public suspend fun recordSpeakingMetrics(
        lessonId: LessonId,
        wpm: Float,
        score: Float,
        passed: Boolean,
    ): Result<Unit>

    /** Construye el snapshot v3 del progreso actual (respaldo, ADR-013/050). */
    public suspend fun snapshot(): Result<SyncSnapshot>

    /**
     * Restaura una copia remota con merge monótono (I4): combina local y
     * remoto tomando el máximo por campo y devuelve la racha resultante.
     */
    public suspend fun restore(snapshot: SyncSnapshot): Result<StreakState>
}

package dev.linguago.core.data.repository

import dev.linguago.core.common.DispatcherProvider
import dev.linguago.core.common.LinguaError
import dev.linguago.core.common.Result
import dev.linguago.core.data.database.LessonProgressDao
import dev.linguago.core.data.database.LessonProgressEntity
import dev.linguago.core.data.database.LinguagoDatabase
import dev.linguago.core.data.database.StreakProgressDao
import dev.linguago.core.data.database.StreakProgressEntity
import dev.linguago.core.domain.model.LessonId
import dev.linguago.core.domain.model.LessonProgress
import dev.linguago.core.domain.model.StreakState
import dev.linguago.core.domain.repository.ProgressRepository
import dev.linguago.core.domain.streak.StreakRules
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Progreso persistido en Room (ADR-005, I4). El único modelo de estado es
 * "solo avanza": XP y racha nunca decrecen, el progreso de lección solo se
 * marca completado. Toda la aritmética pasa por reglas de dominio
 * (StreakRules) — el repositorio es infraestructura, no lógica.
 */
@Singleton
public class ProgressRepositoryImpl
    @Inject
    constructor(
        private val database: LinguagoDatabase,
        private val dispatchers: DispatcherProvider,
    ) : ProgressRepository {
        override fun observeStreak(): Flow<StreakState> =
            database.streakProgressDao().observe().map { entity -> entity?.toDomain() ?: StreakState.zero() }

        override fun observeLessonProgress(): Flow<List<LessonProgress>> =
            database.lessonProgressDao().observeAll().map { entities -> entities.map { it.toDomain() } }

        override suspend fun applyDailyActivity(today: LocalDate): Result<StreakState> =
            withContext(dispatchers.io) {
                try {
                    val dao: StreakProgressDao = database.streakProgressDao()
                    val current = dao.load()?.toDomain() ?: StreakState.zero()
                    val computed = StreakRules.computeOnLessonCompletion(current, today)
                    dao.upsert(computed.toEntity())
                    Result.success(computed)
                } catch (e: Exception) {
                    Result.error(LinguaError.Unknown)
                }
            }

        override suspend fun addXp(amount: Int): Result<StreakState> =
            withContext(dispatchers.io) {
                try {
                    val dao: StreakProgressDao = database.streakProgressDao()
                    val current = dao.load()?.toDomain() ?: StreakState.zero()
                    val updated = current.copy(totalXp = current.totalXp + amount)
                    dao.upsert(updated.toEntity())
                    Result.success(updated)
                } catch (e: Exception) {
                    Result.error(LinguaError.Unknown)
                }
            }

        override suspend fun markLessonCompleted(
            lessonId: LessonId,
            xp: Int,
        ): Result<Unit> =
            withContext(dispatchers.io) {
                try {
                    val dao: LessonProgressDao = database.lessonProgressDao()
                    val previous = dao.load(lessonId.value)
                    val entity =
                        LessonProgressEntity(
                            lessonId = lessonId.value,
                            completed = true,
                            xp = (previous?.xp ?: 0) + xp,
                            attempts = (previous?.attempts ?: 0) + 1,
                        )
                    dao.upsert(entity)
                    Result.success(Unit)
                } catch (e: Exception) {
                    Result.error(LinguaError.Unknown)
                }
            }
    }

/** Mappers entity → dominio (una sola dirección: domain nunca ve entidades). */
private fun StreakProgressEntity.toDomain(): StreakState =
    StreakState(
        current = current,
        longest = longest,
        lastDate = LocalDate.ofEpochDay(lastDateEpochDay),
        totalXp = totalXp,
    )

private fun StreakState.toEntity(): StreakProgressEntity =
    StreakProgressEntity(
        id = STREAK_SINGLETON_ID,
        current = current,
        longest = longest,
        lastDateEpochDay = lastDate.toEpochDay(),
        totalXp = totalXp,
    )

private fun LessonProgressEntity.toDomain(): LessonProgress =
    LessonProgress(
        lessonId = LessonId(lessonId),
        completed = completed,
        xp = xp,
        attempts = attempts,
    )

private const val STREAK_SINGLETON_ID = 1L

package dev.linguago.core.data.repository

import dev.linguago.core.common.DispatcherProvider
import dev.linguago.core.common.LinguaError
import dev.linguago.core.common.Result
import dev.linguago.core.data.database.DailyActivityEntity
import dev.linguago.core.data.database.HeartsProgressEntity
import dev.linguago.core.data.database.LessonProgressDao
import dev.linguago.core.data.database.LessonProgressEntity
import dev.linguago.core.data.database.LinguagoDatabase
import dev.linguago.core.data.database.StreakProgressDao
import dev.linguago.core.data.database.StreakProgressEntity
import dev.linguago.core.data.database.WordProgressEntity
import dev.linguago.core.domain.hearts.HEARTS_MAX
import dev.linguago.core.domain.hearts.HeartsRules
import dev.linguago.core.domain.hearts.HeartsState
import dev.linguago.core.domain.model.DailyActivity
import dev.linguago.core.domain.model.LessonId
import dev.linguago.core.domain.model.LessonProgress
import dev.linguago.core.domain.model.StreakState
import dev.linguago.core.domain.model.SyncSnapshot
import dev.linguago.core.domain.repository.ProgressRepository
import dev.linguago.core.domain.streak.StreakRules
import dev.linguago.core.domain.sync.mergedWith
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

        override fun observeDailyActivity(days: Int): Flow<List<DailyActivity>> {
            val window = days.coerceAtLeast(1).toLong()
            val since = LocalDate.now().minusDays(window - 1).toEpochDay()
            return database
                .dailyActivityDao()
                .observeSince(since)
                .map { entities -> entities.map { it.toDomain() } }
        }

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
            scorePercent: Int,
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
                            speakingAttempts = previous?.speakingAttempts ?: 0,
                            speakingPassedAttempts = previous?.speakingPassedAttempts ?: 0,
                            speakingBestWpm = previous?.speakingBestWpm ?: 0f,
                            speakingLastScore = previous?.speakingLastScore ?: 0f,
                            bestScorePercent = maxOf(previous?.bestScorePercent ?: 0, scorePercent),
                            lastScorePercent = scorePercent,
                        )
                    dao.upsert(entity)
                    Result.success(Unit)
                } catch (e: Exception) {
                    Result.error(LinguaError.Unknown)
                }
            }

        override suspend fun recordDailyActivity(
            today: LocalDate,
            xpEarned: Int,
            lessonsCompleted: Int,
            reviewsCompleted: Int,
        ): Result<Unit> =
            withContext(dispatchers.io) {
                try {
                    val dao = database.dailyActivityDao()
                    val epochDay = today.toEpochDay()
                    val previous = dao.load(epochDay)
                    dao.upsert(
                        DailyActivityEntity(
                            epochDay = epochDay,
                            xpEarned = (previous?.xpEarned ?: 0) + xpEarned,
                            lessonsCompleted = (previous?.lessonsCompleted ?: 0) + lessonsCompleted,
                            reviewsCompleted = (previous?.reviewsCompleted ?: 0) + reviewsCompleted,
                        ),
                    )
                    Result.success(Unit)
                } catch (e: Exception) {
                    Result.error(LinguaError.Unknown)
                }
            }

        override suspend fun recordSpeakingMetrics(
            lessonId: LessonId,
            wpm: Float,
            score: Float,
            passed: Boolean,
        ): Result<Unit> =
            withContext(dispatchers.io) {
                try {
                    val dao = database.lessonProgressDao()
                    val previous = dao.load(lessonId.value)
                    dao.upsert(
                        LessonProgressEntity(
                            lessonId = lessonId.value,
                            completed = previous?.completed ?: false,
                            xp = previous?.xp ?: 0,
                            attempts = previous?.attempts ?: 0,
                            speakingAttempts = (previous?.speakingAttempts ?: 0) + 1,
                            speakingPassedAttempts =
                                (previous?.speakingPassedAttempts ?: 0) + if (passed) 1 else 0,
                            speakingBestWpm = maxOf(previous?.speakingBestWpm ?: 0f, wpm),
                            speakingLastScore = score,
                            bestScorePercent = previous?.bestScorePercent ?: 0,
                            lastScorePercent = previous?.lastScorePercent ?: 0,
                        ),
                    )
                    Result.success(Unit)
                } catch (e: Exception) {
                    Result.error(LinguaError.Unknown)
                }
            }

        override suspend fun snapshot(): Result<SyncSnapshot> =
            withContext(dispatchers.io) {
                try {
                    Result.success(buildSnapshot(System.currentTimeMillis()))
                } catch (e: Exception) {
                    Result.error(LinguaError.Unknown)
                }
            }

        override suspend fun restore(snapshot: SyncSnapshot): Result<StreakState> =
            withContext(dispatchers.io) {
                try {
                    val local = buildSnapshot(exportedAtMillis = 0L)
                    val merged = local.mergedWith(snapshot)
                    database.streakProgressDao().upsert(
                        StreakProgressEntity(
                            id = STREAK_SINGLETON_ID,
                            current = merged.streakCurrent,
                            longest = merged.streakLongest,
                            lastDateEpochDay = merged.streakLastDateEpochDay,
                            totalXp = merged.totalXp,
                        ),
                    )
                    merged.lessons.forEach { entry ->
                        database.lessonProgressDao().upsert(
                            LessonProgressEntity(
                                lessonId = entry.lessonId,
                                completed = entry.completed,
                                xp = entry.xp,
                                attempts = entry.attempts,
                                speakingAttempts = entry.speakingAttempts,
                                speakingPassedAttempts = entry.speakingPassedAttempts,
                                speakingBestWpm = entry.speakingBestWpm,
                                speakingLastScore = entry.speakingLastScore,
                                bestScorePercent = entry.bestScorePercent,
                                lastScorePercent = entry.lastScorePercent,
                            ),
                        )
                    }
                    if (merged.words.isNotEmpty()) {
                        database.wordProgressDao().upsertAll(merged.words.map { it.toEntity() })
                    }
                    // Vidas (ADR-025): mínimo de vidas y máximo del ancla de
                    // regeneración; una penalización no resucita al sincronizar.
                    database.heartsProgressDao().upsert(
                        HeartsProgressEntity(
                            id = HEARTS_SINGLETON_ID,
                            hearts = merged.heartsCurrent,
                            lastRegenEpochSecond = merged.heartsLastRegenEpochSecond,
                        ),
                    )
                    Result.success(
                        StreakState(
                            current = merged.streakCurrent,
                            longest = merged.streakLongest,
                            lastDate = LocalDate.ofEpochDay(merged.streakLastDateEpochDay),
                            totalXp = merged.totalXp,
                        ),
                    )
                } catch (e: Exception) {
                    Result.error(LinguaError.Unknown)
                }
            }

        /** Construye el estado local completo que alimenta snapshot y merge. */
        private suspend fun buildSnapshot(exportedAtMillis: Long): SyncSnapshot {
            val streak = database.streakProgressDao().load()?.toDomain() ?: StreakState.zero()
            val lessons = database.lessonProgressDao().snapshotAll()
            val hearts = effectiveHearts()
            return SyncSnapshot(
                exportedAtMillis = exportedAtMillis,
                streakCurrent = streak.current,
                streakLongest = streak.longest,
                streakLastDateEpochDay = streak.lastDate.toEpochDay(),
                totalXp = streak.totalXp,
                lessons = lessons.map { entity -> entity.toSnapshotEntry() },
                heartsCurrent = hearts.hearts,
                heartsLastRegenEpochSecond = hearts.lastRegenEpochSecond,
                words = database.wordProgressDao().snapshotAll().map { entity -> entity.toSnapshotEntry() },
            )
        }

        /** Vidas efectivas aplicando la regeneración por tiempo (ADR-025). */
        private suspend fun effectiveHearts(): HeartsState {
            val now = System.currentTimeMillis() / 1000L
            val stored = database.heartsProgressDao().load()
            return HeartsRules.regenerated(stored?.toDomain() ?: HeartsRules.initial(now), now)
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
        speakingAttempts = speakingAttempts,
        speakingPassedAttempts = speakingPassedAttempts,
        speakingBestWpm = speakingBestWpm,
        speakingLastScore = speakingLastScore,
        bestScorePercent = bestScorePercent,
        lastScorePercent = lastScorePercent,
    )

private fun LessonProgressEntity.toSnapshotEntry(): SyncSnapshot.LessonEntry =
    SyncSnapshot.LessonEntry(
        lessonId = lessonId,
        completed = completed,
        xp = xp,
        attempts = attempts,
        speakingAttempts = speakingAttempts,
        speakingPassedAttempts = speakingPassedAttempts,
        speakingBestWpm = speakingBestWpm,
        speakingLastScore = speakingLastScore,
        bestScorePercent = bestScorePercent,
        lastScorePercent = lastScorePercent,
    )

private fun WordProgressEntity.toSnapshotEntry(): SyncSnapshot.WordEntry =
    SyncSnapshot.WordEntry(
        word = word,
        courseId = courseId,
        translation = translation,
        reviewCount = reviewCount,
        lastReviewEpochMillis = lastReviewEpochMillis,
        nextReviewEpochMillis = nextReviewEpochMillis,
        stability = stability,
        difficulty = difficulty,
        errorCount = errorCount,
        lastErrorType = lastErrorType,
        culturalNote = culturalNote,
    )

private fun SyncSnapshot.WordEntry.toEntity(): WordProgressEntity =
    WordProgressEntity(
        word = word,
        courseId = courseId,
        translation = translation,
        reviewCount = reviewCount,
        lastReviewEpochMillis = lastReviewEpochMillis,
        nextReviewEpochMillis = nextReviewEpochMillis,
        stability = stability,
        difficulty = difficulty,
        errorCount = errorCount,
        lastErrorType = lastErrorType,
        culturalNote = culturalNote,
    )

private fun DailyActivityEntity.toDomain(): DailyActivity =
    DailyActivity(
        epochDay = epochDay,
        xpEarned = xpEarned,
        lessonsCompleted = lessonsCompleted,
        reviewsCompleted = reviewsCompleted,
    )

private fun HeartsProgressEntity.toDomain(): HeartsState =
    HeartsState(
        hearts = hearts,
        max = HEARTS_MAX,
        lastRegenEpochSecond = lastRegenEpochSecond,
    )

private const val STREAK_SINGLETON_ID = 1L
private const val HEARTS_SINGLETON_ID = 1L

package dev.linguago.core.data.repository

import dev.linguago.core.common.DispatcherProvider
import dev.linguago.core.common.LinguaError
import dev.linguago.core.common.Result
import dev.linguago.core.data.database.LinguagoDatabase
import dev.linguago.core.data.database.WordProgressDao
import dev.linguago.core.data.database.WordProgressEntity
import dev.linguago.core.domain.logic.FsrsScheduler
import dev.linguago.core.domain.logic.ReviewRating
import dev.linguago.core.domain.logic.VocabularyExtractor
import dev.linguago.core.domain.model.Course
import dev.linguago.core.domain.model.CourseId
import dev.linguago.core.domain.model.Lesson
import dev.linguago.core.domain.model.WordProgress
import dev.linguago.core.domain.repository.ProgressRepository
import dev.linguago.core.domain.repository.ReviewRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repositorio del repaso espaciado (ADR-016): Room para el estado FSRS y el
 * programador puro de dominio para calcular vencimientos. Completar una
 * lección solo agrega tarjetas nuevas; nunca reinicia el estado existente.
 */
@Singleton
public class ReviewRepositoryImpl
    @Inject
    constructor(
        private val database: LinguagoDatabase,
        private val progressRepository: ProgressRepository,
        private val dispatchers: DispatcherProvider,
    ) : ReviewRepository {
        override suspend fun collectFromLesson(
            course: Course,
            lesson: Lesson,
            now: Instant,
        ): Result<Int> =
            withContext(dispatchers.io) {
                try {
                    val dao: WordProgressDao = database.wordProgressDao()
                    val existingEntities = dao.loadAll(course.id.value)
                    val existingByKey = existingEntities.associateBy { entity -> pairKey(entity.word, entity.translation) }
                    val pairs = VocabularyExtractor.fromLesson(lesson, course.toLanguage)
                    val newCards =
                        pairs
                            .filterNot { pair -> pairKey(pair.source, pair.target) in existingByKey }
                            .map { pair ->
                                FsrsScheduler
                                    .initialState(
                                        word = pair.source,
                                        translation = pair.target,
                                        courseId = course.id.value,
                                        now = now,
                                    ).copy(culturalNote = pair.culturalNote)
                                    .toEntity()
                            }
                    // Backfill de notas culturales en tarjetas anteriores a v5
                    // (ADR-050): la tarjeta conserva su estado FSRS intacto.
                    val enriched =
                        pairs.mapNotNull { pair ->
                            val note = pair.culturalNote ?: return@mapNotNull null
                            val existing = existingByKey[pairKey(pair.source, pair.target)] ?: return@mapNotNull null
                            existing.takeIf { it.culturalNote == null }?.copy(culturalNote = note)
                        }
                    if (newCards.isNotEmpty()) {
                        dao.insertAll(newCards)
                    }
                    if (enriched.isNotEmpty()) {
                        dao.upsertAll(enriched)
                    }
                    Result.success(newCards.size)
                } catch (error: Exception) {
                    Result.error(LinguaError.Unknown)
                }
            }

        override fun observeDueCount(courseId: CourseId): Flow<Int> =
            database
                .wordProgressDao()
                .observeAll(courseId.value)
                .map { entities -> entities.count { it.isDueAt(System.currentTimeMillis()) } }

        override fun observeTotalCount(courseId: CourseId): Flow<Int> =
            database
                .wordProgressDao()
                .observeAll(courseId.value)
                .map { entities -> entities.size }

        override suspend fun due(
            courseId: CourseId,
            limit: Int,
        ): Result<List<WordProgress>> =
            withContext(dispatchers.io) {
                try {
                    val nowMillis = System.currentTimeMillis()
                    val dueCards =
                        database
                            .wordProgressDao()
                            .loadAll(courseId.value)
                            .filter { entity -> entity.isDueAt(nowMillis) }
                            .take(limit)
                            .map { entity -> entity.toDomain() }
                    Result.success(dueCards)
                } catch (error: Exception) {
                    Result.error(LinguaError.Unknown)
                }
            }

        override suspend fun grade(
            courseId: CourseId,
            word: String,
            rating: ReviewRating,
            now: Instant,
        ): Result<WordProgress> =
            withContext(dispatchers.io) {
                try {
                    val dao: WordProgressDao = database.wordProgressDao()
                    val current =
                        dao.load(courseId.value, word)?.toDomain()
                            ?: return@withContext Result.error(LinguaError.NotFound)
                    val updated = FsrsScheduler.grade(current, rating, now).progress
                    dao.upsert(updated.toEntity())
                    // Actividad diaria local (ADR-050): un fallo de esta
                    // escritura no debe revertir el repaso ya guardado.
                    progressRepository.recordDailyActivity(
                        today = LocalDate.now(),
                        reviewsCompleted = 1,
                    )
                    Result.success(updated)
                } catch (error: Exception) {
                    Result.error(LinguaError.Unknown)
                }
            }

        private fun WordProgressEntity.isDueAt(nowMillis: Long): Boolean = nextReviewEpochMillis <= nowMillis

        private fun WordProgressEntity.toDomain(): WordProgress =
            WordProgress(
                word = word,
                translation = translation,
                courseId = courseId,
                reviewCount = reviewCount,
                lastReview = Instant.ofEpochMilli(lastReviewEpochMillis),
                nextReview = Instant.ofEpochMilli(nextReviewEpochMillis),
                stability = stability,
                difficulty = difficulty,
                culturalNote = culturalNote,
            )

        private fun WordProgress.toEntity(): WordProgressEntity =
            WordProgressEntity(
                word = word,
                courseId = courseId,
                translation = translation,
                reviewCount = reviewCount,
                lastReviewEpochMillis = lastReview.toEpochMilli(),
                nextReviewEpochMillis = nextReview.toEpochMilli(),
                stability = stability,
                difficulty = difficulty,
                culturalNote = culturalNote,
            )
    }

private fun pairKey(
    source: String,
    target: String,
): String = listOf(source.lowercase(), target.lowercase()).sorted().joinToString("|")

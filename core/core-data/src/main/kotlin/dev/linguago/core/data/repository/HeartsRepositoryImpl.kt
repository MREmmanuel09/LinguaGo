package dev.linguago.core.data.repository

import dev.linguago.core.common.DispatcherProvider
import dev.linguago.core.common.LinguaError
import dev.linguago.core.common.Result
import dev.linguago.core.data.database.HeartsProgressEntity
import dev.linguago.core.data.database.LinguagoDatabase
import dev.linguago.core.domain.hearts.HEARTS_MAX
import dev.linguago.core.domain.hearts.HeartsRules
import dev.linguago.core.domain.hearts.HeartsState
import dev.linguago.core.domain.repository.HeartsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Vidas persistidas en Room (ADR-025). La regeneración se calcula por epoch en
 * cada lectura y se persiste el ancla resultante, así que el tiempo con la app
 * cerrada también recupera vidas. No se guarda `max` (constante del dominio).
 */
@Singleton
public class HeartsRepositoryImpl
    @Inject
    constructor(
        private val database: LinguagoDatabase,
        private val dispatchers: DispatcherProvider,
    ) : HeartsRepository {
        override fun observe(): Flow<HeartsState> =
            database
                .heartsProgressDao()
                .observe()
                .map { entity ->
                    val now = nowEpochSecond()
                    HeartsRules.regenerated(entity?.toDomain() ?: HeartsRules.initial(now), now)
                }

        override suspend fun current(): Result<HeartsState> =
            withContext(dispatchers.io) {
                try {
                    val now = nowEpochSecond()
                    val stored = database.heartsProgressDao().load()
                    val state = HeartsRules.regenerated(stored?.toDomain() ?: HeartsRules.initial(now), now)
                    if (stored == null || state != stored.toDomain()) {
                        database.heartsProgressDao().upsert(state.toEntity())
                    }
                    Result.success(state)
                } catch (e: Exception) {
                    Result.error(LinguaError.Unknown)
                }
            }

        override suspend fun consume(): Result<HeartsState> =
            withContext(dispatchers.io) {
                try {
                    val now = nowEpochSecond()
                    val stored = database.heartsProgressDao().load()
                    val before = HeartsRules.regenerated(stored?.toDomain() ?: HeartsRules.initial(now), now)
                    val after = HeartsRules.consume(before, now)
                    database.heartsProgressDao().upsert(after.toEntity())
                    Result.success(after)
                } catch (e: Exception) {
                    Result.error(LinguaError.Unknown)
                }
            }

        override suspend fun refill(): Result<HeartsState> =
            withContext(dispatchers.io) {
                try {
                    val now = nowEpochSecond()
                    val stored = database.heartsProgressDao().load()
                    val before = HeartsRules.regenerated(stored?.toDomain() ?: HeartsRules.initial(now), now)
                    val after = HeartsRules.refill(before, now)
                    database.heartsProgressDao().upsert(after.toEntity())
                    Result.success(after)
                } catch (e: Exception) {
                    Result.error(LinguaError.Unknown)
                }
            }
    }

private fun HeartsProgressEntity.toDomain(): HeartsState =
    HeartsState(
        hearts = hearts,
        max = HEARTS_MAX,
        lastRegenEpochSecond = lastRegenEpochSecond,
    )

private fun HeartsState.toEntity(): HeartsProgressEntity =
    HeartsProgressEntity(
        id = HEARTS_SINGLETON_ID,
        hearts = hearts,
        lastRegenEpochSecond = lastRegenEpochSecond,
    )

private fun nowEpochSecond(): Long = System.currentTimeMillis() / 1000L

private const val HEARTS_SINGLETON_ID = 1L

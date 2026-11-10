package dev.linguago.core.sync

import dev.linguago.core.common.LinguaError
import dev.linguago.core.common.NoOpCryptoService
import dev.linguago.core.common.Result
import dev.linguago.core.domain.model.DailyActivity
import dev.linguago.core.domain.model.LessonId
import dev.linguago.core.domain.model.LessonProgress
import dev.linguago.core.domain.model.StreakState
import dev.linguago.core.domain.model.SyncSnapshot
import dev.linguago.core.domain.repository.ProgressRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

/** Orquestación SyncEngine (ADR-013) con repositorio y nube falsos. */
public class SyncEngineTest {
    /** Repositorio en memoria; restore guarda el snapshot SIN merge (eso ya lo prueba el impl real). */
    private class FakeProgressRepository : ProgressRepository {
        var local: SyncSnapshot =
            SyncSnapshot(
                exportedAtMillis = 0,
                streakCurrent = 0,
                streakLongest = 0,
                streakLastDateEpochDay = 0,
                totalXp = 0,
                lessons = emptyList(),
            )
        var restored: SyncSnapshot? = null

        override fun observeStreak(): Flow<StreakState> = MutableStateFlow(StreakState.zero())

        override suspend fun applyDailyActivity(today: LocalDate): Result<StreakState> = Result.success(StreakState.zero())

        override suspend fun addXp(amount: Int): Result<StreakState> = Result.success(StreakState.zero())

        override suspend fun markLessonCompleted(
            lessonId: LessonId,
            xp: Int,
            scorePercent: Int,
        ): Result<Unit> = Result.success(Unit)

        override fun observeLessonProgress(): Flow<List<LessonProgress>> = MutableStateFlow(emptyList())

        override fun observeDailyActivity(days: Int): Flow<List<DailyActivity>> = MutableStateFlow(emptyList())

        override suspend fun recordDailyActivity(
            today: java.time.LocalDate,
            xpEarned: Int,
            lessonsCompleted: Int,
            reviewsCompleted: Int,
        ): Result<Unit> = Result.success(Unit)

        override suspend fun recordSpeakingMetrics(
            lessonId: LessonId,
            wpm: Float,
            score: Float,
            passed: Boolean,
        ): Result<Unit> = Result.success(Unit)

        override suspend fun snapshot(): Result<SyncSnapshot> = Result.success(local)

        override suspend fun restore(snapshot: SyncSnapshot): Result<StreakState> {
            restored = snapshot
            local = snapshot
            return Result.success(
                StreakState(
                    current = snapshot.streakCurrent,
                    longest = snapshot.streakLongest,
                    lastDate = LocalDate.ofEpochDay(snapshot.streakLastDateEpochDay),
                    totalXp = snapshot.totalXp,
                ),
            )
        }
    }

    /** Nube falsa que guarda el último payload (push) y lo devuelve (fetch). */
    private class FakeCloudProvider : CloudProvider {
        var stored: ByteArray? = null
        var linkUp: Boolean = true

        override val id: String = "fake"

        override fun isLinked(): Boolean = linkUp

        override suspend fun fetchSnapshot(): Result<ByteArray> = stored?.let { Result.success(it) } ?: Result.error(LinguaError.NotFound)

        override suspend fun pushSnapshot(payload: ByteArray): Result<Unit> {
            stored = payload
            return Result.success(Unit)
        }
    }

    @Test
    public fun `backup construye snapshot del repo y lo sube`() =
        runTest {
            val repo = FakeProgressRepository()
            val cloud = FakeCloudProvider()
            repo.local =
                SyncSnapshot(
                    exportedAtMillis = 42,
                    streakCurrent = 2,
                    streakLongest = 2,
                    streakLastDateEpochDay = 10,
                    totalXp = 60,
                    lessons = listOf(SyncSnapshot.LessonEntry("l1", completed = true, xp = 60, attempts = 1)),
                )
            val engine = SyncEngine(repo, SnapshotCodec(), NoOpCryptoService(), cloud)

            val result = engine.backup()

            assertTrue(result is Result.Success)
            val payload = String(cloud.stored!!, Charsets.UTF_8)
            assertTrue(payload.contains("\"totalXp\":60"))
            assertTrue(payload.contains("\"lessonId\":\"l1\""))
        }

    @Test
    public fun `exportSnapshot produce JSON v3 con tarjetas FSRS`() =
        runTest {
            val repo = FakeProgressRepository()
            repo.local =
                SyncSnapshot(
                    exportedAtMillis = 10,
                    streakCurrent = 1,
                    streakLongest = 1,
                    streakLastDateEpochDay = 5,
                    totalXp = 40,
                    lessons = emptyList(),
                    words =
                        listOf(
                            SyncSnapshot.WordEntry(
                                word = "hola",
                                courseId = "es-en",
                                translation = "hello",
                                reviewCount = 2,
                                lastReviewEpochMillis = 100,
                                nextReviewEpochMillis = 200,
                                stability = 3f,
                                difficulty = 5f,
                                culturalNote = "Saludo informal",
                            ),
                        ),
                )
            val engine = SyncEngine(repo, SnapshotCodec(), NoOpCryptoService(), FakeCloudProvider())

            val payload = (engine.exportSnapshot() as Result.Success).value
            val json = String(payload, Charsets.UTF_8)

            assertTrue(json.contains("\"schemaVersion\":3"))
            assertTrue(json.contains("\"word\":\"hola\""))
            assertTrue(json.contains("\"culturalNote\":\"Saludo informal\""))
        }

    @Test
    public fun `importSnapshot decodifica y restaura tarjetas FSRS`() =
        runTest {
            val repo = FakeProgressRepository()
            val codec = SnapshotCodec()
            val remote =
                SyncSnapshot(
                    exportedAtMillis = 90,
                    streakCurrent = 2,
                    streakLongest = 3,
                    streakLastDateEpochDay = 12,
                    totalXp = 80,
                    lessons = emptyList(),
                    words =
                        listOf(
                            SyncSnapshot.WordEntry(
                                word = "bonjour",
                                courseId = "es-fr",
                                translation = "hola",
                                reviewCount = 4,
                                lastReviewEpochMillis = 500,
                                nextReviewEpochMillis = 900,
                                stability = 7f,
                                difficulty = 4f,
                            ),
                        ),
                )
            val payload = (codec.encode(remote) as Result.Success).value
            val engine = SyncEngine(repo, codec, NoOpCryptoService(), FakeCloudProvider())

            val result = engine.importSnapshot(payload)

            assertTrue(result is Result.Success)
            assertEquals(
                "bonjour",
                repo.restored
                    ?.words
                    ?.single()
                    ?.word,
            )
            assertEquals(
                4,
                repo.restored
                    ?.words
                    ?.single()
                    ?.reviewCount,
            )
        }

    @Test
    public fun `restore decodifica payload de la nube y lo da al repo`() =
        runTest {
            val repo = FakeProgressRepository()
            val cloud = FakeCloudProvider()
            val codec = SnapshotCodec()
            val remote =
                SyncSnapshot(
                    exportedAtMillis = 77,
                    streakCurrent = 3,
                    streakLongest = 4,
                    streakLastDateEpochDay = 30,
                    totalXp = 120,
                    lessons = listOf(SyncSnapshot.LessonEntry("l2", completed = true, xp = 120, attempts = 2)),
                )
            cloud.stored = (codec.encode(remote) as Result.Success).value
            val engine = SyncEngine(repo, codec, NoOpCryptoService(), cloud)

            val result = engine.restore()

            assertTrue(result is Result.Success)
            assertEquals(120, (result as Result.Success).value.totalXp)
            assertEquals(
                "l2",
                repo.restored
                    ?.lessons
                    ?.single()
                    ?.lessonId,
            )
        }

    @Test
    public fun `sin nube vinculada backup falla con NotLinked`() =
        runTest {
            val engine = SyncEngine(FakeProgressRepository(), SnapshotCodec(), NoOpCryptoService(), NoopCloudProvider())

            val result = engine.backup()

            assertTrue(result is Result.Error)
            assertEquals(LinguaError.NotLinked, (result as Result.Error).error)
        }
}

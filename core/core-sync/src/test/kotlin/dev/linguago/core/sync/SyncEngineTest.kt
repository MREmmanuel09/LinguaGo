package dev.linguago.core.sync

import dev.linguago.core.common.LinguaError
import dev.linguago.core.common.NoOpCryptoService
import dev.linguago.core.common.Result
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
            lessonId: dev.linguago.core.domain.model.LessonId,
            xp: Int,
        ): Result<Unit> = Result.success(Unit)

        override fun observeLessonProgress(): Flow<List<dev.linguago.core.domain.model.LessonProgress>> = MutableStateFlow(emptyList())

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

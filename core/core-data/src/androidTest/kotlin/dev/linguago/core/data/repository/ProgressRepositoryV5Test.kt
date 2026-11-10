package dev.linguago.core.data.repository

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.linguago.core.common.DefaultDispatcherProvider
import dev.linguago.core.common.Result
import dev.linguago.core.data.database.LinguagoDatabase
import dev.linguago.core.data.database.WordProgressEntity
import dev.linguago.core.domain.model.LessonId
import dev.linguago.core.domain.model.SyncSnapshot
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

/**
 * Persistencia v5 (ADR-050): métricas de voz/precisión, actividad diaria y
 * snapshot v3 con tarjetas FSRS sobre una base Room real en memoria.
 */
@RunWith(AndroidJUnit4::class)
public class ProgressRepositoryV5Test {
    private lateinit var database: LinguagoDatabase
    private lateinit var repository: ProgressRepositoryImpl

    @Before
    public fun setUp() {
        database =
            Room
                .inMemoryDatabaseBuilder(
                    ApplicationProvider.getApplicationContext(),
                    LinguagoDatabase::class.java,
                ).allowMainThreadQueries()
                .build()
        repository = ProgressRepositoryImpl(database, DefaultDispatcherProvider())
    }

    @After
    public fun tearDown() {
        database.close()
    }

    @Test
    public fun metricas_de_voz_y_nota_se_acumulan_sin_perder_el_maximo() =
        runBlocking {
            repository.markLessonCompleted(LessonId("l1"), xp = 30, scorePercent = 80)
            repository.recordSpeakingMetrics(LessonId("l1"), wpm = 90f, score = 0.75f, passed = true)
            repository.recordSpeakingMetrics(LessonId("l1"), wpm = 120f, score = 0.5f, passed = false)

            val progress = repository.observeLessonProgress().first().single()

            assertTrue(progress.completed)
            assertEquals(80, progress.bestScorePercent)
            assertEquals(80, progress.lastScorePercent)
            assertEquals(2, progress.speakingAttempts)
            assertEquals(1, progress.speakingPassedAttempts)
            assertEquals(120f, progress.speakingBestWpm, 0.001f)
            assertEquals(0.5f, progress.speakingLastScore, 0.001f)
        }

    @Test
    public fun actividad_diaria_suma_lecciones_repasos_y_xp_del_mismo_dia() =
        runBlocking {
            val today = LocalDate.now()
            repository.recordDailyActivity(today, xpEarned = 40, lessonsCompleted = 1)
            repository.recordDailyActivity(today, reviewsCompleted = 3)

            val activity = repository.observeDailyActivity().first().single()

            assertEquals(today.toEpochDay(), activity.epochDay)
            assertEquals(40, activity.xpEarned)
            assertEquals(1, activity.lessonsCompleted)
            assertEquals(3, activity.reviewsCompleted)
            assertTrue(activity.isActive)
        }

    @Test
    public fun snapshot_v3_incluye_tarjetas_fsrs_y_nota_cultural() =
        runBlocking {
            database.wordProgressDao().upsert(
                WordProgressEntity(
                    word = "hola",
                    courseId = "es-en",
                    translation = "hello",
                    reviewCount = 3,
                    lastReviewEpochMillis = 1_000,
                    nextReviewEpochMillis = 2_000,
                    stability = 2.5f,
                    difficulty = 4f,
                    errorCount = 1,
                    lastErrorType = "grammar",
                    culturalNote = "Saludo informal",
                ),
            )

            val snapshot = (repository.snapshot() as Result.Success).value

            assertEquals(SyncSnapshot.SCHEMA_VERSION, snapshot.schemaVersion)
            val word = snapshot.words.single()
            assertEquals("hola", word.word)
            assertEquals(3, word.reviewCount)
            assertEquals("Saludo informal", word.culturalNote)
        }

    @Test
    public fun restaurar_snapshot_conserva_tarjetas_y_vidas_sin_retroceder() =
        runBlocking {
            val remote =
                SyncSnapshot(
                    exportedAtMillis = 1_000,
                    streakCurrent = 3,
                    streakLongest = 4,
                    streakLastDateEpochDay = 20_000,
                    totalXp = 250,
                    lessons =
                        listOf(
                            SyncSnapshot.LessonEntry(
                                lessonId = "l1",
                                completed = true,
                                xp = 30,
                                attempts = 1,
                                bestScorePercent = 75,
                            ),
                        ),
                    heartsCurrent = 6,
                    heartsLastRegenEpochSecond = 500,
                    words =
                        listOf(
                            SyncSnapshot.WordEntry(
                                word = "bonjour",
                                courseId = "es-fr",
                                translation = "hola",
                                reviewCount = 4,
                                lastReviewEpochMillis = 1_000,
                                nextReviewEpochMillis = 2_000,
                                stability = 7f,
                                difficulty = 4f,
                            ),
                        ),
                )

            val result = repository.restore(remote)

            assertTrue(result is Result.Success)
            val restored = (repository.snapshot() as Result.Success).value
            assertEquals(3, restored.streakCurrent)
            assertEquals(250, restored.totalXp)
            assertEquals(6, restored.heartsCurrent)
            assertEquals("bonjour", restored.words.single().word)
            assertEquals(4, restored.words.single().reviewCount)
            assertEquals(75, restored.lessons.single().bestScorePercent)
        }

    @Test
    public fun restaurar_no_borra_progreso_local_mas_avanzado() =
        runBlocking {
            database.wordProgressDao().upsert(
                WordProgressEntity(
                    word = "hola",
                    courseId = "es-en",
                    translation = "hello",
                    reviewCount = 8,
                    lastReviewEpochMillis = 9_000,
                    nextReviewEpochMillis = 10_000,
                    stability = 12f,
                    difficulty = 3f,
                ),
            )
            val remote =
                SyncSnapshot(
                    exportedAtMillis = 1_000,
                    streakCurrent = 1,
                    streakLongest = 1,
                    streakLastDateEpochDay = 1,
                    totalXp = 10,
                    lessons = emptyList(),
                    words =
                        listOf(
                            SyncSnapshot.WordEntry(
                                word = "hola",
                                courseId = "es-en",
                                translation = "hello",
                                reviewCount = 2,
                                lastReviewEpochMillis = 500,
                                nextReviewEpochMillis = 1_000,
                                stability = 2f,
                                difficulty = 5f,
                            ),
                        ),
                )

            repository.restore(remote)

            val restored = (repository.snapshot() as Result.Success).value.words.single()
            assertEquals(8, restored.reviewCount)
            assertEquals(9_000L, restored.lastReviewEpochMillis)
        }
}

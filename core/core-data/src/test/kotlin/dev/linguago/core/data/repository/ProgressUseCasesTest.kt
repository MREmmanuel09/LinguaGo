package dev.linguago.core.data.repository

import dev.linguago.core.common.LinguaError
import dev.linguago.core.common.Result
import dev.linguago.core.domain.model.Exercise
import dev.linguago.core.domain.model.ExerciseId
import dev.linguago.core.domain.model.LessonId
import dev.linguago.core.domain.model.LessonProgress
import dev.linguago.core.domain.model.StreakState
import dev.linguago.core.domain.model.SyncSnapshot
import dev.linguago.core.domain.repository.ProgressRepository
import dev.linguago.core.domain.usecase.ExerciseAnswer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

/**
 * Regresión de ADR-025: el XP se persiste UNA sola vez al completar la
 * lección. Antes de este cambio se sumaba por respuesta y otra vez al
 * completar (doble conteo), lo que además impedía descontar el XP de una
 * lección cancelada por quedarse sin vidas.
 */
class ProgressUseCasesTest {
    private val repository = RecordingProgressRepository()
    private val submit = SubmitAnswerUseCaseImpl()
    private val complete = CompleteLessonUseCaseImpl(repository)

    @Test
    fun `a correct answer does not persist xp`() =
        runTest {
            val result = submit(correctChoice(), ExerciseAnswer.OptionPicked(0), emptyList())
            when (result) {
                is Result.Success -> assertTrue(result.value.isCorrect)
                is Result.Error -> error("la calificación no debería fallar")
            }
            assertTrue(repository.addXpCalls.isEmpty())
            assertEquals(0, repository.streak.totalXp)
        }

    @Test
    fun `completing the lesson persists the session xp exactly once`() =
        runTest {
            complete(LessonId("ls-1"), 80)
            assertEquals(listOf(80), repository.addXpCalls)
            assertEquals(listOf(LessonId("ls-1") to 80), repository.completedLessons)
            assertEquals(1, repository.dailyActivityCalls)
            assertEquals(80, repository.streak.totalXp)
        }

    @Test
    fun `answering then completing does not double count xp`() =
        runTest {
            submit(correctChoice(), ExerciseAnswer.OptionPicked(0), emptyList())
            complete(LessonId("ls-1"), 10)
            assertEquals(listOf(10), repository.addXpCalls)
            assertEquals(10, repository.streak.totalXp)
        }

    private fun correctChoice(): Exercise.MultipleChoice =
        Exercise.MultipleChoice(
            id = ExerciseId("ex-1"),
            question = "Elige la traducción",
            options = listOf("hola", "adios"),
            correctIndex = 0,
        )
}

private class RecordingProgressRepository : ProgressRepository {
    val addXpCalls = mutableListOf<Int>()
    val completedLessons = mutableListOf<Pair<LessonId, Int>>()
    var dailyActivityCalls = 0
    var streak = StreakState.zero()

    override fun observeStreak(): Flow<StreakState> = MutableStateFlow(streak)

    override suspend fun applyDailyActivity(today: LocalDate): Result<StreakState> {
        dailyActivityCalls += 1
        return Result.success(streak)
    }

    override suspend fun addXp(amount: Int): Result<StreakState> {
        addXpCalls += amount
        streak = streak.copy(totalXp = streak.totalXp + amount)
        return Result.success(streak)
    }

    override suspend fun markLessonCompleted(
        lessonId: LessonId,
        xp: Int,
    ): Result<Unit> {
        completedLessons += lessonId to xp
        return Result.success(Unit)
    }

    override fun observeLessonProgress(): Flow<List<LessonProgress>> = flowOf(emptyList())

    override suspend fun snapshot(): Result<SyncSnapshot> = Result.error(LinguaError.Unknown)

    override suspend fun restore(snapshot: SyncSnapshot): Result<StreakState> = Result.success(streak)
}

package dev.linguago.core.data.repository

import dev.linguago.core.common.LinguaError
import dev.linguago.core.common.Result
import dev.linguago.core.domain.hearts.HeartsRules
import dev.linguago.core.domain.hearts.HeartsState
import dev.linguago.core.domain.logic.GradingResult
import dev.linguago.core.domain.model.Exercise
import dev.linguago.core.domain.model.ExerciseId
import dev.linguago.core.domain.model.GrammarPoint
import dev.linguago.core.domain.repository.HeartsRepository
import dev.linguago.core.domain.usecase.ExerciseAnswer
import dev.linguago.core.domain.usecase.SubmitAnswerUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Registro de respuesta (ADR-025): el fallo consume una vida, a 0 se cancela
 * la lección y los SKIP de voz (consumeHeart = false) nunca penalizan.
 */
class RegisterAnswerUseCaseTest {
    @Test
    fun `un acierto no consume vida`() =
        runTest {
            val hearts = FakeHeartsRepository(HeartsState(hearts = 3, lastRegenEpochSecond = TEST_NOW))
            val useCase = useCase(grading = GradingResult(isCorrect = true), hearts = hearts)

            val outcome = (useCase(exercise(), ExerciseAnswer.OptionPicked(0)) as Result.Success).value

            assertTrue(outcome.grading.isCorrect)
            assertFalse(outcome.lessonCancelled)
            assertEquals(3, outcome.hearts?.hearts)
            assertEquals(0, hearts.consumeCalls)
        }

    @Test
    fun `un fallo consume una vida`() =
        runTest {
            val hearts = FakeHeartsRepository(HeartsState(hearts = 3, lastRegenEpochSecond = TEST_NOW))
            val useCase = useCase(grading = GradingResult(isCorrect = false), hearts = hearts)

            val outcome = (useCase(exercise(), ExerciseAnswer.OptionPicked(1)) as Result.Success).value

            assertEquals(2, outcome.hearts?.hearts)
            assertFalse(outcome.lessonCancelled)
            assertEquals(1, hearts.consumeCalls)
        }

    @Test
    fun `llegar a cero cancela la leccion`() =
        runTest {
            val hearts = FakeHeartsRepository(HeartsState(hearts = 1, lastRegenEpochSecond = TEST_NOW))
            val useCase = useCase(grading = GradingResult(isCorrect = false), hearts = hearts)

            val outcome = (useCase(exercise(), ExerciseAnswer.OptionPicked(1)) as Result.Success).value

            assertEquals(0, outcome.hearts?.hearts)
            assertTrue(outcome.lessonCancelled)
        }

    @Test
    fun `el skip de voz no consume vida`() =
        runTest {
            val hearts = FakeHeartsRepository(HeartsState(hearts = 3, lastRegenEpochSecond = TEST_NOW))
            val useCase = useCase(grading = GradingResult(isCorrect = false), hearts = hearts)

            val outcome =
                (useCase(exercise(), ExerciseAnswer.SpokenText(""), consumeHeart = false) as Result.Success).value

            assertEquals(3, outcome.hearts?.hearts)
            assertFalse(outcome.lessonCancelled)
            assertEquals(0, hearts.consumeCalls)
        }

    @Test
    fun `un fallo de persistencia de vidas no bloquea la respuesta`() =
        runTest {
            val hearts = FakeHeartsRepository(HeartsState(hearts = 3, lastRegenEpochSecond = TEST_NOW), failConsume = true)
            val useCase = useCase(grading = GradingResult(isCorrect = false), hearts = hearts)

            val outcome = (useCase(exercise(), ExerciseAnswer.OptionPicked(1)) as Result.Success).value

            assertFalse(outcome.grading.isCorrect)
            assertNull(outcome.hearts)
            assertFalse(outcome.lessonCancelled)
        }

    @Test
    fun `un error de calificacion se propaga`() =
        runTest {
            val hearts = FakeHeartsRepository(HeartsState(hearts = 3, lastRegenEpochSecond = TEST_NOW))
            val useCase = useCase(grading = null, hearts = hearts)

            val result = useCase(exercise(), ExerciseAnswer.OptionPicked(0))

            assertTrue(result is Result.Error)
            assertEquals(0, hearts.consumeCalls)
        }

    private fun useCase(
        grading: GradingResult?,
        hearts: FakeHeartsRepository,
    ): RegisterAnswerUseCaseImpl =
        RegisterAnswerUseCaseImpl(
            submitAnswerUseCase = FakeSubmitAnswer(grading),
            repository = hearts,
        )

    private fun exercise(): Exercise.MultipleChoice =
        Exercise.MultipleChoice(
            id = ExerciseId("ex-1"),
            question = "Elige",
            options = listOf("a", "b"),
            correctIndex = 0,
        )
}

private const val TEST_NOW = 1_000L

private class FakeSubmitAnswer(
    private val grading: GradingResult?,
) : SubmitAnswerUseCase {
    override suspend fun invoke(
        exercise: Exercise,
        answer: ExerciseAnswer,
        grammarPoints: List<GrammarPoint>,
    ): Result<GradingResult> =
        if (grading == null) {
            Result.error(LinguaError.Unknown)
        } else {
            Result.success(grading)
        }
}

private class FakeHeartsRepository(
    private var state: HeartsState,
    private val failConsume: Boolean = false,
) : HeartsRepository {
    var consumeCalls = 0

    override fun observe(): Flow<HeartsState> = flowOf(state)

    override suspend fun current(): Result<HeartsState> = Result.success(state)

    override suspend fun consume(): Result<HeartsState> {
        consumeCalls += 1
        if (failConsume) return Result.error(LinguaError.Unknown)
        state = HeartsRules.consume(state, TEST_NOW)
        return Result.success(state)
    }

    override suspend fun refill(): Result<HeartsState> {
        state = HeartsRules.refill(state, TEST_NOW)
        return Result.success(state)
    }
}

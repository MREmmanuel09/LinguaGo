package dev.linguago.core.domain.logic

import dev.linguago.core.domain.model.Exercise
import dev.linguago.core.domain.model.ExerciseId
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RetryPolicyTest {
    private val listen =
        Exercise.ListenType(
            id = ExerciseId("e1"),
            transcription = "Thank you",
            audioText = "Thank you",
        )

    private val choice =
        Exercise.MultipleChoice(
            id = ExerciseId("e2"),
            question = "q",
            options = listOf("a", "b"),
            correctIndex = 0,
        )

    @Test
    fun `solo escucha y produccion admiten reintento`() {
        assertTrue(RetryPolicy.supportsRetry(listen))
        assertFalse(RetryPolicy.supportsRetry(choice))
    }

    @Test
    fun `no se ofrece reintento tras acertar o agotar el maximo`() {
        assertFalse(RetryPolicy.canRetry(listen, isCorrect = true, retryCount = 0))
        assertFalse(RetryPolicy.canRetry(listen, isCorrect = false, retryCount = RetryPolicy.MAX_RETRIES))
        assertFalse(RetryPolicy.canRetry(choice, isCorrect = false, retryCount = 0))
        assertFalse(RetryPolicy.canRetry(listen, isCorrect = false, retryCount = 0, allowRetry = false))
    }

    @Test
    fun `el primer fallo ofrece un reintento`() {
        assertTrue(RetryPolicy.canRetry(listen, isCorrect = false, retryCount = 0))
    }

    @Test
    fun `vida y xp se solicitan una sola vez`() {
        assertTrue(RetryPolicy.shouldRequestHeartConsumption(consumeHeart = true, alreadyConsumed = false))
        assertFalse(RetryPolicy.shouldRequestHeartConsumption(consumeHeart = true, alreadyConsumed = true))
        assertFalse(RetryPolicy.shouldRequestHeartConsumption(consumeHeart = false, alreadyConsumed = false))

        assertTrue(RetryPolicy.shouldAwardXp(isCorrect = true, alreadyAwarded = false))
        assertFalse(RetryPolicy.shouldAwardXp(isCorrect = true, alreadyAwarded = true))
        assertFalse(RetryPolicy.shouldAwardXp(isCorrect = false, alreadyAwarded = false))
    }
}

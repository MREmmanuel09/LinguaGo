package dev.linguago.feature.lesson

import org.junit.Assert.assertEquals
import org.junit.Test

class LessonScoringTest {
    @Test
    fun `dos errores de diez dan ochenta por ciento`() {
        val score = LessonScoring.score(total = 10, correct = 8)

        assertEquals(10, score.total)
        assertEquals(8, score.correct)
        assertEquals(2, score.incorrect)
        assertEquals(80, score.percentage)
    }

    @Test
    fun `sin ejercicios no hay nota`() {
        val score = LessonScoring.score(total = 0, correct = 0)

        assertEquals(0, score.total)
        assertEquals(0, score.incorrect)
        assertEquals(0, score.percentage)
    }

    @Test
    fun `redondea al entero mas cercano y acota valores`() {
        assertEquals(67, LessonScoring.score(total = 3, correct = 2).percentage)
        assertEquals(0, LessonScoring.score(total = 5, correct = -2).correct)
        assertEquals(100, LessonScoring.score(total = 5, correct = 99).percentage)
    }
}

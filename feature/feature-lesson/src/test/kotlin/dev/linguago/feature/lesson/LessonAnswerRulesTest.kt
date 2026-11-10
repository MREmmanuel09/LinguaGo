package dev.linguago.feature.lesson

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Reglas de avance neutro (B-02) y de fichas indexadas (B-06). */
class LessonAnswerRulesTest {
    @Test
    fun `omitir voz conserva combo y no mueve contadores`() {
        val effects =
            LessonAnswerRules.effects(
                skipped = true,
                isCorrect = false,
                firstGrading = true,
                combo = 7,
            )

        assertEquals(7, effects.nextCombo)
        assertEquals(0, effects.correctDelta)
        assertEquals(0, effects.incorrectDelta)
        assertFalse(effects.addMistake)
    }

    @Test
    fun `omitir voz no altera la nota de la leccion`() {
        val before = LessonScoring.score(total = 10, correct = 8)
        val effects =
            LessonAnswerRules.effects(
                skipped = true,
                isCorrect = false,
                firstGrading = true,
                combo = 3,
            )
        val after = LessonScoring.score(total = 10, correct = 8 + effects.correctDelta)

        assertEquals(before.percentage, after.percentage)
        assertEquals(before.incorrect, after.incorrect)
    }

    @Test
    fun `el ejercicio omitido no entra en el denominador de la nota`() {
        val score =
            LessonAnswerRules.score(
                totalExercises = 7,
                skipped = 1,
                correct = 5,
            )

        assertEquals(6, score.total)
        assertEquals(1, score.incorrect)
        assertEquals(83, score.percentage)
    }

    @Test
    fun `respuesta incorrecta normal rompe combo y suma un error`() {
        val effects =
            LessonAnswerRules.effects(
                skipped = false,
                isCorrect = false,
                firstGrading = true,
                combo = 4,
            )

        assertEquals(0, effects.nextCombo)
        assertEquals(0, effects.correctDelta)
        assertEquals(1, effects.incorrectDelta)
        assertTrue(effects.addMistake)
    }

    @Test
    fun `dos fichas con el mismo texto se seleccionan por separado`() {
        val board =
            TokenBoard(
                available =
                    listOf(
                        IndexedToken(0, "la"),
                        IndexedToken(1, "la"),
                        IndexedToken(2, "casa"),
                    ),
                chosen = emptyList(),
            )

        val first = TokenBoardRules.toggle(0, board)
        assertEquals(listOf(0), first.chosen.map { token -> token.id })

        val second = TokenBoardRules.toggle(1, first)
        assertEquals(listOf(0, 1), second.chosen.map { token -> token.id })
        assertEquals(listOf(2), second.available.map { token -> token.id })
    }

    @Test
    fun `quitar una ficha repetida devuelve solo esa al tablero`() {
        val board =
            TokenBoard(
                available = listOf(IndexedToken(2, "casa")),
                chosen =
                    listOf(
                        IndexedToken(0, "la"),
                        IndexedToken(1, "la"),
                    ),
            )

        val result = TokenBoardRules.toggle(1, board)

        assertEquals(listOf(0), result.chosen.map { token -> token.id })
        assertEquals(listOf(2, 1), result.available.map { token -> token.id })
    }
}

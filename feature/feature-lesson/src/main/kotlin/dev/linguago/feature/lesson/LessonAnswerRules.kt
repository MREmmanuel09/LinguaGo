package dev.linguago.feature.lesson

/** Efectos de una respuesta sobre combo, contadores y lista de errores. */
internal data class AnswerEffects(
    val nextCombo: Int,
    val correctDelta: Int,
    val incorrectDelta: Int,
    val addMistake: Boolean,
)

/**
 * Reglas de avance de una respuesta (B-02, I3): omitir un ejercicio de voz es
 * neutro — conserva el combo, no mueve los contadores de nota ni añade el
 * ejercicio a la revisión de errores. El resto de respuestas delega en
 * [ComboRules] y en el primer grading del ejercicio.
 */
internal object LessonAnswerRules {
    fun effects(
        skipped: Boolean,
        isCorrect: Boolean,
        firstGrading: Boolean,
        combo: Int,
    ): AnswerEffects =
        if (skipped) {
            AnswerEffects(
                nextCombo = combo,
                correctDelta = 0,
                incorrectDelta = 0,
                addMistake = false,
            )
        } else {
            AnswerEffects(
                nextCombo = ComboRules.next(combo, isCorrect),
                correctDelta = if (firstGrading && isCorrect) 1 else 0,
                incorrectDelta = if (firstGrading && !isCorrect) 1 else 0,
                addMistake = firstGrading && !isCorrect,
            )
        }

    /**
     * Nota de la lección excluyendo los ejercicios omitidos (B-02): saltar voz
     * no reduce el porcentaje ni infla el denominador.
     */
    fun score(
        totalExercises: Int,
        skipped: Int,
        correct: Int,
    ): LessonScore = LessonScoring.score((totalExercises - skipped).coerceAtLeast(0), correct)
}

/** Tablero de fichas separado en disponibles y elegidas. */
internal data class TokenBoard(
    val available: List<IndexedToken>,
    val chosen: List<IndexedToken>,
)

/**
 * Alterna una ficha por [IndexedToken.id], no por texto (B-06, D-03): dos
 * ocurrencias de la misma palabra son fichas independientes y se pueden
 * seleccionar, reordenar y quitar por separado.
 */
internal object TokenBoardRules {
    fun toggle(
        id: Int,
        board: TokenBoard,
    ): TokenBoard {
        val chosen = board.chosen.firstOrNull { token -> token.id == id }
        if (chosen != null) {
            return TokenBoard(
                available = board.available + chosen,
                chosen = board.chosen - chosen,
            )
        }
        val token = board.available.firstOrNull { candidate -> candidate.id == id } ?: return board
        return TokenBoard(
            available = board.available - token,
            chosen = board.chosen + token,
        )
    }
}

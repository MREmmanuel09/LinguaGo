package dev.linguago.feature.lesson

import kotlin.math.roundToInt

/** Resultado de la lección sobre el primer intento de cada ejercicio (ADR-036). */
data class LessonScore(
    val total: Int = 0,
    val correct: Int = 0,
    val incorrect: Int = 0,
    val percentage: Int = 0,
)

/** Cálculo determinista de la nota final en porcentaje (ADR-036). */
object LessonScoring {
    fun score(
        total: Int,
        correct: Int,
    ): LessonScore {
        val safeTotal = total.coerceAtLeast(0)
        val safeCorrect = correct.coerceIn(0, safeTotal)
        val incorrect = (safeTotal - safeCorrect).coerceAtLeast(0)
        val percentage =
            if (safeTotal == 0) {
                0
            } else {
                ((safeCorrect * 100.0) / safeTotal).roundToInt()
            }
        return LessonScore(
            total = safeTotal,
            correct = safeCorrect,
            incorrect = incorrect,
            percentage = percentage,
        )
    }
}

package dev.linguago.core.domain.placement

import dev.linguago.core.domain.logic.ExerciseGrader
import dev.linguago.core.domain.model.CefrLevel
import dev.linguago.core.domain.model.Course
import dev.linguago.core.domain.model.LessonId
import dev.linguago.core.domain.usecase.ExerciseAnswer

/**
 * Evalúa respuestas de un examen de nivelación y calcula el desbloqueo
 * monótono por niveles CEFR (ADR-058). Cada bloque se evalúa de forma
 * independiente: no hace falta aprobar todo el examen para desbloquear lo
 * demostrado, y el nivel alcanzado es el bloque aprobado más alto.
 */
public object PlacementEvaluator {
    /** Porcentaje mínimo para considerar aprobado un bloque CEFR. */
    public const val DEFAULT_PASS_PERCENT: Int = 70

    /**
     * [answersByQuestionKey] indexa por (levelOrdinal, questionIndex) para
     * evitar depender de IDs de ejercicio que podrían repetirse entre cursos.
     */
    public fun evaluate(
        exam: PlacementExam,
        answers: Map<Pair<Int, Int>, ExerciseAnswer>,
        course: Course,
        passPercent: Int = DEFAULT_PASS_PERCENT,
    ): PlacementResult {
        val threshold = passPercent.coerceIn(1, 100)
        val levelResults = mutableListOf<PlacementLevelResult>()
        var highestPassed: CefrLevel? = null
        var totalCorrect = 0
        var totalQuestions = 0
        var totalAttempted = 0

        // Los bloques se evalúan de forma independiente: un bloque sin responder
        // cuenta como 0 % y no bloquea el resto (el examen puede cerrarse antes
        // de A1→C2). El nivel alcanzado es el bloque aprobado más alto que el
        // usuario haya demostrado, no una cadena obligatoria desde A1.
        exam.blocks.forEachIndexed { blockIndex, block ->
            var correct = 0
            var attemptedQuestions = 0
            block.questions.forEachIndexed { questionIndex, question ->
                val key = blockIndex to questionIndex
                val answer = answers[key]
                if (answer != null) attemptedQuestions++
                val isCorrect =
                    if (answer == null) {
                        false
                    } else {
                        ExerciseGrader.isCorrect(question.exercise, answer)
                    }
                if (isCorrect) correct++
            }
            val total = block.questions.size
            val percent = if (total == 0) 0 else (correct * 100 / total)
            // Con cierre anticipado se exige cobertura suficiente del bloque
            // para que un par de aciertos no desbloquee un nivel entero.
            val minAttempted = if (total == 0) 1 else (total * threshold + 99) / 100
            val passed = total > 0 && attemptedQuestions >= minAttempted && percent >= threshold
            if (passed) highestPassed = block.level
            totalCorrect += correct
            totalQuestions += total
            totalAttempted += attemptedQuestions
            levelResults +=
                PlacementLevelResult(
                    level = block.level,
                    correct = correct,
                    total = total,
                    passed = passed,
                )
        }

        val achieved = highestPassed

        val unlocked: List<LessonId> =
            if (achieved != null) {
                PlacementExamGenerator.lessonsUpTo(course, achieved)
            } else {
                emptyList()
            }

        return PlacementResult(
            courseId = exam.courseId,
            levelResults = levelResults,
            achievedLevel = achieved,
            unlockedLessonIds = unlocked,
            totalCorrect = totalCorrect,
            totalQuestions = totalQuestions,
            totalAttempted = totalAttempted,
        )
    }
}

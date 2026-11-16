package dev.linguago.core.domain.placement

import dev.linguago.core.domain.logic.ExerciseGrader
import dev.linguago.core.domain.model.CefrLevel
import dev.linguago.core.domain.model.Course
import dev.linguago.core.domain.model.LessonId
import dev.linguago.core.domain.usecase.ExerciseAnswer

/**
 * Evalúa respuestas de un examen de nivelación y calcula el desbloqueo
 * monótono por niveles CEFR consecutivos (ADR-058).
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
        var achieved: CefrLevel? = null
        var canContinue = true
        var totalCorrect = 0
        var totalQuestions = 0

        exam.blocks.forEachIndexed { blockIndex, block ->
            var correct = 0
            block.questions.forEachIndexed { questionIndex, question ->
                val key = blockIndex to questionIndex
                val answer = answers[key]
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
            val passed = canContinue && total > 0 && percent >= threshold
            if (passed) {
                achieved = block.level
            } else {
                canContinue = false
            }
            totalCorrect += correct
            totalQuestions += total
            levelResults +=
                PlacementLevelResult(
                    level = block.level,
                    correct = correct,
                    total = total,
                    passed = passed,
                )
        }

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
        )
    }
}

package dev.linguago.core.domain.placement

import dev.linguago.core.domain.model.CefrLevel
import dev.linguago.core.domain.model.Exercise
import dev.linguago.core.domain.model.LessonId

/** Pregunta de un examen de nivelación, tomada del contenido existente (ADR-058). */
public data class PlacementQuestion(
    public val lessonId: LessonId,
    public val cefrLevel: CefrLevel,
    public val exercise: Exercise,
)

/** Bloque de preguntas por nivel CEFR, presentado en orden fácil→difícil. */
public data class PlacementLevelBlock(
    public val level: CefrLevel,
    public val questions: List<PlacementQuestion>,
)

/** Examen de nivelación generado dinámicamente para un curso. */
public data class PlacementExam(
    public val courseId: String,
    public val blocks: List<PlacementLevelBlock>,
) {
    public val totalQuestions: Int get() = blocks.sumOf { it.questions.size }
    public val allQuestions: List<PlacementQuestion> get() = blocks.flatMap { it.questions }
}

/** Resultado por bloque/nivel CEFR. */
public data class PlacementLevelResult(
    public val level: CefrLevel,
    public val correct: Int,
    public val total: Int,
    public val passed: Boolean,
) {
    public val percentage: Int get() = if (total == 0) 0 else (correct * 100 / total)
}

/** Resultado global del examen y nivel más alto demostrado (ADR-058). */
public data class PlacementResult(
    public val courseId: String,
    public val levelResults: List<PlacementLevelResult>,
    public val achievedLevel: CefrLevel?,
    public val unlockedLessonIds: List<LessonId>,
    public val totalCorrect: Int,
    public val totalQuestions: Int,
    public val totalAttempted: Int = totalQuestions,
) {
    public val percentage: Int get() = if (totalAttempted == 0) 0 else (totalCorrect * 100 / totalAttempted)
}

package dev.linguago.core.domain.usecase

import dev.linguago.core.common.Result
import dev.linguago.core.domain.model.Course
import dev.linguago.core.domain.model.CourseId
import dev.linguago.core.domain.model.CourseSummary
import dev.linguago.core.domain.model.Exercise
import dev.linguago.core.domain.model.Lesson
import dev.linguago.core.domain.model.LessonId
import dev.linguago.core.domain.model.StreakState
import dev.linguago.core.domain.model.WordProgress

/**
 * Use cases clave del dominio (RFC §3.2). Cada interface define el contrato
 * operable por los ViewModels; la implementación vive en los módulos core
 * y es independiente de Android (I7).
 */
public interface LoadCourseUseCase {
    /** Carga completa del curso solicitado (assets → dominio). */
    public suspend operator fun invoke(courseId: CourseId): Result<Course>
}

public interface GetAvailableCoursesUseCase {
    /** Lista ligera de los cursos empaquetados en la app. */
    public suspend operator fun invoke(): Result<List<CourseSummary>>
}

public interface StartLessonUseCase {
    /** Abre la lección del curso indicado y devuelve su cola de ejercicios. */
    public suspend operator fun invoke(
        courseId: CourseId,
        lessonId: LessonId,
    ): Result<Lesson>
}

public interface SubmitAnswerUseCase {
    /**
     * Valida la respuesta por tipo de ejercicio, contabiliza XP (+10 por
     * respuesta correcta, RFC §3.2) y devuelve `true` si es correcta.
     */
    public suspend operator fun invoke(
        exercise: Exercise,
        answer: ExerciseAnswer,
    ): Result<Boolean>
}

public interface CompleteLessonUseCase {
    /** Persiste la finalización de la lección: progreso, XP total y racha. */
    public suspend operator fun invoke(
        lessonId: LessonId,
        xp: Int,
    ): Result<Unit>
}

public interface UpdateStreakUseCase {
    /** Aplica la regla de racha tras completar una lección hoy. */
    public suspend operator fun invoke(): Result<StreakState>
}

public interface GetNextReviewUseCase {
    /** Selecciona vocabulario para repaso espaciado (FSRS, Fase 2). */
    public suspend operator fun invoke(limit: Int): Result<List<WordProgress>>
}

/**
 * Respuestas discriminadas por tipo de ejercicio; el SubmitAnswerUseCase
 * resuelve la validación según el par (Exercise, ExerciseAnswer).
 */
public sealed interface ExerciseAnswer {
    /** Índice de opción elegida (MultipleChoice, ListenChoose, KanaChoose…). */
    public data class OptionPicked(
        val index: Int,
    ) : ExerciseAnswer

    /** Lista de tokens ordenados (Translate, OrderWords). */
    public data class TokenSequence(
        val tokens: List<String>,
    ) : ExerciseAnswer

    /** Mapa de emparejamiento izquierda→derecha en índices canónicos de pares. */
    public data class MatchedPairs(
        val leftToRight: Map<Int, Int>,
    ) : ExerciseAnswer

    /** Texto escrito libremente (ListenType). */
    public data class TypedText(
        val text: String,
    ) : ExerciseAnswer

    /** Transcripción reconocida del habla (SpeakRepeat, Fase 2). */
    public data class SpokenText(
        val transcript: String,
    ) : ExerciseAnswer
}

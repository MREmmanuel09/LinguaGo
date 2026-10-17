package dev.linguago.core.content

import dev.linguago.core.content.CourseValidation.Invalid
import dev.linguago.core.domain.model.Course
import dev.linguago.core.domain.model.Exercise
import dev.linguago.core.domain.model.Lesson

/**
 * Validador estructural del contenido pedagógico (Fase 1). Comprueba la
 * integridad que el .json no puede expresar con tipos: IDs únicos, índices
 * correctos dentro de rango, respuestas presentes en opciones, subconjuntos
 * de tokens, pares sin duplicados. Valida TODOS los ejercicios de forma
 * exhaustiva en la carga (fail-fast del contenido, no del usuario).
 */
public sealed interface CourseValidation {
    public data object Valid : CourseValidation

    public data class Invalid(
        val reason: String,
    ) : CourseValidation
}

public object CourseValidator {
    private const val tokenSeparator = " "

    public fun validate(course: Course): CourseValidation {
        if (course.levels.isEmpty()) return Invalid("Curso '${course.id.value}' sin niveles")

        val exerciseIds = mutableSetOf<String>()
        val levelIds = mutableSetOf<String>()
        for (level in course.levels) {
            if (!levelIds.add(level.id.value)) {
                return Invalid("Nivel duplicado '${level.id.value}'")
            }
            if (level.units.isEmpty()) {
                return Invalid("Nivel '${level.id.value}' sin unidades")
            }
            val unitIds = mutableSetOf<String>()
            for (unit in level.units) {
                if (!unitIds.add(unit.id.value)) {
                    return Invalid("Unidad duplicada '${unit.id.value}' en nivel '${level.id.value}'")
                }
                if (unit.lessons.isEmpty()) {
                    return Invalid("Unidad '${unit.id.value}' sin lecciones")
                }
                val lessonIds = mutableSetOf<String>()
                for (lesson in unit.lessons) {
                    if (!lessonIds.add(lesson.id.value)) {
                        return Invalid("Lección duplicada '${lesson.id.value}' en unidad '${unit.id.value}'")
                    }
                    if (lesson.exercises.isEmpty()) {
                        return Invalid("Lección '${lesson.id.value}' sin ejercicios")
                    }
                    for (exercise in lesson.exercises) {
                        if (!exerciseIds.add(exercise.id.value)) {
                            return Invalid("Ejercicio duplicado '${exercise.id.value}' en lección '${lesson.id.value}'")
                        }
                        val exerciseFailure = validateExercise(lesson, exercise)
                        if (exerciseFailure != null) return exerciseFailure
                    }
                }
            }
        }
        return CourseValidation.Valid
    }

    private fun validateExercise(
        lesson: Lesson,
        exercise: Exercise,
    ): CourseValidation.Invalid? =
        when (exercise) {
            is Exercise.MultipleChoice ->
                requirePresentIndex(
                    exercise.options,
                    exercise.correctIndex,
                    exercise.id.value,
                )
            is Exercise.ListenChoose ->
                requirePresentIndex(
                    exercise.options,
                    exercise.correctIndex,
                    exercise.id.value,
                ) ?: validateNotBlank(exercise.audioText, exercise)
            is Exercise.KanaChoose ->
                requireWithin(
                    exercise.correctRomaji,
                    exercise.romajiOptions,
                    exercise.id.value,
                )
            is Exercise.KanjiReading ->
                requireWithin(
                    exercise.correctReading,
                    exercise.readingOptions,
                    exercise.id.value,
                )
            is Exercise.ParticleFill ->
                requireWithin(
                    exercise.correctParticle,
                    exercise.particles,
                    exercise.id.value,
                )
            is Exercise.Translate -> validateTranslate(exercise)
            is Exercise.OrderWords -> validateOrderWords(exercise)
            is Exercise.MatchPairs -> validateMatchPairs(exercise)
            is Exercise.ListenType -> validateNotBlank(exercise.transcription, exercise)
            is Exercise.SpeakRepeat -> validateNotBlank(exercise.targetPhrase, exercise)
        }

    private fun requirePresentIndex(
        options: List<String>,
        correctIndex: Int,
        exerciseId: String,
    ): CourseValidation.Invalid? =
        if (options.isEmpty() || correctIndex !in options.indices) {
            Invalid(
                "Ejercicio '$exerciseId' requiere correctIndex en rango de " +
                    "opciones no vacías (correctIndex=$correctIndex, opciones=${options.size})",
            )
        } else {
            null
        }

    private fun requireWithin(
        correct: String,
        options: List<String>,
        exerciseId: String,
    ): CourseValidation.Invalid? =
        if (options.isEmpty() || correct !in options) {
            Invalid(
                "Ejercicio '$exerciseId' requiere la respuesta correcta entre " +
                    "opciones no vacías ('$correct' no está en ${options.size} opciones)",
            )
        } else {
            null
        }

    private fun validateTranslate(exercise: Exercise.Translate): CourseValidation.Invalid? {
        val correct = exercise.correctTokens
        val distractors = exercise.distractorTokens
        if (correct.isEmpty() || correct.any { it.isBlank() }) {
            return Invalid("Ejercicio '${exercise.id.value}' requiere tokens correctos no vacíos y no vacíos en sí")
        }
        val overlap = correct.intersect(distractors.toSet())
        if (overlap.isNotEmpty()) {
            return Invalid(
                "Ejercicio '${exercise.id.value}' no permite distractores que coincidan " +
                    "con los tokens correctos ($overlap)",
            )
        }
        return null
    }

    private fun validateOrderWords(exercise: Exercise.OrderWords): CourseValidation.Invalid? {
        val answer = exercise.answerTokens
        if (answer.isEmpty() || answer.any { it.isBlank() }) {
            return Invalid("Ejercicio '${exercise.id.value}' requiere answerTokens no vacíos")
        }
        if (exercise.tokens.isEmpty()) {
            return Invalid("Ejercicio '${exercise.id.value}' requiere tokens no vacíos")
        }
        val available = exercise.tokens.groupingBy { it }.eachCount()
        val needed = answer.groupingBy { it }.eachCount()
        val missing =
            needed.entries
                .filter { entry -> entry.value > (available[entry.key] ?: 0) }
                .map { entry -> "'${entry.key}'×${entry.value}" }
                .joinToString(tokenSeparator)
        if (missing.isNotEmpty()) {
            return Invalid(
                "Ejercicio '${exercise.id.value}' requiere answerTokens ⊆ tokens " +
                    "(faltan: $missing)",
            )
        }
        return null
    }

    private fun validateMatchPairs(exercise: Exercise.MatchPairs): CourseValidation.Invalid? {
        val pairs = exercise.pairs
        if (pairs.isEmpty()) {
            return Invalid("Ejercicio '${exercise.id.value}' requiere al menos un par")
        }
        if (pairs.any { pair -> pair.left.isBlank() || pair.right.isBlank() }) {
            return Invalid("Ejercicio '${exercise.id.value}' requiere pares con lados no vacíos")
        }
        val lefts = pairs.map { it.left }
        val rights = pairs.map { it.right }
        if (lefts.distinct().size != pairs.size || rights.distinct().size != pairs.size) {
            return Invalid("Ejercicio '${exercise.id.value}' requiere lados únicos por par")
        }
        return null
    }

    private fun validateNotBlank(
        value: String,
        exercise: Exercise,
    ): CourseValidation.Invalid? =
        if (value.isBlank()) {
            Invalid("Ejercicio '${exercise.id.value}' requiere un enunciado de audio no vacío")
        } else {
            null
        }
}

package dev.linguago.core.content

import dev.linguago.core.content.CourseValidation.Invalid
import dev.linguago.core.domain.model.CefrLevel
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
    private val PLACEHOLDER_PATTERN = Regex("\\{\\{([^}]+)\\}\\}")

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
                    val pedagogyFailure = validateLessonPedagogy(lesson)
                    if (pedagogyFailure != null) return pedagogyFailure
                    val microFailure = validateMicroLesson(lesson)
                    if (microFailure != null) return microFailure
                }
            }
        }
        return CourseValidation.Valid
    }

    /** Valida la micro-clase de gramática/reglas si la lección la declara (ADR-028). */
    private fun validateMicroLesson(lesson: Lesson): CourseValidation.Invalid? {
        val micro = lesson.microLesson ?: return null
        if (micro.title.isBlank()) {
            return Invalid("Lección '${lesson.id.value}' requiere microLesson.title no vacío")
        }
        if (micro.goals.isEmpty() || micro.goals.any { it.isBlank() }) {
            return Invalid("Lección '${lesson.id.value}' requiere microLesson.goals no vacíos")
        }
        if (micro.sections.isEmpty()) {
            return Invalid("Lección '${lesson.id.value}' requiere al menos una sección en microLesson")
        }
        for (section in micro.sections) {
            if (section.heading.isBlank() || section.body.isBlank()) {
                return Invalid("Lección '${lesson.id.value}' requiere microLesson.sections con heading y body")
            }
            if (section.examples.any { example ->
                    example.source.isBlank() ||
                        example.target.isBlank() ||
                        example.note?.isBlank() == true
                }
            ) {
                return Invalid("Lección '${lesson.id.value}' requiere microLesson.examples con source/target")
            }
        }
        return null
    }

    /**
     * Regla de No-Aplanamiento Pedagógico (ADR-018): una lección B1/B2 debe
     * declarar su escenario, can-do, gramática, banda léxica y notas
     * culturales, además de feedback de error en cada ejercicio y al menos
     * una producción oral libre. El feedback de acierto es opcional, pero si
     * existe debe ser coherente con las reglas de la lección.
     */
    private fun validateLessonPedagogy(lesson: Lesson): CourseValidation.Invalid? {
        val requiresCefrMetadata =
            lesson.cefrLevel == CefrLevel.B1 || lesson.cefrLevel == CefrLevel.B2
        if (requiresCefrMetadata) {
            if (lesson.scenario.isNullOrBlank()) {
                return Invalid("Lección '${lesson.id.value}' B1/B2 requiere scenario")
            }
            if (lesson.canDo.isNullOrBlank()) {
                return Invalid("Lección '${lesson.id.value}' B1/B2 requiere canDo")
            }
            if (lesson.grammarPoints.isEmpty()) {
                return Invalid("Lección '${lesson.id.value}' B1/B2 requiere grammarPoints no vacío")
            }
            if (lesson.vocabFrequencyBand == null) {
                return Invalid("Lección '${lesson.id.value}' B1/B2 requiere vocabFrequencyBand")
            }
            if (lesson.culturalNotes.isEmpty()) {
                return Invalid("Lección '${lesson.id.value}' B1/B2 requiere al menos una culturalNote")
            }
            val exerciseWithoutFeedback = lesson.exercises.firstOrNull { it.feedbackOnError == null }
            if (exerciseWithoutFeedback != null) {
                return Invalid(
                    "Lección '${lesson.id.value}' B1/B2 requiere feedbackOnError en todos los ejercicios " +
                        "(falta en '${exerciseWithoutFeedback.id.value}')",
                )
            }
            if (lesson.exercises.none { it is Exercise.FreeSpeakingPrompt }) {
                return Invalid("Lección '${lesson.id.value}' B1/B2 requiere al menos un FreeSpeakingPrompt")
            }
        }

        if (lesson.scenario?.isBlank() == true) {
            return Invalid("Lección '${lesson.id.value}' requiere scenario no vacío si se declara")
        }
        if (lesson.canDo?.isBlank() == true) {
            return Invalid("Lección '${lesson.id.value}' requiere canDo no vacío si se declara")
        }
        if (lesson.grammarPoints.any { point -> point.id.value.isBlank() || point.label.isBlank() }) {
            return Invalid("Lección '${lesson.id.value}' requiere grammarPoints con id y label no vacíos")
        }
        if (lesson.grammarPoints
                .map { it.id }
                .distinct()
                .size != lesson.grammarPoints.size
        ) {
            return Invalid("Lección '${lesson.id.value}' requiere ids de grammarPoints únicos")
        }
        if (lesson.culturalNotes.any { note ->
                note.idiom.isBlank() || note.note.isBlank() || note.translation?.isBlank() == true
            }
        ) {
            return Invalid("Lección '${lesson.id.value}' requiere culturalNotes con idioma y nota no vacíos")
        }
        val grammarIds = lesson.grammarPoints.map { it.id }.toSet()
        for (exercise in lesson.exercises) {
            val feedback = exercise.feedbackOnError
            if (feedback != null && feedback.explanation.isBlank()) {
                return Invalid("Ejercicio '${exercise.id.value}' requiere feedbackOnError con explicación")
            }
            val ruleRef = feedback?.ruleRef
            if (ruleRef != null && ruleRef !in grammarIds) {
                return Invalid(
                    "Ejercicio '${exercise.id.value}' referencia ruleRef '${ruleRef.value}' " +
                        "que no existe en grammarPoints",
                )
            }
            val success = exercise.feedbackOnSuccess
            if (success != null && success.explanation.isBlank()) {
                return Invalid("Ejercicio '${exercise.id.value}' requiere feedbackOnSuccess con explicación")
            }
            val successRuleRef = success?.ruleRef
            if (successRuleRef != null && successRuleRef !in grammarIds) {
                return Invalid(
                    "Ejercicio '${exercise.id.value}' referencia feedbackOnSuccess.ruleRef '${successRuleRef.value}' " +
                        "que no existe en grammarPoints",
                )
            }
            if (exercise is Exercise.FreeSpeakingPrompt) {
                val missingRef = exercise.grammarRuleRefs.firstOrNull { it !in grammarIds }
                if (missingRef != null) {
                    return Invalid(
                        "Ejercicio '${exercise.id.value}' referencia grammarRuleRefs '${missingRef.value}' " +
                            "que no existe en grammarPoints",
                    )
                }
            }
        }
        return null
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
            is Exercise.AudioOnlyPrompt -> validateAudioOnlyPrompt(exercise)
            is Exercise.SemanticMatch -> validateSemanticMatch(exercise)
            is Exercise.ClozeDragDrop -> validateClozeDragDrop(exercise)
            is Exercise.FreeSpeakingPrompt -> validateFreeSpeakingPrompt(exercise)
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

    private fun validateAudioOnlyPrompt(exercise: Exercise.AudioOnlyPrompt): CourseValidation.Invalid? {
        if (exercise.audioText.isBlank()) {
            return Invalid("Ejercicio '${exercise.id.value}' requiere un enunciado de audio no vacío")
        }
        if (exercise.question?.isBlank() == true) {
            return Invalid("Ejercicio '${exercise.id.value}' requiere una pregunta no vacía si se declara")
        }
        return requirePresentIndex(exercise.options, exercise.correctIndex, exercise.id.value)
    }

    private fun validateSemanticMatch(exercise: Exercise.SemanticMatch): CourseValidation.Invalid? {
        val pairs = exercise.pairs
        if (pairs.isEmpty()) {
            return Invalid("Ejercicio '${exercise.id.value}' requiere al menos un par semántico")
        }
        if (pairs.any { pair -> pair.left.isBlank() || pair.right.isBlank() }) {
            return Invalid("Ejercicio '${exercise.id.value}' requiere pares semánticos con lados no vacíos")
        }
        if (pairs.map { it.left }.distinct().size != pairs.size || pairs.map { it.right }.distinct().size != pairs.size) {
            return Invalid("Ejercicio '${exercise.id.value}' requiere lados semánticos únicos por par")
        }
        if (exercise.instructions?.isBlank() == true || pairs.any { it.nuance?.isBlank() == true }) {
            return Invalid("Ejercicio '${exercise.id.value}' requiere instrucciones y matices no vacíos si se declaran")
        }
        return null
    }

    private fun validateClozeDragDrop(exercise: Exercise.ClozeDragDrop): CourseValidation.Invalid? {
        if (exercise.passage.isBlank()) {
            return Invalid("Ejercicio '${exercise.id.value}' requiere un texto de cloze no vacío")
        }
        val blanks = exercise.blanks
        if (blanks.isEmpty()) {
            return Invalid("Ejercicio '${exercise.id.value}' requiere al menos un hueco")
        }
        if (blanks.any { blank -> !isCompleteBlank(blank) }) {
            return Invalid("Ejercicio '${exercise.id.value}' requiere huecos con respuesta dentro de sus opciones")
        }
        if (blanks.map { it.id }.distinct().size != blanks.size) {
            return Invalid("Ejercicio '${exercise.id.value}' requiere ids de hueco únicos")
        }
        val placeholders =
            PLACEHOLDER_PATTERN
                .findAll(exercise.passage)
                .map { match -> match.groupValues[1] }
                .toSet()
        if (placeholders != blanks.map { it.id }.toSet()) {
            return Invalid(
                "Ejercicio '${exercise.id.value}' requiere que los huecos {{id}} del texto " +
                    "coincidan exactamente con blanks[]",
            )
        }
        return null
    }

    private fun isCompleteBlank(blank: Exercise.ClozeBlank): Boolean {
        if (blank.id.isBlank() || blank.answer.isBlank()) return false
        return blank.options.isNotEmpty() && blank.answer in blank.options
    }

    private fun validateFreeSpeakingPrompt(exercise: Exercise.FreeSpeakingPrompt): CourseValidation.Invalid? {
        if (exercise.prompt.isBlank() || exercise.modelAnswer.isBlank()) {
            return Invalid("Ejercicio '${exercise.id.value}' requiere consigna y respuesta modelo no vacías")
        }
        if (exercise.minTokens < 1) {
            return Invalid("Ejercicio '${exercise.id.value}' requiere minTokens >= 1")
        }
        if (exercise.conceptClusters.isEmpty() ||
            exercise.conceptClusters.any { cluster -> cluster.isEmpty() || cluster.any { it.isBlank() } }
        ) {
            return Invalid("Ejercicio '${exercise.id.value}' requiere conceptClusters no vacíos y sin términos en blanco")
        }
        if (exercise.selfAssessmentCriteria.isEmpty() || exercise.selfAssessmentCriteria.any { it.isBlank() }) {
            return Invalid("Ejercicio '${exercise.id.value}' requiere criterios de autoevaluación no vacíos")
        }
        val minWpm = exercise.minWpm
        val maxWpm = exercise.maxWpm
        if (minWpm != null && maxWpm != null && minWpm > maxWpm) {
            return Invalid("Ejercicio '${exercise.id.value}' requiere minWpm <= maxWpm")
        }
        if (exercise.timeLimitSeconds != null && exercise.timeLimitSeconds !in 10..300) {
            return Invalid("Ejercicio '${exercise.id.value}' requiere timeLimitSeconds entre 10 y 300")
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

package dev.linguago.core.domain.logic

import dev.linguago.core.domain.model.Exercise
import dev.linguago.core.domain.usecase.ExerciseAnswer

/**
 * Evaluador de respuestas del motor de aprendizaje (Fase 1). Es una función
 * pura, testeable sin entorno (I7): decide si la respuesta dada es correcta
 * para el ejercicio, sin efectos y sin excepciones. Las respuestas con un
 * tipo inesperado para el ejercicio evaluado se consideran incorrectas.
 */
public object ExerciseGrader {
    public fun isCorrect(
        exercise: Exercise,
        answer: ExerciseAnswer,
    ): Boolean =
        when (answer) {
            is ExerciseAnswer.OptionPicked -> gradeOptionPicked(exercise, answer.index)
            is ExerciseAnswer.TokenSequence -> gradeTokenSequence(exercise, answer.tokens)
            is ExerciseAnswer.MatchedPairs -> gradeMatchedPairs(exercise, answer.leftToRight)
            is ExerciseAnswer.TypedText -> gradeTypoable(exercise, answer.text)
            is ExerciseAnswer.SpokenText -> gradeRepeatable(exercise, answer.transcript)
        }

    private fun gradeOptionPicked(
        exercise: Exercise,
        index: Int,
    ): Boolean =
        when (exercise) {
            is Exercise.MultipleChoice -> index == exercise.correctIndex
            is Exercise.ListenChoose -> index == exercise.correctIndex
            is Exercise.AudioOnlyPrompt -> index == exercise.correctIndex
            is Exercise.KanaChoose -> exercise.romajiOptions.getOrNull(index) == exercise.correctRomaji
            is Exercise.KanjiReading -> exercise.readingOptions.getOrNull(index) == exercise.correctReading
            is Exercise.ParticleFill -> exercise.particles.getOrNull(index) == exercise.correctParticle
            else -> false
        }

    private fun gradeTokenSequence(
        exercise: Exercise,
        tokens: List<String>,
    ): Boolean =
        when (exercise) {
            is Exercise.Translate -> normalize(tokens) == normalize(exercise.correctTokens)
            is Exercise.OrderWords -> normalize(tokens) == normalize(exercise.answerTokens)
            else -> false
        }

    /** Correcto si y solo si cada izquierda canónica `i` apunta a la derecha
     * canónica `i`: la UI envía la asignación ya remapeada a índices canónicos
     * de la lista de pares del ejercicio MatchPairs.
     */
    private fun gradeMatchedPairs(
        exercise: Exercise,
        leftToRight: Map<Int, Int>,
    ): Boolean =
        when (exercise) {
            is Exercise.MatchPairs ->
                exercise.pairs.indices.all { index ->
                    leftToRight[index] == index
                }
            is Exercise.SemanticMatch ->
                exercise.pairs.indices.all { index ->
                    leftToRight[index] == index
                }
            else -> false
        }

    private fun gradeTypoable(
        exercise: Exercise,
        text: String,
    ): Boolean =
        when (exercise) {
            is Exercise.ListenType -> normalize(text) == normalize(exercise.transcription)
            else -> false
        }

    private fun gradeRepeatable(
        exercise: Exercise,
        transcript: String,
    ): Boolean =
        when (exercise) {
            is Exercise.SpeakRepeat -> normalize(transcript) == normalize(exercise.targetPhrase)
            else -> false
        }

    /**
     * Normalización de frases libres: minúsculas, sin puntuación (incluida la
     * japonesa) y con espacios colapsados. Exige que ambas frases produzcan
     * la misma secuencia de palabras.
     */
    private fun normalize(text: String): String =
        text
            .lowercase()
            .replace(PUNCTUATION, " ")
            .replace(WHITESPACE, " ")
            .trim()

    private val PUNCTUATION = Regex("[\\p{P}]")
    private val WHITESPACE = Regex("\\s+")

    private fun normalize(tokens: List<String>): String = normalize(tokens.joinToString(" ").trim())
}

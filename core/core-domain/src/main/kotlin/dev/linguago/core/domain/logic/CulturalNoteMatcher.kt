package dev.linguago.core.domain.logic

import dev.linguago.core.domain.model.CulturalNote
import dev.linguago.core.domain.model.Exercise
import dev.linguago.core.domain.model.Lesson

/**
 * Selecciona la [CulturalNote] de la lección cuyo modismo aparece en el
 * texto visible del ejercicio (F2.2), para acompañar el feedback con su
 * explicación cultural cuando realmente aplica. Comparación sin distinguir
 * mayúsculas; la primera nota coincidente gana.
 */
public object CulturalNoteMatcher {
    /**
     * Devuelve la nota cultural aplicable al [exercise], o `null` si la
     * lección no define modismos o ninguno aparece en el ejercicio.
     */
    public fun noteFor(
        lesson: Lesson,
        exercise: Exercise,
    ): CulturalNote? {
        if (lesson.culturalNotes.isEmpty()) return null
        val texts = textsFor(exercise)
        return lesson.culturalNotes.firstOrNull { note ->
            note.idiom.isNotBlank() &&
                texts.any { text -> text.contains(note.idiom, ignoreCase = true) }
        }
    }

    /** Campos visibles del ejercicio donde puede aparecer un modismo. */
    private fun textsFor(exercise: Exercise): List<String> =
        when (exercise) {
            is Exercise.MultipleChoice -> listOf(exercise.question, exercise.options.joinToString(" "))
            is Exercise.Translate ->
                listOf(
                    exercise.sourcePhrase,
                    exercise.correctTokens.joinToString(" "),
                    exercise.distractorTokens.joinToString(" "),
                )
            is Exercise.MatchPairs -> exercise.pairs.map { pair -> "${pair.left} ${pair.right}" }
            is Exercise.OrderWords ->
                listOf(exercise.tokens.joinToString(" "), exercise.answerTokens.joinToString(" "))
            is Exercise.ListenChoose -> listOf(exercise.audioText, exercise.options.joinToString(" "))
            is Exercise.ListenType -> listOf(exercise.transcription, exercise.audioText)
            is Exercise.SpeakRepeat -> listOfNotNull(exercise.targetPhrase, exercise.audioText)
            is Exercise.KanaChoose ->
                listOf(exercise.kana, exercise.correctRomaji, exercise.romajiOptions.joinToString(" "))
            is Exercise.KanjiReading ->
                listOf(exercise.kanji, exercise.correctReading, exercise.readingOptions.joinToString(" "))
            is Exercise.ParticleFill ->
                listOf(
                    exercise.sentenceTemplate,
                    exercise.correctParticle,
                    exercise.particles.joinToString(" "),
                )
            is Exercise.AudioOnlyPrompt ->
                listOf(exercise.audioText, exercise.options.joinToString(" ")) + listOfNotNull(exercise.question)
            is Exercise.SemanticMatch -> exercise.pairs.map { pair -> "${pair.left} ${pair.right}" }
            is Exercise.ClozeDragDrop ->
                listOf(exercise.passage) +
                    exercise.blanks.map { blank ->
                        (listOf(blank.answer) + blank.options).joinToString(" ")
                    }
            is Exercise.FreeSpeakingPrompt ->
                listOf(
                    exercise.prompt,
                    exercise.modelAnswer,
                    exercise.conceptClusters.flatten().joinToString(" "),
                )
        }
}

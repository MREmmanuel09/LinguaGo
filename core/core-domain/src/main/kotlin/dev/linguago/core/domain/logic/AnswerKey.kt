package dev.linguago.core.domain.logic

import dev.linguago.core.domain.model.Exercise

/**
 * Respuesta canónica de un ejercicio, en forma estructurada y segura para
 * mostrar al alumno (ADR-028). No depende de la respuesta del usuario y no
 * se serializa: la UI decide cómo presentarla.
 */
public sealed interface AnswerKey {
    /** Opción correcta de un ejercicio de selección. */
    public data class Options(
        public val index: Int,
        public val text: String,
    ) : AnswerKey

    /** Secuencia correcta de tokens (traducción u ordenación). */
    public data class Tokens(
        public val tokens: List<String>,
    ) : AnswerKey

    /** Parejas correctas izquierda → derecha. */
    public data class Pairs(
        public val pairs: List<Pair<String, String>>,
    ) : AnswerKey

    /** Texto correcto de un dictado o una repetición. */
    public data class Text(
        public val text: String,
    ) : AnswerKey

    /** Respuesta modelo de una producción oral libre. */
    public data class Speaking(
        public val text: String,
    ) : AnswerKey

    /** Asignación correcta de cada hueco del cloze. */
    public data class Cloze(
        public val assignments: Map<String, String>,
    ) : AnswerKey
}

/** Devuelve la respuesta canónica del ejercicio para revelarla en el feedback. */
public fun Exercise.answerKey(): AnswerKey =
    when (this) {
        is Exercise.MultipleChoice -> AnswerKey.Options(correctIndex, options.getOrNull(correctIndex).orEmpty())
        is Exercise.Translate -> AnswerKey.Tokens(correctTokens)
        is Exercise.MatchPairs -> AnswerKey.Pairs(pairs.map { pair -> pair.left to pair.right })
        is Exercise.OrderWords -> AnswerKey.Tokens(answerTokens)
        is Exercise.ListenChoose -> AnswerKey.Options(correctIndex, options.getOrNull(correctIndex).orEmpty())
        is Exercise.ListenType -> AnswerKey.Text(transcription)
        is Exercise.SpeakRepeat -> AnswerKey.Text(targetPhrase)
        is Exercise.KanaChoose ->
            AnswerKey.Options(
                index = romajiOptions.indexOf(correctRomaji),
                text = correctRomaji,
            )
        is Exercise.KanjiReading ->
            AnswerKey.Options(
                index = readingOptions.indexOf(correctReading),
                text = correctReading,
            )
        is Exercise.ParticleFill ->
            AnswerKey.Options(
                index = particles.indexOf(correctParticle),
                text = correctParticle,
            )
        is Exercise.AudioOnlyPrompt -> AnswerKey.Options(correctIndex, options.getOrNull(correctIndex).orEmpty())
        is Exercise.SemanticMatch -> AnswerKey.Pairs(pairs.map { pair -> pair.left to pair.right })
        is Exercise.ClozeDragDrop -> AnswerKey.Cloze(blanks.associate { blank -> blank.id to blank.answer })
        is Exercise.FreeSpeakingPrompt -> AnswerKey.Speaking(modelAnswer)
    }

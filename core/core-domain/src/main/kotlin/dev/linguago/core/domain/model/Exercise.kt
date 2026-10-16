package dev.linguago.core.domain.model

/**
 * Tipos de ejercicio del sistema (RFC §3.1). Cada tipo queda modelado como
 * data class con parámetros completos y autocodificables en contenido JSON.
 * La validación estructural (respuestas correctas dentro de opciones,
 * tokens únicos, etc.) la hace el contenido validador (core-content, Fase 1).
 */
public sealed interface Exercise {
    public val id: ExerciseId
    public val audioText: String?

    /** Elección única entre opciones con respuesta correcta única. */
    public data class MultipleChoice(
        override val id: ExerciseId,
        val question: String,
        val options: List<String>,
        val correctIndex: Int,
        override val audioText: String? = null,
    ) : Exercise

    /** Traducción ensamblando tokens de respuesta/distraídos. */
    public data class Translate(
        override val id: ExerciseId,
        val sourcePhrase: String,
        val correctTokens: List<String>,
        val distractorTokens: List<String> = emptyList(),
        override val audioText: String? = null,
    ) : Exercise

    /** Emparejar conceptos (palabra↔traducción, kana↔romaji, ...). */
    public data class MatchPairs(
        override val id: ExerciseId,
        /** pares (izquierda, derecha) asociados por identidad — la UI los desordena. */
        val pairs: List<Pair<String, String>>,
        override val audioText: String? = null,
    ) : Exercise

    /** Ordenar tokens para reconstruir una frase objetivo. */
    public data class OrderWords(
        override val id: ExerciseId,
        val tokens: List<String>,
        val answerTokens: List<String>,
        override val audioText: String? = null,
    ) : Exercise

    /** Escuchar y elegir la transcripción entre opciones. */
    public data class ListenChoose(
        override val id: ExerciseId,
        val options: List<String>,
        val correctIndex: Int,
        /** Ejercicio donde el enunciado es obligatoriamente el audio. */
        override val audioText: String,
    ) : Exercise

    /** Escuchar y escribir la frase exacta (dictado). */
    public data class ListenType(
        override val id: ExerciseId,
        val transcription: String,
        override val audioText: String,
    ) : Exercise

    /** Repetir en voz alta una frase objetivo (STT, Fase 2). */
    public data class SpeakRepeat(
        override val id: ExerciseId,
        val targetPhrase: String,
        override val audioText: String? = null,
    ) : Exercise

    /** Reconocer kana japonés (romaji entre opciones). */
    public data class KanaChoose(
        override val id: ExerciseId,
        val kana: String,
        val romajiOptions: List<String>,
        val correctRomaji: String,
        override val audioText: String? = null,
    ) : Exercise

    /** Reconocer lectura de un kanji entre opciones. */
    public data class KanjiReading(
        override val id: ExerciseId,
        val kanji: String,
        val readingOptions: List<String>,
        val correctReading: String,
        override val audioText: String? = null,
    ) : Exercise

    /** Rellenar la partícula que falta en una frase japonesa. */
    public data class ParticleFill(
        override val id: ExerciseId,
        val sentenceTemplate: String,
        val particles: List<String>,
        val correctParticle: String,
        override val audioText: String? = null,
    ) : Exercise
}

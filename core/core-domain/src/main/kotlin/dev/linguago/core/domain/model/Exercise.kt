package dev.linguago.core.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Tipos de ejercicio del sistema (RFC §3.1). Cada tipo queda modelado como
 * data class con parámetros completos y autocodificables en contenido JSON.
 * La discriminación polimórfica usa el discriminator "type" con el nombre
 * simple del tipo vía @SerialName (ADR-009). La validación estructural
 * (respuestas correctas dentro de opciones, tokens únicos, etc.) la hace el
 * validador de contenido (core-content, Fase 1).
 */
@Serializable
public sealed interface Exercise {
    public val id: ExerciseId
    public val audioText: String?

    @Serializable
    @SerialName("MultipleChoice")
    public data class MultipleChoice(
        override val id: ExerciseId,
        val question: String,
        val options: List<String>,
        val correctIndex: Int,
        override val audioText: String? = null,
    ) : Exercise

    /** Traducción ensamblando tokens de respuesta/distraídos. */
    @Serializable
    @SerialName("Translate")
    public data class Translate(
        override val id: ExerciseId,
        val sourcePhrase: String,
        val correctTokens: List<String>,
        val distractorTokens: List<String> = emptyList(),
        override val audioText: String? = null,
    ) : Exercise

    /** Emparejar conceptos (palabra↔traducción, kana↔romaji, ...). */
    @Serializable
    @SerialName("MatchPairs")
    public data class MatchPairs(
        override val id: ExerciseId,
        /** Pares (izquierda, derecha) asociados por identidad — la UI los desordena. */
        val pairs: List<MatchPair>,
        override val audioText: String? = null,
    ) : Exercise

    /** Par de conceptos asociados en MatchPairs; el JSON usa {left, right}. */
    @Serializable
    public data class MatchPair(
        val left: String,
        val right: String,
    )

    /** Ordenar tokens para reconstruir una frase objetivo. */
    @Serializable
    @SerialName("OrderWords")
    public data class OrderWords(
        override val id: ExerciseId,
        val tokens: List<String>,
        val answerTokens: List<String>,
        override val audioText: String? = null,
    ) : Exercise

    /** Escuchar y elegir la transcripción entre opciones. */
    @Serializable
    @SerialName("ListenChoose")
    public data class ListenChoose(
        override val id: ExerciseId,
        val options: List<String>,
        val correctIndex: Int,
        /** Frase cuyo audio es el enunciado del ejercicio (obligatorio). */
        override val audioText: String,
    ) : Exercise

    /** Escuchar y escribir la frase exacta (dictado). */
    @Serializable
    @SerialName("ListenType")
    public data class ListenType(
        override val id: ExerciseId,
        val transcription: String,
        override val audioText: String,
    ) : Exercise

    /** Repetir en voz alta una frase objetivo (STT, Fase 2). */
    @Serializable
    @SerialName("SpeakRepeat")
    public data class SpeakRepeat(
        override val id: ExerciseId,
        val targetPhrase: String,
        override val audioText: String? = null,
    ) : Exercise

    /** Reconocer kana japonés (romaji entre opciones). */
    @Serializable
    @SerialName("KanaChoose")
    public data class KanaChoose(
        override val id: ExerciseId,
        val kana: String,
        val romajiOptions: List<String>,
        val correctRomaji: String,
        override val audioText: String? = null,
    ) : Exercise

    /** Reconocer lectura de un kanji entre opciones. */
    @Serializable
    @SerialName("KanjiReading")
    public data class KanjiReading(
        override val id: ExerciseId,
        val kanji: String,
        val readingOptions: List<String>,
        val correctReading: String,
        override val audioText: String? = null,
    ) : Exercise

    /** Rellenar la partícula que falta en una frase japonesa. */
    @Serializable
    @SerialName("ParticleFill")
    public data class ParticleFill(
        override val id: ExerciseId,
        val sentenceTemplate: String,
        val particles: List<String>,
        val correctParticle: String,
        override val audioText: String? = null,
    ) : Exercise
}

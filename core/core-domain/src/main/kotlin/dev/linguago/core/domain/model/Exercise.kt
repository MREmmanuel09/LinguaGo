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
    public val feedbackOnError: FeedbackOnError?

    /** Categoría del error para el feedback estructurado de v2 (ADR-018). */
    @Serializable
    public enum class FeedbackErrorType {
        @SerialName("grammar")
        GRAMMAR,

        @SerialName("vocabulary")
        VOCABULARY,

        @SerialName("spelling")
        SPELLING,

        @SerialName("pronunciation")
        PRONUNCIATION,

        @SerialName("word_order")
        WORD_ORDER,

        @SerialName("register")
        REGISTER,

        @SerialName("comprehension")
        COMPREHENSION,
    }

    /** Explicación concreta del error, opcional en A1/A2 y obligatoria en B1/B2. */
    @Serializable
    public data class FeedbackOnError(
        public val type: FeedbackErrorType,
        public val explanation: String,
        public val ruleRef: GrammarPointId? = null,
    )

    /** Matiz de registro de un par semántico (B2). */
    @Serializable
    public enum class SemanticRegister {
        @SerialName("neutral")
        NEUTRAL,

        @SerialName("formal")
        FORMAL,

        @SerialName("informal")
        INFORMAL,

        @SerialName("literary")
        LITERARY,

        @SerialName("colloquial")
        COLLOQUIAL,
    }

    /** Par de `SemanticMatch` con registro y matiz pragmático opcionales. */
    @Serializable
    public data class SemanticPair(
        public val left: String,
        public val right: String,
        public val register: SemanticRegister? = null,
        public val nuance: String? = null,
    )

    /** Hueco de un `ClozeDragDrop`: respuesta correcta y opciones arrastrables. */
    @Serializable
    public data class ClozeBlank(
        public val id: String,
        public val answer: String,
        public val options: List<String>,
    )

    @Serializable
    @SerialName("MultipleChoice")
    public data class MultipleChoice(
        override val id: ExerciseId,
        val question: String,
        val options: List<String>,
        val correctIndex: Int,
        override val audioText: String? = null,
        override val feedbackOnError: FeedbackOnError? = null,
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
        override val feedbackOnError: FeedbackOnError? = null,
    ) : Exercise

    /** Emparejar conceptos (palabra↔traducción, kana↔romaji, ...). */
    @Serializable
    @SerialName("MatchPairs")
    public data class MatchPairs(
        override val id: ExerciseId,
        /** Pares (izquierda, derecha) asociados por identidad — la UI los desordena. */
        val pairs: List<MatchPair>,
        override val audioText: String? = null,
        override val feedbackOnError: FeedbackOnError? = null,
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
        override val feedbackOnError: FeedbackOnError? = null,
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
        override val feedbackOnError: FeedbackOnError? = null,
    ) : Exercise

    /** Escuchar y escribir la frase exacta (dictado). */
    @Serializable
    @SerialName("ListenType")
    public data class ListenType(
        override val id: ExerciseId,
        val transcription: String,
        override val audioText: String,
        override val feedbackOnError: FeedbackOnError? = null,
    ) : Exercise

    /** Repetir en voz alta una frase objetivo (STT, Fase 2). */
    @Serializable
    @SerialName("SpeakRepeat")
    public data class SpeakRepeat(
        override val id: ExerciseId,
        val targetPhrase: String,
        override val audioText: String? = null,
        override val feedbackOnError: FeedbackOnError? = null,
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
        override val feedbackOnError: FeedbackOnError? = null,
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
        override val feedbackOnError: FeedbackOnError? = null,
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
        override val feedbackOnError: FeedbackOnError? = null,
    ) : Exercise

    /**
     * Comprensión auditiva sin transcripción visible (v2, ADR-018):
     * [audioText] solo alimenta al TTS y nunca se muestra antes de responder.
     */
    @Serializable
    @SerialName("AudioOnlyPrompt")
    public data class AudioOnlyPrompt(
        override val id: ExerciseId,
        override val audioText: String,
        val options: List<String>,
        val correctIndex: Int,
        val question: String? = null,
        val allowSlowPlayback: Boolean = false,
        override val feedbackOnError: FeedbackOnError? = null,
    ) : Exercise

    /** Emparejamiento semántico con matiz de registro (v2, B2). */
    @Serializable
    @SerialName("SemanticMatch")
    public data class SemanticMatch(
        override val id: ExerciseId,
        val pairs: List<SemanticPair>,
        val instructions: String? = null,
        override val audioText: String? = null,
        override val feedbackOnError: FeedbackOnError? = null,
    ) : Exercise

    /**
     * Cloze con huecos arrastrables (v2): [passage] referencia cada hueco
     * con `{{id}}` y [blanks] define respuesta y opciones.
     */
    @Serializable
    @SerialName("ClozeDragDrop")
    public data class ClozeDragDrop(
        override val id: ExerciseId,
        val passage: String,
        val blanks: List<ClozeBlank>,
        override val audioText: String? = null,
        override val feedbackOnError: FeedbackOnError? = null,
    ) : Exercise

    /**
     * Producción oral libre B2 (v2, ADR-019). Se evalúa offline con
     * `FreeSpeakingHeuristicGrader` (F1.1) y autoevaluación guiada; prohibido
     * cualquier LLM on-device o cloud.
     */
    @Serializable
    @SerialName("FreeSpeakingPrompt")
    public data class FreeSpeakingPrompt(
        override val id: ExerciseId,
        val prompt: String,
        val minTokens: Int,
        val conceptClusters: List<List<String>>,
        val modelAnswer: String,
        val selfAssessmentCriteria: List<String>,
        val grammarRuleRefs: List<GrammarPointId> = emptyList(),
        val timeLimitSeconds: Int? = null,
        val minWpm: Double? = null,
        val maxWpm: Double? = null,
        override val audioText: String? = null,
        override val feedbackOnError: FeedbackOnError? = null,
    ) : Exercise
}

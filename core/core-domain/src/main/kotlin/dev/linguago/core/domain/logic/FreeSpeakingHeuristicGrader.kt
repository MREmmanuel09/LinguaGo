package dev.linguago.core.domain.logic

import dev.linguago.core.domain.model.Exercise
import dev.linguago.core.domain.model.GrammarPoint
import dev.linguago.core.domain.model.GrammarPointId

/** Motivos deterministas por los que un `FreeSpeakingPrompt` puede no pasar. */
public enum class FreeSpeakingReason {
    TOO_SHORT,
    LOW_COVERAGE,
    MISSING_GRAMMAR,
    WPM_OUT_OF_RANGE,
    LOW_SELF_ASSESSMENT,
}

/** Veredicto del motor heurístico offline (ADR-019). */
public sealed interface FreeSpeakingVerdict {
    public data object Pass : FreeSpeakingVerdict

    public data class NeedsWork(
        public val reasons: List<FreeSpeakingReason>,
    ) : FreeSpeakingVerdict
}

/**
 * Respuesta oral libre del estudiante: transcripción del STT (Vosk),
 * duración de la captura y autoevaluación opcional en el rango 0..1.
 */
public data class SpokenFreeResponse(
    public val transcript: String,
    public val durationMillis: Long,
    public val selfAssessmentScore: Double? = null,
)

/** Desglose determinista del resultado de [FreeSpeakingHeuristicGrader]. */
public data class FreeSpeakingFeedback(
    public val passes: Boolean,
    public val tokenCount: Int,
    public val coveredClusters: Int,
    public val totalClusters: Int,
    public val detectedGrammarRefs: List<GrammarPointId>,
    public val wpm: Double,
    public val verdict: FreeSpeakingVerdict,
)

/**
 * Evaluación 100% offline y determinista de [Exercise.FreeSpeakingPrompt]
 * (ADR-019; prohibido cualquier LLM on-device o cloud). Combina cuatro
 * señales objetivas sobre la transcripción — longitud mínima de tokens,
 * cobertura de `conceptClusters` con tolerancia a homófonos de Vosk
 * (distancia de edición acotada), detección por patrones de los
 * `grammarPoints` exigidos y cadencia WPM — con la autoevaluación del
 * estudiante. Un grammarPoint sin patrón conocido no se exige, para no
 * bloquear contenido válido por un catálogo incompleto (I3).
 */
public object FreeSpeakingHeuristicGrader {
    /**
     * Evalúa la respuesta contra el ejercicio. [grammarPoints] es la lista
     * de focos gramaticales de la lección que resuelve los
     * `grammarRuleRefs` del ejercicio. Nunca lanza: toda entrada produce un
     * [FreeSpeakingFeedback] determinista.
     */
    public fun evaluate(
        exercise: Exercise.FreeSpeakingPrompt,
        grammarPoints: List<GrammarPoint>,
        response: SpokenFreeResponse,
    ): FreeSpeakingFeedback {
        val tokens = tokenize(response.transcript)
        val tokenCount = tokens.size
        val reasons = mutableListOf<FreeSpeakingReason>()

        if (tokenCount < exercise.minTokens) {
            reasons += FreeSpeakingReason.TOO_SHORT
        }

        val coveredClusters = exercise.conceptClusters.count { cluster -> isClusterCovered(cluster, tokens) }
        val requiredCoverage = maxOf(MIN_CLUSTER_COVERAGE, (exercise.conceptClusters.size + 1) / 2)
        if (coveredClusters < requiredCoverage) {
            reasons += FreeSpeakingReason.LOW_COVERAGE
        }

        val detectableRefs =
            exercise.grammarRuleRefs.mapNotNull { ref ->
                val point = grammarPoints.firstOrNull { candidate -> candidate.id == ref } ?: return@mapNotNull null
                if (patternFor(point) == null) return@mapNotNull null
                ref
            }
        val detectedRefs =
            detectableRefs
                .mapNotNull { ref ->
                    val point = grammarPoints.first { candidate -> candidate.id == ref }
                    if (patternFor(point)?.containsMatchIn(response.transcript) == true) ref else null
                }
        if (detectedRefs.size != detectableRefs.size) {
            reasons += FreeSpeakingReason.MISSING_GRAMMAR
        }

        val wpm =
            if (response.durationMillis > 0) {
                tokenCount * MILLIS_PER_MINUTE.toDouble() / response.durationMillis
            } else {
                0.0
            }
        val belowMin = exercise.minWpm != null && wpm < exercise.minWpm
        val aboveMax = exercise.maxWpm != null && wpm > exercise.maxWpm
        if (belowMin || aboveMax) {
            reasons += FreeSpeakingReason.WPM_OUT_OF_RANGE
        }

        val selfScore = response.selfAssessmentScore
        if (selfScore != null && selfScore < SELF_ASSESSMENT_THRESHOLD) {
            reasons += FreeSpeakingReason.LOW_SELF_ASSESSMENT
        }

        val verdict =
            if (reasons.isEmpty()) {
                FreeSpeakingVerdict.Pass
            } else {
                FreeSpeakingVerdict.NeedsWork(reasons)
            }
        return FreeSpeakingFeedback(
            passes = reasons.isEmpty(),
            tokenCount = tokenCount,
            coveredClusters = coveredClusters,
            totalClusters = exercise.conceptClusters.size,
            detectedGrammarRefs = detectedRefs,
            wpm = wpm,
            verdict = verdict,
        )
    }

    /** Tokeniza en minúsculas conservando contracciones (`i'm` es un token). */
    private fun tokenize(text: String): List<String> =
        text
            .lowercase()
            .split(TOKEN_SEPARATOR)
            .filter { token -> token.isNotBlank() }

    /** Un cluster queda cubierto si alguno de sus términos (o sinónimos) aparece. */
    private fun isClusterCovered(
        cluster: List<String>,
        tokens: List<String>,
    ): Boolean =
        cluster.any { term ->
            val words = tokenize(term)
            words.isNotEmpty() && words.all { word -> tokens.any { token -> nearMatch(token, word) } }
        }

    /** Igualdad exacta o distancia de edición acotada para palabras largas. */
    private fun nearMatch(
        token: String,
        term: String,
    ): Boolean {
        if (term.isBlank()) return false
        if (token == term) return true
        return term.length >= MIN_LENGTH_FOR_FUZZY && editDistance(token, term) <= MAX_EDIT_DISTANCE
    }

    /** Levenshtein clásico con dos filas (determinista y sin asignaciones ruidosas). */
    private fun editDistance(
        first: String,
        second: String,
    ): Int {
        var previous = IntArray(second.length + 1) { index -> index }
        for (i in first.indices) {
            val current = IntArray(second.length + 1)
            current[0] = i + 1
            for (j in second.indices) {
                val substitution = if (first[i] == second[j]) 0 else 1
                current[j + 1] =
                    minOf(
                        previous[j + 1] + 1,
                        current[j] + 1,
                        previous[j] + substitution,
                    )
            }
            previous = current
        }
        return previous[second.length]
    }

    /** Catálogo determinista de patrones por palabra clave del label del grammarPoint. */
    private fun patternFor(point: GrammarPoint): Regex? {
        val label = point.label.lowercase()
        return when {
            label.contains("conditional") -> CONDITIONAL_PATTERN
            label.contains("hedging") || label.contains("hedge") -> HEDGING_PATTERN
            label.contains("passive") -> PASSIVE_PATTERN
            label.contains("compar") -> COMPARATIVE_PATTERN
            label.contains("inversion") -> INVERSION_PATTERN
            label.contains("reported") || label.contains("reporting") -> REPORTED_PATTERN
            label.contains("cleft") -> CLEFT_PATTERN
            label.contains("subjunctive") -> SUBJUNCTIVE_PATTERN
            else -> null
        }
    }

    private val TOKEN_SEPARATOR = Regex("[^\\p{L}\\p{N}']+")
    private const val MILLIS_PER_MINUTE = 60_000
    private const val MIN_CLUSTER_COVERAGE = 1
    private const val MAX_EDIT_DISTANCE = 2
    private const val MIN_LENGTH_FOR_FUZZY = 5
    private const val SELF_ASSESSMENT_THRESHOLD = 0.5

    private val CONDITIONAL_PATTERN =
        Regex("\\bif\\b|\\bunless\\b|\\bprovided that\\b|\\bas long as\\b|\\bwould\\b|\\bcould\\b|\\bshould\\b", RegexOption.IGNORE_CASE)
    private val HEDGING_PATTERN =
        Regex("\\bi'm afraid\\b|\\bperhaps\\b|\\bmaybe\\b|\\bit seems\\b|\\bprobably\\b|\\bpossibly\\b", RegexOption.IGNORE_CASE)
    private val PASSIVE_PATTERN =
        Regex("\\b(?:am|is|are|was|were|been|being|be)\\s+[\\w'-]+ed\\b", RegexOption.IGNORE_CASE)
    private val COMPARATIVE_PATTERN =
        Regex("\\bmore\\b|\\bless\\b|\\bthan\\b", RegexOption.IGNORE_CASE)
    private val INVERSION_PATTERN =
        Regex("\\bnever\\b|\\bseldom\\b|\\brarely\\b|\\bhardly\\b|\\bnot only\\b|\\bunder no circumstances\\b", RegexOption.IGNORE_CASE)
    private val REPORTED_PATTERN =
        Regex("\\bit is (?:said|believed|thought|reported|expected)\\b|\\bis (?:said|believed|thought)\\s+to\\b", RegexOption.IGNORE_CASE)
    private val CLEFT_PATTERN =
        Regex("\\bit (?:is|was)\\b|\\bwhat\\b", RegexOption.IGNORE_CASE)
    private val SUBJUNCTIVE_PATTERN =
        Regex("\\bwere\\b|\\bsuggest\\b|\\brecommend\\b|\\binsist\\b|\\bdemand\\b|\\bessential\\b|\\bimportant\\b", RegexOption.IGNORE_CASE)
}

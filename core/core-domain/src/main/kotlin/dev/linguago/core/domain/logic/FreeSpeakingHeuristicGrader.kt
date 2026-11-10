package dev.linguago.core.domain.logic

import dev.linguago.core.domain.model.Exercise
import dev.linguago.core.domain.model.GrammarPoint
import dev.linguago.core.domain.model.GrammarPointId
import dev.linguago.core.domain.model.Language
import java.text.Normalizer

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
    /**
     * Señales informativas que no bloquean el paso (ADR-043): la
     * autoevaluación baja se reporta aquí en lugar de fallar el ejercicio.
     */
    public val advisories: List<FreeSpeakingReason> = emptyList(),
)

/**
 * Evaluación 100% offline y determinista de [Exercise.FreeSpeakingPrompt]
 * (ADR-019; prohibido cualquier LLM on-device o cloud). Combina cuatro
 * señales objetivas sobre la transcripción — longitud mínima de tokens,
 * cobertura de `conceptClusters` con tolerancia a la fonética de Vosk
 * (diacríticos normalizados según ADR-039 y distancia de edición acotada
 * según ADR-043), detección por patrones de los `grammarPoints` exigidos y
 * cadencia WPM con un margen de tolerancia (ADR-043). La autoevaluación es
 * informativa: se reporta en [FreeSpeakingFeedback.advisories] y nunca
 * bloquea el paso (ADR-043). Un grammarPoint sin patrón conocido no se
 * exige, para no bloquear contenido válido por un catálogo incompleto (I3).
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
        language: Language = Language.ENGLISH,
    ): FreeSpeakingFeedback {
        val tokens = tokenize(response.transcript)
        val grammarText =
            if (language == Language.JAPANESE) {
                // El contenido de los cursos JA tokeniza por morfemas con
                // espacios; el STT real no. Se normaliza para que los patrones
                // de dos morfos (`と 言い`) matcheen cualquier transcripción.
                // Se evita fold(): la descomposición NFD separa los dakuten y
                // perdería が/ぎ/ざ/で/び.
                response.transcript.lowercase().replace(Regex("\\s+"), "")
            } else {
                fold(response.transcript)
            }
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
                val pattern = patternFor(point, language) ?: return@mapNotNull null
                // El patrón solo es exigible si la respuesta modelo lo
                // demuestra: un catálogo incompleto no puede castigar al alumno
                // por una gramática que el propio contenido no ejemplifica (I3).
                val modelText =
                    if (language == Language.JAPANESE) {
                        exercise.modelAnswer.lowercase().replace(Regex("\\s+"), "")
                    } else {
                        fold(exercise.modelAnswer)
                    }
                if (!pattern.containsMatchIn(modelText)) return@mapNotNull null
                ref
            }
        val detectedRefs =
            detectableRefs
                .mapNotNull { ref ->
                    val point = grammarPoints.first { candidate -> candidate.id == ref }
                    if (patternFor(point, language)?.containsMatchIn(grammarText) == true) ref else null
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
        val minWpm = exercise.minWpm
        val maxWpm = exercise.maxWpm
        // ADR-043: la cadencia admite un margen del ±20% (min/1.2, max*1.2),
        // porque Vosk y el micrófono escalan tiempos sin señalar error real.
        val belowMin = minWpm != null && wpm < minWpm / WPM_TOLERANCE
        val aboveMax = maxWpm != null && wpm > maxWpm * WPM_TOLERANCE
        if (belowMin || aboveMax) {
            reasons += FreeSpeakingReason.WPM_OUT_OF_RANGE
        }

        // ADR-043: la autoevaluación es informativa; solo las señales
        // objetivas componen el veredicto.
        val advisories = mutableListOf<FreeSpeakingReason>()
        val selfScore = response.selfAssessmentScore
        if (selfScore != null && selfScore < SELF_ASSESSMENT_THRESHOLD) {
            advisories += FreeSpeakingReason.LOW_SELF_ASSESSMENT
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
            advisories = advisories,
        )
    }

    /**
     * Términos de las ideas clave ya cubiertas por [transcript] (para la
     * visualización en vivo): devuelve el primer término que cubre cada
     * cluster, sin duplicados y con la misma tolerancia a homófonos que la
     * evaluación final.
     */
    public fun coveredClusterTerms(
        exercise: Exercise.FreeSpeakingPrompt,
        transcript: String,
    ): List<String> {
        val tokens = tokenize(transcript)
        return exercise.conceptClusters
            .mapNotNull { cluster -> coveredTerm(cluster, tokens) }
            .distinct()
    }

    /**
     * Tokeniza en minúsculas conservando contracciones (`i'm` es un token) y
     * sin diacríticos latinos: NFD expone las tildes como marcas combinantes
     * que se filtran, igual que la evaluación escrita (ADR-039). El japonés
     * no separa palabras con espacios y sus dakuten son contenido, no
     * acentos, así que su rama no se normaliza (ADR-043).
     */
    private fun tokenize(text: String): List<String> {
        val lower = text.lowercase()
        if (JAPANESE_PATTERN.containsMatchIn(lower)) {
            return lower.filter { char -> char.isLetterOrDigit() }.map { char -> char.toString() }
        }
        val plain = Normalizer.normalize(lower, Normalizer.Form.NFD).replace(COMBINING_MARK, "")
        return plain
            .split(TOKEN_SEPARATOR)
            .filter { token -> token.isNotBlank() }
    }

    /** Un cluster queda cubierto si alguno de sus términos (o sinónimos) aparece. */
    private fun isClusterCovered(
        cluster: List<String>,
        tokens: List<String>,
    ): Boolean = coveredTerm(cluster, tokens) != null

    /** Primer término del cluster presente en [tokens], o `null` si no hay. */
    private fun coveredTerm(
        cluster: List<String>,
        tokens: List<String>,
    ): String? =
        cluster.firstOrNull { term ->
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

    /**
     * Catálogo determinista por familia gramatical y [language] (ADR-045):
     * las etiquetas del contenido pueden venir en español, francés, portugués
     * o japonés, pero el patrón se aplica siempre en el idioma meta.
     */
    private fun patternFor(
        point: GrammarPoint,
        language: Language,
    ): Regex? {
        val label = fold(point.label)
        return when {
            CONDITIONAL_KEYS.any(label::contains) -> conditionalPattern(language)
            HEDGING_KEYS.any(label::contains) -> hedgingPattern(language)
            listOf(
                "tempos do relato",
                "narrative tense",
                "narrativa literaria",
                "物語の時制",
                "retrospectiva",
                "retrospective",
            ).any(label::contains) -> tensePattern(language)
            REPORTED_KEYS.any(label::contains) -> reportedPattern(language)
            PASSIVE_KEYS.any(label::contains) -> passivePattern(language)
            COMPARATIVE_KEYS.any(label::contains) -> comparativePattern(language)
            INVERSION_KEYS.any(label::contains) -> inversionPattern(language)
            CLEFT_KEYS.any(label::contains) -> cleftPattern(language)
            SUBJUNCTIVE_KEYS.any(label::contains) -> subjunctivePattern(language)
            TENSE_KEYS.any(label::contains) -> tensePattern(language)
            CONTRAST_KEYS.any(label::contains) -> contrastPattern(language)
            RELATIVE_KEYS.any(label::contains) -> relativePattern(language)
            MODAL_KEYS.any(label::contains) -> modalPattern(language)
            QUESTION_TAG_KEYS.any(label::contains) -> questionTagPattern(language)
            else -> null
        }
    }

    /** Minúsculas sin diacríticos para comparar etiquetas de cualquier idioma. */
    private fun fold(text: String): String =
        Normalizer
            .normalize(text.lowercase(), Normalizer.Form.NFD)
            .replace(COMBINING_MARK, "")

    private fun conditionalPattern(language: Language): Regex =
        when (language) {
            Language.FRENCH -> FR_CONDITIONAL_PATTERN
            Language.PORTUGUESE -> PT_CONDITIONAL_PATTERN
            Language.JAPANESE -> JA_CONDITIONAL_PATTERN
            else -> EN_CONDITIONAL_PATTERN
        }

    private fun hedgingPattern(language: Language): Regex =
        when (language) {
            Language.FRENCH -> FR_HEDGING_PATTERN
            Language.PORTUGUESE -> PT_HEDGING_PATTERN
            Language.JAPANESE -> JA_HEDGING_PATTERN
            else -> EN_HEDGING_PATTERN
        }

    private fun passivePattern(language: Language): Regex =
        when (language) {
            Language.FRENCH -> FR_PASSIVE_PATTERN
            Language.PORTUGUESE -> PT_PASSIVE_PATTERN
            Language.JAPANESE -> JA_PASSIVE_PATTERN
            else -> EN_PASSIVE_PATTERN
        }

    private fun comparativePattern(language: Language): Regex =
        when (language) {
            Language.FRENCH -> FR_COMPARATIVE_PATTERN
            Language.PORTUGUESE -> PT_COMPARATIVE_PATTERN
            Language.JAPANESE -> JA_COMPARATIVE_PATTERN
            else -> EN_COMPARATIVE_PATTERN
        }

    private fun inversionPattern(language: Language): Regex =
        when (language) {
            Language.FRENCH -> FR_INVERSION_PATTERN
            Language.PORTUGUESE -> PT_INVERSION_PATTERN
            Language.JAPANESE -> JA_INVERSION_PATTERN
            else -> EN_INVERSION_PATTERN
        }

    private fun reportedPattern(language: Language): Regex =
        when (language) {
            Language.FRENCH -> FR_REPORTED_PATTERN
            Language.PORTUGUESE -> PT_REPORTED_PATTERN
            Language.JAPANESE -> JA_REPORTED_PATTERN
            else -> EN_REPORTED_PATTERN
        }

    private fun cleftPattern(language: Language): Regex =
        when (language) {
            Language.FRENCH -> FR_CLEFT_PATTERN
            Language.PORTUGUESE -> PT_CLEFT_PATTERN
            Language.JAPANESE -> JA_CLEFT_PATTERN
            else -> EN_CLEFT_PATTERN
        }

    private fun subjunctivePattern(language: Language): Regex =
        when (language) {
            Language.FRENCH -> FR_SUBJUNCTIVE_PATTERN
            Language.PORTUGUESE -> PT_SUBJUNCTIVE_PATTERN
            Language.JAPANESE -> JA_SUBJUNCTIVE_PATTERN
            else -> EN_SUBJUNCTIVE_PATTERN
        }

    private fun tensePattern(language: Language): Regex =
        when (language) {
            Language.FRENCH -> FR_TENSE_PATTERN
            Language.PORTUGUESE -> PT_TENSE_PATTERN
            Language.JAPANESE -> JA_TENSE_PATTERN
            else -> EN_TENSE_PATTERN
        }

    private fun contrastPattern(language: Language): Regex =
        when (language) {
            Language.FRENCH -> FR_CONTRAST_PATTERN
            Language.PORTUGUESE -> PT_CONTRAST_PATTERN
            Language.JAPANESE -> JA_CONTRAST_PATTERN
            else -> EN_CONTRAST_PATTERN
        }

    private fun relativePattern(language: Language): Regex =
        when (language) {
            Language.FRENCH -> FR_RELATIVE_PATTERN
            Language.PORTUGUESE -> PT_RELATIVE_PATTERN
            Language.JAPANESE -> JA_RELATIVE_PATTERN
            else -> EN_RELATIVE_PATTERN
        }

    private fun modalPattern(language: Language): Regex =
        when (language) {
            Language.FRENCH -> FR_MODAL_PATTERN
            Language.PORTUGUESE -> PT_MODAL_PATTERN
            Language.JAPANESE -> JA_MODAL_PATTERN
            else -> EN_MODAL_PATTERN
        }

    private fun questionTagPattern(language: Language): Regex =
        when (language) {
            Language.FRENCH -> FR_QUESTION_TAG_PATTERN
            Language.PORTUGUESE -> PT_QUESTION_TAG_PATTERN
            Language.JAPANESE -> JA_QUESTION_TAG_PATTERN
            else -> EN_QUESTION_TAG_PATTERN
        }

    private val TOKEN_SEPARATOR = Regex("[^\\p{L}\\p{N}']+")
    private val COMBINING_MARK = Regex("[\\u0300-\\u036F]")
    private val JAPANESE_PATTERN = Regex("[\\u3040-\\u30FF\\u3400-\\u4DBF\\u4E00-\\u9FFF]")
    private const val MILLIS_PER_MINUTE = 60_000
    private const val MIN_CLUSTER_COVERAGE = 1
    private const val MAX_EDIT_DISTANCE = 2

    /** A partir de esta longitud se tolera la fonética de Vosk (ADR-043). */
    private const val MIN_LENGTH_FOR_FUZZY = 4
    private const val SELF_ASSESSMENT_THRESHOLD = 0.5

    /** Margen de cadencia: WPM válido entre min/1.2 y max*1.2 (ADR-043). */
    private const val WPM_TOLERANCE = 1.2

    private val CONDITIONAL_KEYS =
        listOf("conditional", "conditionnel", "condicional", "hypothese", "hipotesis", "hipotese", "条件", "仮定", "混合条件")
    private val HEDGING_KEYS =
        listOf("hedg", "atenu", "attenu", "cautela", "緩和", "ヘッジ", "substatement")
    private val PASSIVE_KEYS = listOf("passiv", "passif", "pasiva", "受身", "受け身")
    private val COMPARATIVE_KEYS = listOf("compar", "比較")
    private val INVERSION_KEYS = listOf("invers", "倒置")
    private val REPORTED_KEYS =
        listOf("reported", "reporting", "report", "rapport", "relato", "indireto", "indirect", "伝聞", "報告")
    private val CLEFT_KEYS =
        listOf("cleft", "cliv", "enfatic", "mise en relief", "分裂文", "強調構文")
    private val SUBJUNCTIVE_KEYS = listOf("subjunt", "subjonctif", "接続法")
    private val TENSE_KEYS =
        listOf(
            "past continuous",
            "past simple",
            "present perfect",
            "narrative tense",
            "narrativa literaria",
            "perfect",
            "aspect",
            "temps du recit",
            "tempos do relato",
            "物語の時制",
            "完了のアスペクト",
            "アスペクト",
            "retrospectiva",
            "retrospective",
        )
    private val CONTRAST_KEYS =
        listOf("contrast", "contraste", "contraste linkers", "connecteurs de contraste", "conectores de contraste", "対比", "譲歩")
    private val RELATIVE_KEYS =
        listOf("relative", "relativ", "relativs", "relativos", "連体修飾")
    private val MODAL_KEYS =
        listOf("modal", "modaux", "modais", "obligation", "deduction", "deduccion", "deduc", "deduction", "modality", "modalidade")
    private val QUESTION_TAG_KEYS =
        listOf("question tag", "question tags", "pregunta de confirmacion", "tag question")

    private val EN_CONDITIONAL_PATTERN =
        Regex(
            "\\bif\\b|\\bunless\\b|\\bprovided that\\b|\\bas long as\\b|\\bwould\\b|\\bcould\\b" +
                "|\\bshould\\b",
            RegexOption.IGNORE_CASE,
        )
    private val FR_CONDITIONAL_PATTERN =
        Regex(
            "\\bsi\\b|\\bau cas ou\\b|\\ba condition que\\b|\\bpourr\\w*\\b|\\bser\\w*\\b|\\baur\\w*\\b" +
                "|\\bdevr\\w*\\b|\\bfaudr\\w*\\b|\\bobtiendr\\w*\\b|\\bsoutiendr\\w*\\b|\\bviendr\\w*\\b",
            RegexOption.IGNORE_CASE,
        )
    private val PT_CONDITIONAL_PATTERN =
        Regex(
            "\\bse\\b|\\bdesde que\\b|\\bcaso\\b|\\b(?:poder|precisar|ser|ter|haver|obter|invest|apoi|estender" +
                "|assinar|gostar|ir|vir)\\w*(?:ia|iam|ias|iamos)\\b",
            RegexOption.IGNORE_CASE,
        )
    private val JA_CONDITIONAL_PATTERN = Regex("条件|仮定|場合|もし|なら|たら|ば|限り")

    private val EN_HEDGING_PATTERN =
        Regex(
            "\\bi'm afraid\\b|\\bperhaps\\b|\\bmaybe\\b|\\bit seems\\b|\\bprobably\\b|\\bpossibly\\b" +
                "|\\bi wonder\\b|\\bit might\\b|\\bi suppose\\b|\\bi guess\\b|\\bkind of\\b|\\bsort of\\b" +
                "|\\bto be honest\\b|\\bsomewhat\\b|\\bto say the least\\b|\\brather\\b|\\bsuggest\\b|\\bsuggests\\b",
            RegexOption.IGNORE_CASE,
        )
    private val FR_HEDGING_PATTERN =
        Regex(
            "\\bpeut-etre\\b|\\bquelque peu\\b|\\bun peu\\b|\\bje me demande\\b|\\bil vaudrait mieux\\b" +
                "|\\bje (?:pense|crois|trouve)\\b|\\ba mon avis\\b|\\bje comprends\\b|\\bsugg\\w*\\b" +
                "|\\b(?:pourr|ser|faudr|preferer|suggerer)\\w*(?:ait|ais|ions|iez|aient)\\b",
            RegexOption.IGNORE_CASE,
        )
    private val PT_HEDGING_PATTERN =
        Regex(
            "\\btalvez\\b|\\bum pouco\\b|\\bum tanto\\b|\\beu entendo\\b|\\bna minha opiniao\\b" +
                "|\\b(?:precisar|poder|ser|ter|sugerir|preferir)\\w*(?:ia|iam|ias|iamos)\\b" +
                "|\\bpergunto-me\\b",
            RegexOption.IGNORE_CASE,
        )
    private val JA_HEDGING_PATTERN =
        Regex("ご迷惑|少し|ちょっと|おそらく|たぶん|かもしれ|でしょう|でしょうか|と思います|提案|緩和|控えめ|必要|できれば")

    private val EN_PASSIVE_PATTERN =
        Regex(
            "\\b(?:am|is|are|was|were|been|being|be)\\s+(?:\\w+ed|shown|made|done|given|taken|seen|sent" +
                "|held|built|told|found|written|spoken|known|missing|available|ready|overdue)\\b",
            RegexOption.IGNORE_CASE,
        )
    private val FR_PASSIVE_PATTERN =
        Regex(
            "\\b(?:a|ont|est|sont|etait|etaient|sera|seront|serait|seraient|soit|soient|etre)\\b" +
                "[^.!?]{0,35}\\b\\w+(?:e|ee|es|ees|i|ie|is|ies|u|ue|us|ues)\\b",
            RegexOption.IGNORE_CASE,
        )
    private val PT_PASSIVE_PATTERN =
        Regex(
            "\\b(?:foi|foram|e|sao|era|eram|sera|serao|seria|seriam|seja|sejam|sendo|ser|estava|estavam|esteve|estao)\\b" +
                "\\s+\\w+(?:ado|ada|ados|adas|ido|ida|idos|idas|isto|ista|istos|istas)\\b",
            RegexOption.IGNORE_CASE,
        )
    private val JA_PASSIVE_PATTERN = Regex("され|られ")

    private val EN_COMPARATIVE_PATTERN =
        Regex("\\bmore\\b|\\bless\\b|\\bthan\\b", RegexOption.IGNORE_CASE)
    private val FR_COMPARATIVE_PATTERN =
        Regex("\\bplus\\b|\\bmoins\\b|\\bmieux\\b|\\bmeilleur\\w*\\b|\\bpire\\b|\\bautant\\b", RegexOption.IGNORE_CASE)
    private val PT_COMPARATIVE_PATTERN =
        Regex(
            "\\bmais\\b|\\bmenos\\b|\\bmelhor\\b|\\bmelhores\\b|\\bpior\\b|\\bmaior\\b|\\bmenor\\b|\\btao\\b|\\btanto\\b",
            RegexOption.IGNORE_CASE,
        )
    private val JA_COMPARATIVE_PATTERN = Regex("比較|より|ほど|もっと|一番|ほうが")

    private val EN_INVERSION_PATTERN =
        Regex(
            "\\bnever\\b|\\bseldom\\b|\\brarely\\b|\\bhardly\\b|\\bnot only\\b|\\bunder no circumstances\\b",
            RegexOption.IGNORE_CASE,
        )
    private val FR_INVERSION_PATTERN =
        Regex(
            "\\bnon seulement\\b|\\ben aucun cas\\b|\\brarement\\b|\\ba peine\\b" +
                "|\\bce n'est qu\\b|\\bnullement\\b",
            RegexOption.IGNORE_CASE,
        )
    private val PT_INVERSION_PATTERN =
        Regex(
            "\\bnao so\\b|\\bem nenhum caso\\b|\\braramente\\b",
            RegexOption.IGNORE_CASE,
        )
    private val JA_INVERSION_PATTERN = Regex("倒置|決して|ほとんど|めったに|ありません|ない")

    private val EN_REPORTED_PATTERN =
        Regex(
            "\\bit is (?:said|believed|thought|reported|expected|claimed|suggested)\\b|\\bis (?:said|believed|thought|claimed)\\s+to\\b" +
                "|\\b(?:said that|told me that|asked whether|asked if|told me|asked whether)\\b" +
                "|\\b(?:ana|my manager|he|she|they|we) said (?:the|that|me)\\b" +
                "|\\b(?:could you tell me if|can you tell me whether|do you know if|do you know whether|tell me who|tell me where|tell me when|tell me whether)\\b",
            RegexOption.IGNORE_CASE,
        )
    private val FR_REPORTED_PATTERN =
        Regex(
            "\\b(?:dit|demande|repondu|explique|raconte|selon|d'apres)\\b|\\bserait\\b|\\bseraient\\b" +
                "|\\baurai(?:t|ent)\\b|\\brapport\\b",
            RegexOption.IGNORE_CASE,
        )
    private val PT_REPORTED_PATTERN =
        Regex(
            "\\b(?:disse|falou|perguntou|contou|explicou|segundo|conforme)\\b|\\bseria\\b|\\bseriam\\b" +
                "|\\bteria\\b|\\bteriam\\b|\\brelato\\b",
            RegexOption.IGNORE_CASE,
        )
    private val JA_REPORTED_PATTERN = Regex("伝聞|報告|ということ|と言|と聞|によると")

    private val EN_CLEFT_PATTERN = Regex("\\bit (?:is|was)\\b|\\bwhat\\b", RegexOption.IGNORE_CASE)
    private val FR_CLEFT_PATTERN =
        Regex("\\bce (?:qui|que|dont)\\b|\\bc'est\\b|\\bce sont\\b|\\bmise en relief\\b", RegexOption.IGNORE_CASE)
    private val PT_CLEFT_PATTERN =
        Regex("\\bo que\\b|\\be que\\b|\\bfoi .{0,40} que\\b|\\bquem\\b", RegexOption.IGNORE_CASE)
    private val JA_CLEFT_PATTERN = Regex("分裂文|の(は|が)|こそ|強調構文")

    private val EN_SUBJUNCTIVE_PATTERN =
        Regex(
            "\\bwere\\b|\\bsuggest\\b|\\brecommend\\b|\\binsist\\b|\\bdemand\\b|\\bessential\\b|\\bimportant\\b",
            RegexOption.IGNORE_CASE,
        )
    private val FR_SUBJUNCTIVE_PATTERN =
        Regex(
            "\\bbien que\\b|\\bquoique\\b|\\bpour que\\b|\\bavant que\\b|\\bil faut que\\b|\\bil est essentiel que\\b" +
                "|\\b(?:soit|soient|fasse|fassions|puisse|ait|aies|parlions|reflechissions)\\b|\\bfaudrait\\b|\\bpourrions\\b|\\bdevrions\\b|\\bseraient\\b",
            RegexOption.IGNORE_CASE,
        )
    private val PT_SUBJUNCTIVE_PATTERN =
        Regex(
            "\\bembora\\b|\\bpara que\\b|\\bantes que\\b|\\be preciso que\\b|\\be possivel que\\b" +
                "|\\b(?:seja|sejam|tenha|tenham|faca|facamos|possa|falemos)\\b|\\b(?:deveriamos|poderiamos|seriamos|teriamos|estariamos|fariamos)\\b",
            RegexOption.IGNORE_CASE,
        )
    private val JA_SUBJUNCTIVE_PATTERN = Regex("べき|必要|希望|提案")

    private val EN_TENSE_PATTERN =
        Regex(
            "\\b(?:was|were)\\s+\\w+ing\\b|" +
                "\\bhad\\s+" +
                "(?:been|being|bent|done|gone|seen|sent|told|taken|given|written|built|held|found|made|\\w+ed)\\b|" +
                "\\b(?:i|we|you|they|he|she)'(?:ve|d)\\s+" +
                "(?:been|seen|done|gone|taken|finished|worked|started|left|read|chosen|shown|given|spoken)\\b|" +
                "\\b(?:have|has|had|haven't|hasn't|hadn't)\\s+" +
                "(?:been|seen|done|gone|taken|finished|worked|started|left|read|chosen|shown|given|spoken|\\w+ed)\\b|" +
                "\\b(?:watched|joined|started|felt|worked|stopped|fell|went|came|saw|took|gave|found|made|said|told|shook|broke)\\b",
            RegexOption.IGNORE_CASE,
        )
    private val FR_TENSE_PATTERN =
        Regex(
            "\\b(?:j'ai|j'avai|n'avions|nous avions|vous aviez|ils avaient|elles avaient|" +
                "j'avais|n'etions|nous etions|vous etiez|ils etaient|elles etaient)\\b|" +
                "\\b(?:avons|aviez|ayons|ayez|aient|etait|etaient)\\b|" +
                "\\b(?:a|ont|est|sont|etait|etaient)\\s+\\w+(?:e|ee|es|ees|i|ie|is|ies|u|ue|us|ues)\\b",
            RegexOption.IGNORE_CASE,
        )
    private val PT_TENSE_PATTERN =
        Regex(
            "\\b(?:eu|ele|ela|nos|voces|eles|elas)\\s+(?:fui|era|tinha|tive|fez|disse|veio|foi|eram|" +
                "tinham|viajei|visitamos|perdemos|ajudou)\\b|" +
                "\\b(?:foi|foram|era|eram|estava|estavam|esteve|estao)\\s+\\w+(?:ado|ada|ados|adas|ido|ida|idos|idas)\\b|" +
                "\\b(?:viajei|vi|falei|estudei|estudou|perdemos|perdeu|ajudou|tivemos|teve|entendi|mudava|mudei|" +
                "aceitei|encontrei|entregamos|era)\\b|\\b\\w+(?:ia|iam|ias|iamos)\\b",
            RegexOption.IGNORE_CASE,
        )
    private val JA_TENSE_PATTERN = Regex("だった|でした|していました|していました|ました|ませんでした|ていた|なかった")

    private val EN_CONTRAST_PATTERN =
        Regex(
            "\\bhowever\\b|\\balthough\\b|\\bthough\\b|\\bdespite\\b|\\bin spite of\\b|\\bnevertheless\\b|" +
                "\\bwhereas\\b|\\bon the other hand\\b",
            RegexOption.IGNORE_CASE,
        )
    private val FR_CONTRAST_PATTERN =
        Regex(
            "\\bcependant\\b|\\bpourtant\\b|\\balors que\\b|\\bmalgre\\b|\\bcontrairement a\\b",
            RegexOption.IGNORE_CASE,
        )
    private val PT_CONTRAST_PATTERN =
        Regex(
            "\\bentretanto\\b|\\bcontudo\\b|\\bembora\\b|\\bporem\\b|\\bno entanto\\b|\\bainda assim\\b",
            RegexOption.IGNORE_CASE,
        )
    private val JA_CONTRAST_PATTERN = Regex("しかし|けれど|だが|一方で|それでも|でも|ただし|が")

    private val EN_RELATIVE_PATTERN =
        Regex(
            "\\bwho\\b|\\bwhich\\b|\\bthat\\b|\\bwhere\\b|\\bwhen\\b|\\bwhose\\b|\\bwhom\\b",
            RegexOption.IGNORE_CASE,
        )
    private val FR_RELATIVE_PATTERN =
        Regex("\\bqui\\b|\\bque\\b|\\bquoi\\b|\\bo[uù]\\b|\\bdont\\b", RegexOption.IGNORE_CASE)
    private val PT_RELATIVE_PATTERN =
        Regex("\\bquem\\b|\\bque\\b|\\bqual\\b|\\bde que\\b|\\bem que\\b", RegexOption.IGNORE_CASE)
    private val JA_RELATIVE_PATTERN = Regex("の|が|をする|な")

    private val EN_MODAL_PATTERN =
        Regex(
            "\\bmust\\b|\\bmay\\b|\\bmight\\b|\\bhave to\\b|\\bneed to\\b|\\bought to\\b|\\bshould\\b|\\bcould\\b|\\bwould\\b|\\bcan\\b",
            RegexOption.IGNORE_CASE,
        )
    private val FR_MODAL_PATTERN =
        Regex(
            "\\bdoit\\b|\\bdoivent\\b|\\bfaut\\b|\\bpeut\\b|\\bpeuvent\\b|\\bveut\\b|\\bveulent\\b|\\bpourrait\\b|\\bdevrait\\b|\\bsaurait\\b|\\bvoudrais\\b|\\bpourrais\\b|\\bdevrais\\b|\\bfaudrait\\b|\\bserait\\b",
            RegexOption.IGNORE_CASE,
        )
    private val PT_MODAL_PATTERN =
        Regex(
            "\\bdeve\\b|\\bdevem\\b|\\bprecisa\\b|\\bprecisam\\b|\\btem de\\b|\\btemos de\\b|\\bpoderia\\b|\\bdeveria\\b|\\bficaria\\b|\\bpode\\b|\\bpodem\\b",
            RegexOption.IGNORE_CASE,
        )
    private val JA_MODAL_PATTERN = Regex("しなければならない|すべき|必要|できる|できれば|たい")

    private val EN_QUESTION_TAG_PATTERN =
        Regex(
            "\\b(?:am|is|are|was|were|has|have|had|do|does|did|can|could|will|would|should|must)n't\\s+(?:i|you|we|they|he|she)\\?",
            RegexOption.IGNORE_CASE,
        )
    private val FR_QUESTION_TAG_PATTERN =
        Regex("\\b(?:ne|n')\\s*(?:est-ce|es-tu|sommes-nous|etes-vous|va-t-il|va-t-elle)\\b", RegexOption.IGNORE_CASE)
    private val PT_QUESTION_TAG_PATTERN =
        Regex("\\b(?:nao|não)\\s+(?:e|es|e|estamos|estao|esta)\\b", RegexOption.IGNORE_CASE)
    private val JA_QUESTION_TAG_PATTERN = Regex("ますか|ですか|ましたか|でしょう")
}

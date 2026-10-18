package dev.linguago.core.domain.logic

import dev.linguago.core.domain.model.Exercise
import dev.linguago.core.domain.model.ExerciseId
import dev.linguago.core.domain.model.GrammarPoint
import dev.linguago.core.domain.model.GrammarPointId
import dev.linguago.core.domain.usecase.ExerciseAnswer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExerciseGraderTest {
    private fun ejercicioId(id: String): ExerciseId = ExerciseId(id)

    @Test
    fun multiple_choice_acierta_por_indice() {
        val exercise =
            Exercise.MultipleChoice(
                id = ejercicioId("mc"),
                question = "Cómo se dice 'manzana'?",
                options = listOf("apple", "orange", "banana"),
                correctIndex = 0,
            )
        assertEquals(true, ExerciseGrader.isCorrect(exercise, ExerciseAnswer.OptionPicked(0)))
        assertEquals(false, ExerciseGrader.isCorrect(exercise, ExerciseAnswer.OptionPicked(1)))
        assertEquals(false, ExerciseGrader.isCorrect(exercise, ExerciseAnswer.OptionPicked(5)))
    }

    @Test
    fun kana_choose_compara_romaji_no_indice_del_correcto() {
        val exercise =
            Exercise.KanaChoose(
                id = ejercicioId("kana"),
                kana = "あ",
                romajiOptions = listOf("i", "a", "u"),
                correctRomaji = "a",
            )
        assertEquals(true, ExerciseGrader.isCorrect(exercise, ExerciseAnswer.OptionPicked(1)))
        assertEquals(false, ExerciseGrader.isCorrect(exercise, ExerciseAnswer.OptionPicked(0)))
    }

    @Test
    fun translate_secuencia_correcta_aceptando_desorden_de_espacios() {
        val exercise =
            Exercise.Translate(
                id = ejercicioId("tr"),
                sourcePhrase = "Apple",
                correctTokens = listOf("the", "red", "apple"),
            )
        assertEquals(true, ExerciseGrader.isCorrect(exercise, ExerciseAnswer.TokenSequence(listOf("the", "red", "apple"))))
        assertEquals(true, ExerciseGrader.isCorrect(exercise, ExerciseAnswer.TokenSequence(listOf("The", "Red ", "  apple"))))
        assertEquals(false, ExerciseGrader.isCorrect(exercise, ExerciseAnswer.TokenSequence(listOf("red", "apple", "the"))))
        assertEquals(false, ExerciseGrader.isCorrect(exercise, ExerciseAnswer.TokenSequence(listOf("the", "red", "pear"))))
    }

    @Test
    fun order_words_ignora_puntuacion_y_capitalizacion() {
        val exercise =
            Exercise.OrderWords(
                id = ejercicioId("ow"),
                tokens = listOf("Good", "morning", "teacher", "!"),
                answerTokens = listOf("Good", "morning", "teacher"),
            )
        assertEquals(true, ExerciseGrader.isCorrect(exercise, ExerciseAnswer.TokenSequence(listOf("Good", "morning", "teacher"))))
        assertEquals(true, ExerciseGrader.isCorrect(exercise, ExerciseAnswer.TokenSequence(listOf("good", "morning", "teacher!"))))
        assertEquals(false, ExerciseGrader.isCorrect(exercise, ExerciseAnswer.TokenSequence(listOf("morning", "good", "teacher"))))
    }

    @Test
    fun match_pairs_exige_mapeo_canonico_identico() {
        val exercise =
            Exercise.MatchPairs(
                id = ejercicioId("mp"),
                pairs =
                    listOf(
                        Exercise.MatchPair(left = "perro", right = "dog"),
                        Exercise.MatchPair(left = "gato", right = "cat"),
                        Exercise.MatchPair(left = "pájaro", right = "bird"),
                    ),
            )
        assertEquals(true, ExerciseGrader.isCorrect(exercise, ExerciseAnswer.MatchedPairs(mapOf(0 to 0, 1 to 1, 2 to 2))))
        assertEquals(false, ExerciseGrader.isCorrect(exercise, ExerciseAnswer.MatchedPairs(mapOf(0 to 1, 1 to 0, 2 to 2))))
        assertEquals(false, ExerciseGrader.isCorrect(exercise, ExerciseAnswer.MatchedPairs(mapOf(0 to 0, 1 to 1))))
    }

    @Test
    fun listen_type_normaliza_puntuacion_japonesa() {
        val exercise =
            Exercise.ListenType(
                id = ejercicioId("lt"),
                transcription = "わたし は 日本人 です。",
                audioText = "わたし は 日本人 です。",
            )
        assertEquals(true, ExerciseGrader.isCorrect(exercise, ExerciseAnswer.TypedText("わたし は 日本人 です")))
        assertEquals(false, ExerciseGrader.isCorrect(exercise, ExerciseAnswer.TypedText("わたしは日本人です。")))
        assertEquals(false, ExerciseGrader.isCorrect(exercise, ExerciseAnswer.TypedText("わたし は 犬 です")))
    }

    @Test
    fun answer_type_inesperado_es_incorrecto_sin_excepciones() {
        val exercise =
            Exercise.MultipleChoice(
                id = ejercicioId("mc"),
                question = "q",
                options = listOf("a"),
                correctIndex = 0,
            )
        assertEquals(false, ExerciseGrader.isCorrect(exercise, ExerciseAnswer.TokenSequence(listOf("a"))))
        assertEquals(false, ExerciseGrader.isCorrect(exercise, ExerciseAnswer.TypedText("a")))
        assertEquals(
            false,
            ExerciseGrader.isCorrect(exercise, ExerciseAnswer.MatchedPairs(mapOf(0 to 0))),
        )
    }

    // ---- FreeSpeakingHeuristicGrader (F1.1, ADR-019) ----

    @Test
    fun free_speaking_transcripcion_vacia_no_pasa() {
        val feedback = evaluate("")
        assertFalse(feedback.passes)
        assertTrue((feedback.verdict as FreeSpeakingVerdict.NeedsWork).reasons.contains(FreeSpeakingReason.TOO_SHORT))
    }

    @Test
    fun free_speaking_bajo_min_tokens_marca_too_short() {
        val feedback = evaluate("a b c d e", minTokens = 6)
        assertFalse(feedback.passes)
        assertTrue((feedback.verdict as FreeSpeakingVerdict.NeedsWork).reasons.contains(FreeSpeakingReason.TOO_SHORT))
    }

    @Test
    fun free_speaking_justo_min_tokens_no_marca_too_short() {
        val feedback = evaluate("a b c d e", minTokens = 5)
        assertFalse((feedback.verdict as FreeSpeakingVerdict.NeedsWork).reasons.contains(FreeSpeakingReason.TOO_SHORT))
    }

    @Test
    fun free_speaking_todos_los_clusters_cubiertos_pasa() {
        val feedback = evaluate("deadline split", minTokens = 2)
        assertEquals(2, feedback.coveredClusters)
        assertEquals(2, feedback.totalClusters)
        assertTrue(feedback.passes)
    }

    @Test
    fun free_speaking_sinonimo_cubre_cluster() {
        val feedback =
            evaluate(
                "I am glad about the plan",
                minTokens = 2,
                conceptClusters = listOf(listOf("happy", "glad")),
            )
        assertTrue(feedback.passes)
        assertEquals(1, feedback.coveredClusters)
    }

    @Test
    fun free_speaking_homofono_con_una_letra_de_distancia_cubre_cluster() {
        val feedback = evaluate("dedline", minTokens = 1, conceptClusters = listOf(listOf("deadline")))
        assertTrue(feedback.passes)
        assertEquals(1, feedback.coveredClusters)
    }

    @Test
    fun free_speaking_distancia_mayor_a_dos_no_cubre_cluster() {
        val feedback = evaluate("dddddd", minTokens = 1, conceptClusters = listOf(listOf("deadline")))
        assertFalse(feedback.passes)
        assertTrue(
            (feedback.verdict as FreeSpeakingVerdict.NeedsWork).reasons.contains(FreeSpeakingReason.LOW_COVERAGE),
        )
    }

    @Test
    fun free_speaking_un_cluster_de_tres_no_alcanza_cobertura() {
        val feedback =
            evaluate(
                "a1 x y",
                minTokens = 3,
                conceptClusters = listOf(listOf("a1"), listOf("b2"), listOf("c3")),
            )
        assertFalse(feedback.passes)
        assertEquals(1, feedback.coveredClusters)
        assertTrue(
            (feedback.verdict as FreeSpeakingVerdict.NeedsWork).reasons.contains(FreeSpeakingReason.LOW_COVERAGE),
        )
    }

    @Test
    fun free_speaking_grammar_ref_detectada_por_patron_conditional() {
        val ref = GrammarPointId("gp-cond")
        val feedback =
            evaluate(
                "if we split the delivery",
                minTokens = 1,
                conceptClusters = listOf(listOf("split")),
                grammarRuleRefs = listOf(ref),
                grammarPoints = listOf(GrammarPoint(ref, "Mixed conditionals")),
            )
        assertTrue(feedback.passes)
        assertEquals(listOf(ref), feedback.detectedGrammarRefs)
    }

    @Test
    fun free_speaking_omision_de_gramatica_marca_missing_grammar() {
        val ref = GrammarPointId("gp-cond")
        val feedback =
            evaluate(
                "we cannot move the date",
                minTokens = 1,
                conceptClusters = listOf(listOf("move")),
                grammarRuleRefs = listOf(ref),
                grammarPoints = listOf(GrammarPoint(ref, "Mixed conditionals")),
            )
        assertFalse(feedback.passes)
        assertTrue(
            (feedback.verdict as FreeSpeakingVerdict.NeedsWork).reasons.contains(FreeSpeakingReason.MISSING_GRAMMAR),
        )
    }

    @Test
    fun free_speaking_label_sin_patron_no_se_exige() {
        val ref = GrammarPointId("gp-unknown")
        val feedback =
            evaluate(
                "we finished on time",
                minTokens = 1,
                conceptClusters = listOf(listOf("finished")),
                grammarRuleRefs = listOf(ref),
                grammarPoints = listOf(GrammarPoint(ref, "Pragmatics of turn-taking")),
            )
        assertTrue(feedback.passes)
    }

    @Test
    fun free_speaking_hedging_detecta_im_afraid() {
        val ref = GrammarPointId("gp-hedge")
        val feedback =
            evaluate(
                "I'm afraid we can't",
                minTokens = 1,
                conceptClusters = listOf(listOf("afraid")),
                grammarRuleRefs = listOf(ref),
                grammarPoints = listOf(GrammarPoint(ref, "Hedging")),
            )
        assertTrue(feedback.passes)
        assertEquals(listOf(ref), feedback.detectedGrammarRefs)
    }

    @Test
    fun free_speaking_wpm_bajo_minimo_marca_rango() {
        val feedback =
            evaluate(
                "one",
                millis = 60_000L,
                minTokens = 1,
                conceptClusters = listOf(listOf("one")),
                minWpm = 50.0,
            )
        assertFalse(feedback.passes)
        assertTrue(
            (feedback.verdict as FreeSpeakingVerdict.NeedsWork).reasons.contains(FreeSpeakingReason.WPM_OUT_OF_RANGE),
        )
    }

    @Test
    fun free_speaking_wpm_sobre_maximo_marca_rango() {
        val feedback =
            evaluate(
                "one",
                millis = 60_000L,
                minTokens = 1,
                conceptClusters = listOf(listOf("one")),
                maxWpm = 0.5,
            )
        assertFalse(feedback.passes)
        assertTrue(
            (feedback.verdict as FreeSpeakingVerdict.NeedsWork).reasons.contains(FreeSpeakingReason.WPM_OUT_OF_RANGE),
        )
    }

    @Test
    fun free_speaking_wpm_dentro_de_rango_pasa() {
        val feedback =
            evaluate(
                "one",
                millis = 60_000L,
                minTokens = 1,
                conceptClusters = listOf(listOf("one")),
                minWpm = 1.0,
                maxWpm = 5.0,
            )
        assertTrue(feedback.passes)
        assertEquals(1.0, feedback.wpm, 0.001)
    }

    @Test
    fun free_speaking_autoevaluacion_baja_no_pasa() {
        val feedback = evaluate("deadline split", minTokens = 2, selfAssessmentScore = 0.4)
        assertFalse(feedback.passes)
        assertTrue(
            (feedback.verdict as FreeSpeakingVerdict.NeedsWork).reasons.contains(FreeSpeakingReason.LOW_SELF_ASSESSMENT),
        )
    }

    @Test
    fun free_speaking_autoevaluacion_alta_pasa() {
        val feedback = evaluate("deadline split", minTokens = 2, selfAssessmentScore = 0.8)
        assertTrue(feedback.passes)
    }

    @Test
    fun free_speaking_sin_autoevaluacion_pasa() {
        val feedback = evaluate("deadline split", minTokens = 2, selfAssessmentScore = null)
        assertTrue(feedback.passes)
    }

    @Test
    fun free_speaking_respuesta_completa_valida_pasa() {
        val cond = GrammarPointId("gp-cond")
        val hedge = GrammarPointId("gp-hedge")
        val feedback =
            evaluate(
                "I'm afraid we cannot move the deadline, but we could split the delivery",
                millis = 10_000L,
                minTokens = 5,
                conceptClusters = listOf(listOf("deadline"), listOf("split")),
                grammarRuleRefs = listOf(hedge, cond),
                grammarPoints =
                    listOf(
                        GrammarPoint(cond, "Mixed conditionals"),
                        GrammarPoint(hedge, "Hedging"),
                    ),
                minWpm = 70.0,
                maxWpm = 160.0,
            )
        assertTrue(feedback.passes)
        assertEquals(13, feedback.tokenCount)
        assertTrue(feedback.wpm > 70.0)
        assertTrue(feedback.wpm < 160.0)
    }

    @Test
    fun free_speaking_mayusculas_y_puntuacion_no_rompen_deteccion() {
        val ref = GrammarPointId("gp-cond")
        val feedback =
            evaluate(
                "IF WE SPLIT, WE COULD!",
                minTokens = 1,
                conceptClusters = listOf(listOf("split")),
                grammarRuleRefs = listOf(ref),
                grammarPoints = listOf(GrammarPoint(ref, "Conditionals")),
            )
        assertTrue(feedback.passes)
        assertEquals(listOf(ref), feedback.detectedGrammarRefs)
    }

    @Test
    fun free_speaking_contracciones_cuentan_un_token() {
        val feedback = evaluate("I'm afraid", minTokens = 2, conceptClusters = listOf(listOf("afraid")))
        assertEquals(2, feedback.tokenCount)
        assertTrue(feedback.passes)
    }

    @Test
    fun free_speaking_duracion_cero_da_wpm_cero_sin_crashear() {
        val plain = evaluate("one", millis = 0L, minTokens = 1, conceptClusters = listOf(listOf("one")))
        assertEquals(0.0, plain.wpm, 0.0)
        assertTrue(plain.passes)

        val ranged =
            evaluate(
                "one",
                millis = 0L,
                minTokens = 1,
                conceptClusters = listOf(listOf("one")),
                minWpm = 1.0,
            )
        assertFalse(ranged.passes)
    }

    @Test
    fun free_speaking_clusters_en_otro_alfabeto_se_cubren() {
        val feedback = evaluate("こんにちは", minTokens = 1, conceptClusters = listOf(listOf("こんにちは")))
        assertEquals(1, feedback.coveredClusters)
        assertTrue(feedback.passes)
    }

    @Test
    fun free_speaking_dos_grammar_refs_ambas_detectadas() {
        val cond = GrammarPointId("gp-cond")
        val hedge = GrammarPointId("gp-hedge")
        val feedback =
            evaluate(
                "I'm afraid if we move the date we could lose the client",
                minTokens = 1,
                conceptClusters = listOf(listOf("move")),
                grammarRuleRefs = listOf(cond, hedge),
                grammarPoints =
                    listOf(
                        GrammarPoint(cond, "Conditionals"),
                        GrammarPoint(hedge, "Hedging"),
                    ),
            )
        assertEquals(setOf(cond, hedge), feedback.detectedGrammarRefs.toSet())
        assertTrue(feedback.passes)
    }

    @Test
    fun free_speaking_una_de_dos_grammar_refs_falta_marca_missing() {
        val cond = GrammarPointId("gp-cond")
        val hedge = GrammarPointId("gp-hedge")
        val feedback =
            evaluate(
                "if we move the date we could lose the client",
                minTokens = 1,
                conceptClusters = listOf(listOf("move")),
                grammarRuleRefs = listOf(cond, hedge),
                grammarPoints =
                    listOf(
                        GrammarPoint(cond, "Conditionals"),
                        GrammarPoint(hedge, "Hedging"),
                    ),
            )
        assertFalse(feedback.passes)
        assertTrue(
            (feedback.verdict as FreeSpeakingVerdict.NeedsWork).reasons.contains(FreeSpeakingReason.MISSING_GRAMMAR),
        )
    }

    @Test
    fun free_speaking_cluster_vacio_no_cubre_y_no_crashea() {
        val feedback = evaluate("word", minTokens = 1, conceptClusters = listOf(listOf("")))
        assertFalse(feedback.passes)
        assertEquals(0, feedback.coveredClusters)
    }

    private fun evaluate(
        transcript: String,
        millis: Long = 6_000L,
        minTokens: Int = 5,
        conceptClusters: List<List<String>> = listOf(listOf("deadline"), listOf("split")),
        grammarRuleRefs: List<GrammarPointId> = emptyList(),
        grammarPoints: List<GrammarPoint> = emptyList(),
        minWpm: Double? = null,
        maxWpm: Double? = null,
        selfAssessmentScore: Double? = null,
    ): FreeSpeakingFeedback =
        FreeSpeakingHeuristicGrader.evaluate(
            exercise =
                Exercise.FreeSpeakingPrompt(
                    id = ExerciseId("fs"),
                    prompt = "Responde oralmente",
                    minTokens = minTokens,
                    conceptClusters = conceptClusters,
                    modelAnswer = "Modelo",
                    selfAssessmentCriteria = listOf("¿Bien?"),
                    grammarRuleRefs = grammarRuleRefs,
                    minWpm = minWpm,
                    maxWpm = maxWpm,
                ),
            grammarPoints = grammarPoints,
            response =
                SpokenFreeResponse(
                    transcript = transcript,
                    durationMillis = millis,
                    selfAssessmentScore = selfAssessmentScore,
                ),
        )
}

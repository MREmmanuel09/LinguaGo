package dev.linguago.core.domain.logic

import dev.linguago.core.domain.model.Exercise
import dev.linguago.core.domain.model.ExerciseId
import dev.linguago.core.domain.model.GrammarPointId
import dev.linguago.core.domain.usecase.ExerciseAnswer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** GradingResult estructurado y mecánicas B2+ del grader (F1.2, ADR-018). */
public class ExerciseGraderFeedbackTest {
    private fun ejercicioId(id: String): ExerciseId = ExerciseId(id)

    @Test
    public fun grade_incorrecto_adjunta_regla_y_explicacion() {
        val exercise = multipleChoiceConFeedback(ruleRef = GrammarPointId("gp-mixed"))
        val result = ExerciseGrader.grade(exercise, ExerciseAnswer.OptionPicked(1))
        assertFalse(result.isCorrect)
        assertEquals(GrammarPointId("gp-mixed"), result.matchedErrorRuleId)
        assertEquals("Explicación del distractor.", result.diagnosticFeedback)
    }

    @Test
    public fun grade_correcto_no_adjunta_feedback() {
        val exercise = multipleChoiceConFeedback(ruleRef = GrammarPointId("gp-mixed"))
        val result = ExerciseGrader.grade(exercise, ExerciseAnswer.OptionPicked(0))
        assertTrue(result.isCorrect)
        assertEquals(null, result.matchedErrorRuleId)
        assertEquals(null, result.diagnosticFeedback)
        assertEquals(null, result.successExplanation)
        assertEquals(null, result.matchedSuccessRuleId)
    }

    @Test
    public fun grade_correcto_adjunta_explicacion_de_acierto() {
        val exercise =
            multipleChoiceConFeedback(ruleRef = GrammarPointId("gp-mixed"))
                .copy(
                    feedbackOnSuccess =
                        Exercise.FeedbackOnSuccess(
                            explanation = "La opción correcta mantiene la condicional mixta.",
                            ruleRef = GrammarPointId("gp-mixed"),
                        ),
                )
        val result = ExerciseGrader.grade(exercise, ExerciseAnswer.OptionPicked(0))

        assertTrue(result.isCorrect)
        assertEquals("La opción correcta mantiene la condicional mixta.", result.successExplanation)
        assertEquals(GrammarPointId("gp-mixed"), result.matchedSuccessRuleId)
        assertEquals(null, result.diagnosticFeedback)
    }

    @Test
    public fun grade_sin_feedback_on_error_no_inventa_diagnostico() {
        val exercise =
            Exercise.MultipleChoice(
                id = ejercicioId("mc-sin-feedback"),
                question = "q",
                options = listOf("a", "b"),
                correctIndex = 0,
            )
        val result = ExerciseGrader.grade(exercise, ExerciseAnswer.OptionPicked(1))
        assertFalse(result.isCorrect)
        assertEquals(null, result.matchedErrorRuleId)
        assertEquals(null, result.diagnosticFeedback)
    }

    @Test
    public fun grade_sin_rule_ref_deja_la_regla_en_null() {
        val exercise = multipleChoiceConFeedback(ruleRef = null)
        val result = ExerciseGrader.grade(exercise, ExerciseAnswer.OptionPicked(1))
        assertFalse(result.isCorrect)
        assertEquals(null, result.matchedErrorRuleId)
        assertEquals("Explicación del distractor.", result.diagnosticFeedback)
    }

    @Test
    public fun grade_audio_only_prompt_adjunta_feedback_al_fallar() {
        val exercise =
            Exercise.AudioOnlyPrompt(
                id = ejercicioId("ao"),
                audioText = "I'm afraid we can't bring the deadline forward.",
                options = listOf("A discount", "A phased delivery"),
                correctIndex = 1,
                feedbackOnError =
                    Exercise.FeedbackOnError(
                        type = Exercise.FeedbackErrorType.COMPREHENSION,
                        explanation = "Rechaza el plazo pero ofrece fases.",
                        ruleRef = GrammarPointId("gp-hedging"),
                    ),
            )
        val correct = ExerciseGrader.grade(exercise, ExerciseAnswer.OptionPicked(1))
        assertTrue(correct.isCorrect)
        val failed = ExerciseGrader.grade(exercise, ExerciseAnswer.OptionPicked(0))
        assertFalse(failed.isCorrect)
        assertEquals(GrammarPointId("gp-hedging"), failed.matchedErrorRuleId)
        assertEquals("Rechaza el plazo pero ofrece fases.", failed.diagnosticFeedback)
    }

    @Test
    public fun grade_semantic_match_adjunta_feedback_al_fallar() {
        val exercise =
            Exercise.SemanticMatch(
                id = ejercicioId("sm"),
                pairs =
                    listOf(
                        Exercise.SemanticPair("That's a fair point.", "Conceder"),
                        Exercise.SemanticPair("I'm afraid that won't work.", "Rechazo atenuado"),
                    ),
                feedbackOnError =
                    Exercise.FeedbackOnError(
                        type = Exercise.FeedbackErrorType.REGISTER,
                        explanation = "Distingue si concede, rechaza o aplaza.",
                        ruleRef = GrammarPointId("gp-hedging"),
                    ),
            )
        val correct = ExerciseGrader.grade(exercise, ExerciseAnswer.MatchedPairs(mapOf(0 to 0, 1 to 1)))
        assertTrue(correct.isCorrect)
        val failed = ExerciseGrader.grade(exercise, ExerciseAnswer.MatchedPairs(mapOf(0 to 1, 1 to 0)))
        assertFalse(failed.isCorrect)
        assertEquals(GrammarPointId("gp-hedging"), failed.matchedErrorRuleId)
    }

    @Test
    public fun grade_listen_choose_adjunta_feedback_al_fallar() {
        val exercise =
            Exercise.ListenChoose(
                id = ejercicioId("lc"),
                options = listOf("x", "y"),
                correctIndex = 1,
                audioText = "y",
                feedbackOnError =
                    Exercise.FeedbackOnError(
                        type = Exercise.FeedbackErrorType.COMPREHENSION,
                        explanation = "Escucha de nuevo la frase.",
                    ),
            )
        val result = ExerciseGrader.grade(exercise, ExerciseAnswer.OptionPicked(0))
        assertFalse(result.isCorrect)
        assertEquals("Escucha de nuevo la frase.", result.diagnosticFeedback)
    }

    @Test
    public fun grade_token_sequence_adjunta_feedback_al_fallar() {
        val exercise =
            Exercise.Translate(
                id = ejercicioId("tr"),
                sourcePhrase = "la manzana roja",
                correctTokens = listOf("the", "red", "apple"),
                feedbackOnError =
                    Exercise.FeedbackOnError(
                        type = Exercise.FeedbackErrorType.WORD_ORDER,
                        explanation = "En inglés el adjetivo va antes del sustantivo.",
                    ),
            )
        val result = ExerciseGrader.grade(exercise, ExerciseAnswer.TokenSequence(listOf("red", "apple", "the")))
        assertFalse(result.isCorrect)
        assertEquals("En inglés el adjetivo va antes del sustantivo.", result.diagnosticFeedback)
    }

    @Test
    public fun grade_typed_text_adjunta_feedback_al_fallar() {
        val exercise =
            Exercise.ListenType(
                id = ejercicioId("lt-f"),
                transcription = "Good morning",
                audioText = "Good morning",
                feedbackOnError =
                    Exercise.FeedbackOnError(
                        type = Exercise.FeedbackErrorType.SPELLING,
                        explanation = "Revisa la ortografía de morning.",
                    ),
            )
        val result = ExerciseGrader.grade(exercise, ExerciseAnswer.TypedText("Good mourning"))
        assertFalse(result.isCorrect)
        assertEquals("Revisa la ortografía de morning.", result.diagnosticFeedback)
    }

    @Test
    public fun grade_spoken_text_adjunta_feedback_al_fallar() {
        val exercise =
            Exercise.SpeakRepeat(
                id = ejercicioId("sr"),
                targetPhrase = "Nice to meet you",
                feedbackOnError =
                    Exercise.FeedbackOnError(
                        type = Exercise.FeedbackErrorType.PRONUNCIATION,
                        explanation = "Pronuncia cada palabra completa.",
                    ),
            )
        val result = ExerciseGrader.grade(exercise, ExerciseAnswer.SpokenText("nice to meet"))
        assertFalse(result.isCorrect)
        assertEquals("Pronuncia cada palabra completa.", result.diagnosticFeedback)
    }

    @Test
    public fun cloze_correcto_con_todos_los_huecos() {
        assertTrue(
            ExerciseGrader.isCorrect(
                clozeExercise(),
                ExerciseAnswer.ClozeFilled(mapOf("b1" to "narrow", "b2" to "would have")),
            ),
        )
    }

    @Test
    public fun cloze_normaliza_mayusculas_puntuacion_y_espacios() {
        assertTrue(
            ExerciseGrader.isCorrect(
                clozeExercise(),
                ExerciseAnswer.ClozeFilled(mapOf("b1" to " Narrow, ", "b2" to "Would  Have")),
            ),
        )
    }

    @Test
    public fun cloze_hueco_equivocado_es_incorrecto() {
        assertFalse(
            ExerciseGrader.isCorrect(
                clozeExercise(),
                ExerciseAnswer.ClozeFilled(mapOf("b1" to "narrow", "b2" to "will have")),
            ),
        )
    }

    @Test
    public fun cloze_hueco_faltante_es_incorrecto() {
        assertFalse(
            ExerciseGrader.isCorrect(
                clozeExercise(),
                ExerciseAnswer.ClozeFilled(mapOf("b1" to "narrow")),
            ),
        )
    }

    @Test
    public fun cloze_vacio_es_incorrecto() {
        assertFalse(ExerciseGrader.isCorrect(clozeExercise(), ExerciseAnswer.ClozeFilled(emptyMap())))
    }

    @Test
    public fun cloze_ignora_huecos_desconocidos() {
        assertTrue(
            ExerciseGrader.isCorrect(
                clozeExercise(),
                ExerciseAnswer.ClozeFilled(mapOf("b1" to "narrow", "b2" to "would have", "b9" to "basura")),
            ),
        )
    }

    @Test
    public fun cloze_grade_incorrecto_adjunta_feedback() {
        val result =
            ExerciseGrader.grade(
                clozeExercise(),
                ExerciseAnswer.ClozeFilled(mapOf("b1" to "narrow", "b2" to "will have")),
            )
        assertFalse(result.isCorrect)
        assertEquals(GrammarPointId("gp-mixed-conditionals"), result.matchedErrorRuleId)
        assertEquals("La condicional mixta combina presente y consecuencia.", result.diagnosticFeedback)
    }

    @Test
    public fun cloze_answer_en_ejercicio_no_cloze_es_incorrecto() {
        val exercise =
            Exercise.MultipleChoice(
                id = ejercicioId("mc-cloze-ajeno"),
                question = "q",
                options = listOf("a"),
                correctIndex = 0,
            )
        assertFalse(ExerciseGrader.isCorrect(exercise, ExerciseAnswer.ClozeFilled(mapOf("b1" to "a"))))
    }

    @Test
    public fun option_picked_en_cloze_es_incorrecto() {
        assertFalse(ExerciseGrader.isCorrect(clozeExercise(), ExerciseAnswer.OptionPicked(0)))
    }

    private fun multipleChoiceConFeedback(ruleRef: GrammarPointId?): Exercise.MultipleChoice =
        Exercise.MultipleChoice(
            id = ejercicioId("mc-feedback"),
            question = "If we ___ the scope, we would have more time.",
            options = listOf("narrow", "narrowed"),
            correctIndex = 0,
            feedbackOnError =
                Exercise.FeedbackOnError(
                    type = Exercise.FeedbackErrorType.GRAMMAR,
                    explanation = "Explicación del distractor.",
                    ruleRef = ruleRef,
                ),
        )

    private fun clozeExercise(): Exercise.ClozeDragDrop =
        Exercise.ClozeDragDrop(
            id = ejercicioId("cloze"),
            passage = "If we {{b1}} the scope now, we {{b2}} more time for testing.",
            blanks =
                listOf(
                    Exercise.ClozeBlank(id = "b1", answer = "narrow", options = listOf("narrow", "narrowed")),
                    Exercise.ClozeBlank(id = "b2", answer = "would have", options = listOf("will have", "would have")),
                ),
            feedbackOnError =
                Exercise.FeedbackOnError(
                    type = Exercise.FeedbackErrorType.GRAMMAR,
                    explanation = "La condicional mixta combina presente y consecuencia.",
                    ruleRef = GrammarPointId("gp-mixed-conditionals"),
                ),
        )
}

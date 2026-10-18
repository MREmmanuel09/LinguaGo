package dev.linguago.core.domain.model

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Contrato de serialización del contenido v2 (ADR-018): los cursos v1 deben
 * seguir decodificando con valores por defecto y las cuatro variantes nuevas
 * de ejercicio deben conservar sus campos tipados.
 */
class CourseV2SerializationTest {
    private val json = Json

    @Test
    fun curso_v1_decodifica_con_defaults_v2() {
        val course = json.decodeFromString(Course.serializer(), V1_COURSE)
        val lesson =
            course.levels
                .first()
                .units
                .first()
                .lessons
                .first()

        assertEquals(1, course.schemaVersion)
        assertNull(course.locale)
        assertEquals(CefrLevel.A1, lesson.cefrLevel)
        assertNull(lesson.scenario)
        assertNull(lesson.canDo)
        assertTrue(lesson.grammarPoints.isEmpty())
        assertNull(lesson.vocabFrequencyBand)
        assertTrue(lesson.culturalNotes.isEmpty())
        assertNull(lesson.exercises.first().feedbackOnError)
    }

    @Test
    fun curso_v2_decodifica_locale_y_metadatos_b2() {
        val course = json.decodeFromString(Course.serializer(), V2_COURSE)
        val lesson =
            course.levels
                .first()
                .units
                .first()
                .lessons
                .first()

        assertEquals(2, course.schemaVersion)
        assertEquals(CourseLocale.ENGLISH_GB, course.locale)
        assertEquals(CefrLevel.B2, lesson.cefrLevel)
        assertEquals("Negociar un plazo", lesson.scenario)
        assertEquals("Puedo negociar y justificar concesiones.", lesson.canDo)
        assertEquals(VocabFrequencyBand.BAND_3000_5000, lesson.vocabFrequencyBand)
        assertEquals(GrammarPointId("gp-hedging"), lesson.grammarPoints.first().id)
        assertEquals("play it by ear", lesson.culturalNotes.first().idiom)
    }

    @Test
    fun audio_only_prompt_decodifica() {
        val exercise =
            json.decodeFromString(Exercise.serializer(), AUDIO_ONLY_PROMPT) as Exercise.AudioOnlyPrompt

        assertEquals("e-audio", exercise.id.value)
        assertEquals("I'm afraid we can't.", exercise.audioText)
        assertEquals(listOf("A", "B"), exercise.options)
        assertEquals(1, exercise.correctIndex)
        assertEquals("What did you hear?", exercise.question)
        assertTrue(exercise.allowSlowPlayback)
        assertEquals(Exercise.FeedbackErrorType.COMPREHENSION, exercise.feedbackOnError?.type)
    }

    @Test
    fun semantic_match_decodifica_registro_y_matiz() {
        val exercise =
            json.decodeFromString(Exercise.serializer(), SEMANTIC_MATCH) as Exercise.SemanticMatch

        assertEquals(2, exercise.pairs.size)
        assertEquals(Exercise.SemanticRegister.FORMAL, exercise.pairs.first().register)
        assertEquals("Rechazo atenuado", exercise.pairs.first().nuance)
        assertEquals("Empareja las columnas", exercise.instructions)
    }

    @Test
    fun cloze_drag_drop_decodifica_huecos() {
        val exercise =
            json.decodeFromString(Exercise.serializer(), CLOZE_DRAG_DROP) as Exercise.ClozeDragDrop

        assertEquals("If we {{b1}} now, we {{b2}} time.", exercise.passage)
        assertEquals(2, exercise.blanks.size)
        assertEquals("b1", exercise.blanks.first().id)
        assertEquals("narrow", exercise.blanks.first().answer)
        assertTrue("narrowed" in exercise.blanks.first().options)
    }

    @Test
    fun free_speaking_prompt_decodifica_heuristica() {
        val exercise =
            json.decodeFromString(Exercise.serializer(), FREE_SPEAKING) as Exercise.FreeSpeakingPrompt

        assertEquals("Responde al cliente.", exercise.prompt)
        assertEquals(25, exercise.minTokens)
        assertEquals(2, exercise.conceptClusters.size)
        assertEquals(listOf(GrammarPointId("gp-hedging")), exercise.grammarRuleRefs)
        assertEquals(60, exercise.timeLimitSeconds)
        assertEquals(70.0, exercise.minWpm ?: 0.0, 0.0)
        assertEquals(160.0, exercise.maxWpm ?: 0.0, 0.0)
        assertEquals("I'm afraid we can't.", exercise.modelAnswer)
        assertEquals(2, exercise.selfAssessmentCriteria.size)
    }

    @Test
    fun feedback_on_error_decodifica_word_order_y_rule_ref() {
        val exercise =
            json.decodeFromString(Exercise.serializer(), FEEDBACK_ON_ERROR) as Exercise.MultipleChoice
        val feedback = exercise.feedbackOnError

        assertEquals(Exercise.FeedbackErrorType.WORD_ORDER, feedback?.type)
        assertEquals("El orden de los tokens cambia el sentido.", feedback?.explanation)
        assertEquals(GrammarPointId("gp-word-order"), feedback?.ruleRef)
    }

    @Test
    fun round_trip_lesson_v2_conserva_metadatos() {
        val lesson =
            Lesson(
                id = LessonId("lv-9-un-1-ls-1"),
                title = "Plazos",
                exercises =
                    listOf(
                        Exercise.FreeSpeakingPrompt(
                            id = ExerciseId("e-free"),
                            prompt = "Negocia el plazo.",
                            minTokens = 20,
                            conceptClusters = listOf(listOf("deadline", "schedule")),
                            modelAnswer = "I'm afraid we can't.",
                            selfAssessmentCriteria = listOf("¿Fui cortés?"),
                            grammarRuleRefs = listOf(GrammarPointId("gp-hedging")),
                            timeLimitSeconds = 45,
                            minWpm = 60.0,
                            maxWpm = 150.0,
                            feedbackOnError =
                                Exercise.FeedbackOnError(
                                    type = Exercise.FeedbackErrorType.GRAMMAR,
                                    explanation = "Falta el atenuador.",
                                    ruleRef = GrammarPointId("gp-hedging"),
                                ),
                        ),
                    ),
                cefrLevel = CefrLevel.B2,
                scenario = "Renegociar un plazo.",
                canDo = "Puedo justificar una concesión.",
                grammarPoints = listOf(GrammarPoint(GrammarPointId("gp-hedging"), "Hedging")),
                vocabFrequencyBand = VocabFrequencyBand.BAND_3000_5000,
                culturalNotes = listOf(CulturalNote("play it by ear", "Decidir sobre la marcha.")),
            )

        val encoded = json.encodeToString(Lesson.serializer(), lesson)
        val decoded = json.decodeFromString(Lesson.serializer(), encoded)

        assertEquals(lesson, decoded)
    }

    private companion object {
        private const val V1_COURSE =
            """
            {
              "id": "es-en",
              "from": "es",
              "to": "en",
              "schemaVersion": 1,
              "levels": [
                {
                  "id": "lv-1",
                  "name": "Nivel 1",
                  "units": [
                    {
                      "id": "un-1",
                      "title": "Unidad",
                      "lessons": [
                        {
                          "id": "ls-1",
                          "title": "Saludos",
                          "exercises": [
                            {
                              "type": "MultipleChoice",
                              "id": "e1",
                              "question": "¿Hola?",
                              "options": ["Hello", "Bye"],
                              "correctIndex": 0
                            }
                          ]
                        }
                      ]
                    }
                  ]
                }
              ]
            }
            """

        private const val V2_COURSE =
            """
            {
              "id": "es-en",
              "from": "es",
              "to": "en",
              "schemaVersion": 2,
              "locale": "en-GB",
              "levels": [
                {
                  "id": "lv-9",
                  "name": "B2",
                  "units": [
                    {
                      "id": "un-1",
                      "title": "Negociación",
                      "lessons": [
                        {
                          "id": "ls-1",
                          "title": "Plazos",
                          "cefrLevel": "B2",
                          "scenario": "Negociar un plazo",
                          "canDo": "Puedo negociar y justificar concesiones.",
                          "grammarPoints": [
                            {"id": "gp-hedging", "label": "Hedging"}
                          ],
                          "vocabFrequencyBand": "3000-5000",
                          "culturalNotes": [
                            {"idiom": "play it by ear", "note": "Decidir sobre la marcha."}
                          ],
                          "exercises": [
                            {
                              "type": "MultipleChoice",
                              "id": "e1",
                              "question": "¿Cortés?",
                              "options": ["I'm afraid...", "No."],
                              "correctIndex": 0,
                              "feedbackOnError": {
                                "type": "register",
                                "explanation": "No.",
                                "ruleRef": "gp-hedging"
                              }
                            }
                          ]
                        }
                      ]
                    }
                  ]
                }
              ]
            }
            """

        private const val AUDIO_ONLY_PROMPT =
            """
            {
              "type": "AudioOnlyPrompt",
              "id": "e-audio",
              "audioText": "I'm afraid we can't.",
              "question": "What did you hear?",
              "options": ["A", "B"],
              "correctIndex": 1,
              "allowSlowPlayback": true,
              "feedbackOnError": {
                "type": "comprehension",
                "explanation": "No se ofreció un plazo.",
                "ruleRef": "gp-hedging"
              }
            }
            """

        private const val SEMANTIC_MATCH =
            """
            {
              "type": "SemanticMatch",
              "id": "e-sem",
              "instructions": "Empareja las columnas",
              "pairs": [
                {
                  "left": "I'm afraid that won't work.",
                  "right": "Rechazo atenuado",
                  "register": "formal",
                  "nuance": "Rechazo atenuado"
                },
                {"left": "Fair enough.", "right": "Concesión"}
              ]
            }
            """

        private const val CLOZE_DRAG_DROP =
            """
            {
              "type": "ClozeDragDrop",
              "id": "e-cloze",
              "passage": "If we {{b1}} now, we {{b2}} time.",
              "blanks": [
                {"id": "b1", "answer": "narrow", "options": ["narrow", "narrowed"]},
                {"id": "b2", "answer": "would have", "options": ["will have", "would have"]}
              ]
            }
            """

        private const val FREE_SPEAKING =
            """
            {
              "type": "FreeSpeakingPrompt",
              "id": "e-free",
              "prompt": "Responde al cliente.",
              "minTokens": 25,
              "conceptClusters": [
                ["deadline", "schedule"],
                ["phase", "stages"]
              ],
              "grammarRuleRefs": ["gp-hedging"],
              "timeLimitSeconds": 60,
              "minWpm": 70,
              "maxWpm": 160,
              "modelAnswer": "I'm afraid we can't.",
              "selfAssessmentCriteria": [
                "¿Rechacé con cortesía?",
                "¿Propuse una alternativa?"
              ]
            }
            """

        private const val FEEDBACK_ON_ERROR =
            """
            {
              "type": "MultipleChoice",
              "id": "e-order",
              "question": "Elige el orden correcto",
              "options": ["I am here", "Here I am"],
              "correctIndex": 0,
              "feedbackOnError": {
                "type": "word_order",
                "explanation": "El orden de los tokens cambia el sentido.",
                "ruleRef": "gp-word-order"
              }
            }
            """
    }
}

package dev.linguago.core.content

import dev.linguago.core.domain.model.CefrLevel
import dev.linguago.core.domain.model.Course
import dev.linguago.core.domain.model.CourseId
import dev.linguago.core.domain.model.CulturalNote
import dev.linguago.core.domain.model.Exercise
import dev.linguago.core.domain.model.ExerciseId
import dev.linguago.core.domain.model.GrammarPoint
import dev.linguago.core.domain.model.GrammarPointId
import dev.linguago.core.domain.model.Language
import dev.linguago.core.domain.model.Lesson
import dev.linguago.core.domain.model.LessonId
import dev.linguago.core.domain.model.Level
import dev.linguago.core.domain.model.LevelId
import dev.linguago.core.domain.model.Unit
import dev.linguago.core.domain.model.UnitId
import dev.linguago.core.domain.model.VocabFrequencyBand
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Reglas del validador estructural: falla al primer problema con una
 * descripción del contenido defectuoso y acepta contenido sano.
 */
class CourseValidatorTest {
    @Test
    fun admite_curso_sano() {
        assertEquals(
            CourseValidation.Valid,
            CourseValidator.validate(cursoCon(ejercicioId = "e1", ejercicio = mcCorrecta())),
        )
    }

    @Test
    fun rechaza_curso_sin_niveles() {
        val empty =
            Course(
                id = CourseId("demo"),
                fromLanguage = Language.SPANISH,
                toLanguage = Language.ENGLISH,
                levels = emptyList(),
            )
        assertEquals(
            true,
            CourseValidator.validate(empty) is CourseValidation.Invalid,
        )
    }

    @Test
    fun rechaza_ids_duplicados_en_la_jerarquia() {
        val lesson =
            Lesson(
                id = LessonId("ls-1"),
                title = "L",
                exercises =
                    listOf(
                        mcCorrecta(id = "e1"),
                        mcCorrecta(id = "e1"),
                    ),
            )
        val course =
            Course(
                id = CourseId("demo"),
                fromLanguage = Language.SPANISH,
                toLanguage = Language.ENGLISH,
                levels = listOf(ruta(lesson)),
            )
        val validation = CourseValidator.validate(course)

        assertEquals(true, validation is CourseValidation.Invalid)
        assertEquals(
            true,
            (validation as CourseValidation.Invalid).reason.contains("duplicado"),
        )
    }

    @Test
    fun rechaza_correct_index_fuera_de_rango() {
        val exercise =
            Exercise.MultipleChoice(
                id = ExerciseId("e1"),
                question = "q",
                options = listOf("a", "b"),
                correctIndex = 2,
            )
        waits_for_invalid(cursoUnEjercicio(id = "e1", exercise = exercise), "correctIndex")
    }

    @Test
    fun rechaza_respuesta_kanji_no_presente_en_opciones() {
        val exercise =
            Exercise.KanjiReading(
                id = ExerciseId("e1"),
                kanji = "人",
                readingOptions = listOf("ひと", "じん"),
                correctReading = "にん",
            )
        waits_for_invalid(cursoUnEjercicio(id = "e1", exercise = exercise), "entre opciones")
    }

    @Test
    fun rechaza_distractores_solapados_con_correctos() {
        val exercise =
            Exercise.Translate(
                id = ExerciseId("e1"),
                sourcePhrase = "s",
                correctTokens = listOf("red", "apple"),
                distractorTokens = listOf("apple"),
            )
        waits_for_invalid(cursoUnEjercicio(id = "e1", exercise = exercise), "distractores")
    }

    @Test
    fun rechaza_respuesta_ordenar_que_no_cabe_en_tokens() {
        val exercise =
            Exercise.OrderWords(
                id = ExerciseId("e1"),
                tokens = listOf("the", "apple"),
                answerTokens = listOf("the", "green", "apple"),
            )
        waits_for_invalid(cursoUnEjercicio(id = "e1", exercise = exercise), "answerTokens")
    }

    @Test
    fun admite_ordenar_con_repeticiones_y_distractores() {
        val exercise =
            Exercise.OrderWords(
                id = ExerciseId("e1"),
                tokens = listOf("the", "the", "cat", "dog"),
                answerTokens = listOf("the", "the", "cat"),
            )
        val course = cursoUnEjercicio(id = "e1", exercise = exercise)
        assertEquals(CourseValidation.Valid, CourseValidator.validate(course))
    }

    @Test
    fun rechaza_pares_duplicados_por_lado() {
        val exercise =
            Exercise.MatchPairs(
                id = ExerciseId("e1"),
                pairs =
                    listOf(
                        Exercise.MatchPair(left = "uno", right = "one"),
                        Exercise.MatchPair(left = "dos", right = "one"),
                    ),
            )
        waits_for_invalid(cursoUnEjercicio(id = "e1", exercise = exercise), "únicos")
    }

    @Test
    fun rechaza_dictado_sobre_frase_vacia() {
        val exercise =
            Exercise.ListenType(
                id = ExerciseId("e1"),
                transcription = "   ",
                audioText = "hola",
            )
        waits_for_invalid(cursoUnEjercicio(id = "e1", exercise = exercise), "audio")
    }

    @Test
    fun rechaza_leccion_b1_sin_scenario() {
        waits_for_invalid(
            cursoConLeccion(b2Lesson(cefrLevel = CefrLevel.B1, scenario = null)),
            "scenario",
        )
    }

    @Test
    fun rechaza_leccion_b2_sin_cando() {
        waits_for_invalid(cursoConLeccion(b2Lesson(canDo = null)), "canDo")
    }

    @Test
    fun rechaza_leccion_b2_sin_grammar_points() {
        waits_for_invalid(cursoConLeccion(b2Lesson(grammarPoints = emptyList())), "grammarPoints")
    }

    @Test
    fun rechaza_leccion_b2_sin_vocab_frequency_band() {
        waits_for_invalid(cursoConLeccion(b2Lesson(vocabFrequencyBand = null)), "vocabFrequencyBand")
    }

    @Test
    fun rechaza_leccion_b2_sin_cultural_notes() {
        waits_for_invalid(cursoConLeccion(b2Lesson(culturalNotes = emptyList())), "culturalNote")
    }

    @Test
    fun rechaza_leccion_b2_sin_free_speaking_prompt() {
        waits_for_invalid(
            cursoConLeccion(b2Lesson(exercises = listOf(mcConFeedback()))),
            "FreeSpeakingPrompt",
        )
    }

    @Test
    fun rechaza_leccion_b2_con_ejercicio_sin_feedback_on_error() {
        waits_for_invalid(
            cursoConLeccion(b2Lesson(exercises = listOf(freeSpeaking(), mcCorrecta()))),
            "feedbackOnError",
        )
    }

    @Test
    fun admite_leccion_b2_completa() {
        assertEquals(
            CourseValidation.Valid,
            CourseValidator.validate(cursoConLeccion(b2Lesson())),
        )
    }

    @Test
    fun rechaza_rule_ref_inexistente() {
        waits_for_invalid(
            cursoConLeccion(
                b2Lesson(
                    exercises =
                        listOf(
                            freeSpeaking(
                                feedbackOnError = errorFeedback(GrammarPointId("gp-missing")),
                            ),
                        ),
                ),
            ),
            "ruleRef",
        )
    }

    @Test
    fun rechaza_grammar_rule_ref_inexistente() {
        waits_for_invalid(
            cursoConLeccion(
                b2Lesson(
                    exercises =
                        listOf(
                            freeSpeaking(
                                grammarRuleRefs = listOf(GrammarPointId("gp-missing")),
                            ),
                        ),
                ),
            ),
            "grammarRuleRefs",
        )
    }

    @Test
    fun rechaza_feedback_on_error_sin_explicacion() {
        waits_for_invalid(
            cursoConLeccion(
                b2Lesson(
                    exercises =
                        listOf(
                            freeSpeaking(
                                feedbackOnError =
                                    Exercise.FeedbackOnError(
                                        type = Exercise.FeedbackErrorType.GRAMMAR,
                                        explanation = "   ",
                                    ),
                            ),
                        ),
                ),
            ),
            "explicación",
        )
    }

    @Test
    fun rechaza_grammar_point_sin_label() {
        waits_for_invalid(
            cursoConLeccion(
                b2Lesson(
                    grammarPoints =
                        listOf(
                            GrammarPoint(GrammarPointId("gp-1"), "  "),
                        ),
                ),
            ),
            "label",
        )
    }

    @Test
    fun rechaza_grammar_point_id_duplicado() {
        val point = GrammarPoint(GrammarPointId("gp-1"), "Hedging")
        waits_for_invalid(
            cursoConLeccion(b2Lesson(grammarPoints = listOf(point, point))),
            "únicos",
        )
    }

    @Test
    fun rechaza_cultural_note_sin_nota() {
        waits_for_invalid(
            cursoConLeccion(
                b2Lesson(culturalNotes = listOf(CulturalNote("play it by ear", "   "))),
            ),
            "culturalNotes",
        )
    }

    @Test
    fun rechaza_audio_only_con_correct_index_fuera_de_rango() {
        val exercise =
            Exercise.AudioOnlyPrompt(
                id = ExerciseId("e1"),
                audioText = "Listen",
                options = listOf("a", "b"),
                correctIndex = 2,
            )
        waits_for_invalid(cursoUnEjercicio(id = "e1", exercise = exercise), "correctIndex")
    }

    @Test
    fun rechaza_cloze_con_respuesta_fuera_de_opciones() {
        val exercise =
            Exercise.ClozeDragDrop(
                id = ExerciseId("e1"),
                passage = "{{b1}}",
                blanks =
                    listOf(
                        Exercise.ClozeBlank(id = "b1", answer = "x", options = listOf("y", "z")),
                    ),
            )
        waits_for_invalid(cursoUnEjercicio(id = "e1", exercise = exercise), "opciones")
    }

    @Test
    fun rechaza_cloze_con_placeholders_desalineados() {
        val exercise =
            Exercise.ClozeDragDrop(
                id = ExerciseId("e1"),
                passage = "{{b1}}",
                blanks =
                    listOf(
                        Exercise.ClozeBlank(id = "b1", answer = "x", options = listOf("x", "y")),
                        Exercise.ClozeBlank(id = "b2", answer = "z", options = listOf("z", "w")),
                    ),
            )
        waits_for_invalid(cursoUnEjercicio(id = "e1", exercise = exercise), "huecos")
    }

    @Test
    fun rechaza_semantic_match_con_lados_duplicados() {
        val exercise =
            Exercise.SemanticMatch(
                id = ExerciseId("e1"),
                pairs =
                    listOf(
                        Exercise.SemanticPair(left = "same", right = "one"),
                        Exercise.SemanticPair(left = "same", right = "two"),
                    ),
            )
        waits_for_invalid(cursoUnEjercicio(id = "e1", exercise = exercise), "únicos")
    }

    @Test
    fun rechaza_free_speaking_con_min_wpm_mayor_que_max() {
        waits_for_invalid(
            cursoUnEjercicio(id = "e1", exercise = freeSpeaking(minWpm = 200.0, maxWpm = 100.0)),
            "minWpm",
        )
    }

    @Test
    fun rechaza_free_speaking_sin_concept_clusters() {
        waits_for_invalid(
            cursoUnEjercicio(id = "e1", exercise = freeSpeaking(conceptClusters = emptyList())),
            "conceptClusters",
        )
    }

    private fun waits_for_invalid(
        course: Course,
        expectedFragment: String,
    ) {
        val validation = CourseValidator.validate(course)
        assertEquals(true, validation is CourseValidation.Invalid)
        assertEquals(
            true,
            (validation as CourseValidation.Invalid).reason.contains(expectedFragment),
        )
    }

    private fun mcCorrecta(id: String = "e1"): Exercise.MultipleChoice =
        Exercise.MultipleChoice(
            id = ExerciseId(id),
            question = "q",
            options = listOf("a", "b"),
            correctIndex = 0,
        )

    private fun mcConFeedback(id: String = "e1"): Exercise = mcCorrecta(id).copy(feedbackOnError = errorFeedback())

    private fun errorFeedback(ruleRef: GrammarPointId? = null): Exercise.FeedbackOnError =
        Exercise.FeedbackOnError(
            type = Exercise.FeedbackErrorType.GRAMMAR,
            explanation = "Explica el error",
            ruleRef = ruleRef,
        )

    private fun freeSpeaking(
        grammarRuleRefs: List<GrammarPointId> = emptyList(),
        conceptClusters: List<List<String>> = listOf(listOf("word")),
        minWpm: Double? = null,
        maxWpm: Double? = null,
        feedbackOnError: Exercise.FeedbackOnError? = errorFeedback(),
    ): Exercise.FreeSpeakingPrompt =
        Exercise.FreeSpeakingPrompt(
            id = ExerciseId("e-free"),
            prompt = "Habla",
            minTokens = 20,
            conceptClusters = conceptClusters,
            modelAnswer = "Modelo",
            selfAssessmentCriteria = listOf("¿Bien?"),
            grammarRuleRefs = grammarRuleRefs,
            minWpm = minWpm,
            maxWpm = maxWpm,
            feedbackOnError = feedbackOnError,
        )

    private fun b2Lesson(
        cefrLevel: CefrLevel = CefrLevel.B2,
        scenario: String? = "Escenario",
        canDo: String? = "Puedo hacerlo",
        grammarPoints: List<GrammarPoint> = listOf(GrammarPoint(GrammarPointId("gp-1"), "Hedging")),
        vocabFrequencyBand: VocabFrequencyBand? = VocabFrequencyBand.BAND_3000_5000,
        culturalNotes: List<CulturalNote> = listOf(CulturalNote("play it by ear", "Nota cultural")),
        exercises: List<Exercise> = listOf(freeSpeaking()),
    ): Lesson =
        Lesson(
            id = LessonId("ls-1"),
            title = "L",
            exercises = exercises,
            cefrLevel = cefrLevel,
            scenario = scenario,
            canDo = canDo,
            grammarPoints = grammarPoints,
            vocabFrequencyBand = vocabFrequencyBand,
            culturalNotes = culturalNotes,
        )

    private fun cursoConLeccion(lesson: Lesson): Course =
        Course(
            id = CourseId("demo"),
            fromLanguage = Language.SPANISH,
            toLanguage = Language.ENGLISH,
            levels = listOf(ruta(lesson)),
        )

    private fun cursoCon(
        ejercicioId: String,
        ejercicio: Exercise,
    ): Course = cursoUnEjercicio(id = ejercicioId, exercise = ejercicio)

    /** Curso mínimo con una sola lección y un solo ejercicio. */
    private fun cursoUnEjercicio(
        id: String,
        exercise: Exercise,
    ): Course =
        Course(
            id = CourseId("demo"),
            fromLanguage = Language.SPANISH,
            toLanguage = Language.ENGLISH,
            levels = listOf(ruta(Lesson(id = LessonId("ls-1"), title = "L", exercises = listOf(exercise)))),
        )

    private fun ruta(lesson: Lesson): Level =
        Level(
            id = LevelId("lv-1"),
            name = "Nivel",
            units =
                listOf(
                    Unit(
                        id = UnitId("un-1"),
                        title = "Unidad",
                        lessons = listOf(lesson),
                    ),
                ),
        )
}

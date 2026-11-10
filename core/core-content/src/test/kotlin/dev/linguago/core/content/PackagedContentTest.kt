package dev.linguago.core.content

import dev.linguago.core.domain.logic.FreeSpeakingHeuristicGrader
import dev.linguago.core.domain.logic.FreeSpeakingReason
import dev.linguago.core.domain.logic.FreeSpeakingVerdict
import dev.linguago.core.domain.logic.SpokenFreeResponse
import dev.linguago.core.domain.logic.VocabularyExtractor
import dev.linguago.core.domain.model.CefrLevel
import dev.linguago.core.domain.model.CourseLocale
import dev.linguago.core.domain.model.Exercise
import dev.linguago.core.domain.model.Language
import dev.linguago.core.domain.model.VocabFrequencyBand
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Golpe de caja del contenido REAL empaquetado en los módulos content: cada
 * curso del repo se decodifica con la MISMA maquinaria de runtime
 * (CourseJson + CourseValidator) y debe pasar limpio. Si este test falla en
 * CI, el contenido se rompió antes de llegar al usuario.
 */
class PackagedContentTest {
    @Test
    fun los_cursos_empaquetados_parsean_y_validan() {
        val expectations =
            listOf(
                "es-en" to "Inglés esencial",
                "es-pt" to "Portugués de arranque",
                "es-fr" to "Francés de cabeza",
                "es-ja" to "Japonés de sillón",
            )
        for ((courseId, expectedTitle) in expectations) {
            val manifest = CourseJson.parseManifest(readAsset(courseId, MANIFEST_FILE))
            assertTrue("El manifest de $courseId no decodifica", manifest != null)
            assertEquals("Título inesperado en $courseId", expectedTitle, manifest?.title)

            val parsed = CourseJson.parseCourse(readAsset(courseId, COURSE_FILE))
            assertTrue("El curso $courseId no decodifica", parsed != null)
            val validation = CourseValidator.validate(parsed!!)
            assertTrue(
                "El curso $courseId falla la validación: $validation",
                validation == CourseValidation.Valid,
            )
        }
    }

    @Test
    fun es_en_v2_conserva_ids_historicos_y_feedback() {
        val course = CourseJson.parseCourse(readAsset("es-en", COURSE_FILE))
        assertTrue("es-en debe seguir decodificando en v2", course != null)
        assertEquals(2, course!!.schemaVersion)
        assertEquals(CourseLocale.ENGLISH_US, course.locale)

        val lessons = course.levels.flatMap { level -> level.units.flatMap { unit -> unit.lessons } }
        val historicLessons = lessons.filter { lesson -> lesson.id.value.startsWith(HISTORIC_LESSON_PREFIX) }
        assertEquals(HISTORIC_LESSON_IDS, historicLessons.map { lesson -> lesson.id.value })

        val historicExercises = historicLessons.flatMap { lesson -> lesson.exercises }
        assertEquals(HISTORIC_EXERCISE_IDS, historicExercises.map { exercise -> exercise.id.value })
        assertTrue(
            "La migración debe enriquecer feedbackOnError en todos los ejercicios",
            lessons
                .flatMap { lesson -> lesson.exercises }
                .all { exercise -> exercise.feedbackOnError != null },
        )
    }

    @Test
    fun es_en_v2_integra_bloques_b1_b2_completos() {
        val course = CourseJson.parseCourse(readAsset("es-en", COURSE_FILE))
        assertTrue("es-en debe decodificar en v2", course != null)

        val lessons = course!!.levels.flatMap { level -> level.units.flatMap { unit -> unit.lessons } }
        val b1Lessons =
            lessons.filter { lesson ->
                lesson.cefrLevel == CefrLevel.B1 && !lesson.id.value.contains("-p0-")
            }
        val b2Lessons = lessons.filter { lesson -> lesson.cefrLevel == CefrLevel.B2 }
        assertEquals(20, b1Lessons.size)
        assertEquals(22, b2Lessons.size)
        assertEquals((1..180).map { index -> "es-en-b1-e$index" }, b1Lessons.flatMap { lesson -> lesson.exercises }.map { it.id.value })
        assertEquals((1..220).map { index -> "es-en-b2-e$index" }, b2Lessons.flatMap { lesson -> lesson.exercises }.map { it.id.value })

        val pilotB1Lessons = lessons.filter { lesson -> lesson.id.value.startsWith("es-en-b1-p0-") }
        assertEquals("El piloto B1 de F0 debe tener 10 lecciones", 10, pilotB1Lessons.size)
        assertTrue(pilotB1Lessons.all { lesson -> lesson.exercises.size == 9 })
        assertTrue(pilotB1Lessons.all { lesson -> lesson.grammarPoints.size == 2 })
        assertTrue(pilotB1Lessons.all { lesson -> lesson.culturalNotes.isNotEmpty() })
        assertTrue(
            "Cada lección B1 del piloto debe tener un FreeSpeakingPrompt",
            pilotB1Lessons.all { lesson ->
                lesson.exercises.count { exercise -> exercise is Exercise.FreeSpeakingPrompt } == 1
            },
        )
        assertTrue(
            "Todos los ejercicios del piloto B1 deben tener feedbackOnError",
            pilotB1Lessons.flatMap { lesson -> lesson.exercises }.all { exercise -> exercise.feedbackOnError != null },
        )

        val a2Lessons =
            lessons.filter { lesson ->
                lesson.id.value.startsWith("es-en-a2-") && !lesson.id.value.contains("-p0-")
            }
        assertEquals(16, a2Lessons.size)
        assertTrue(
            "Las 16 lecciones del bloque A2 deben declarar cefrLevel A2",
            a2Lessons.all { lesson ->
                lesson.cefrLevel == CefrLevel.A2
            },
        )
        val allA2Lessons = lessons.filter { lesson -> lesson.cefrLevel == CefrLevel.A2 }
        assertTrue("El nivel A2 incluye el bloque nuevo y 'Mi rutina'", allA2Lessons.size >= 17)
        assertTrue(
            "'Mi rutina' debe vivir en el nivel A2",
            allA2Lessons.any { lesson -> lesson.id.value == "es-en-lv-1-un-2-ls-3" },
        )
        assertEquals(
            (1..112).map { index -> "es-en-a2-e$index" },
            a2Lessons.flatMap { lesson -> lesson.exercises }.map { it.id.value },
        )
        assertTrue("Cada lección A2 debe tener 7 ejercicios", a2Lessons.all { lesson -> lesson.exercises.size == 7 })

        val pilotA2Lessons = lessons.filter { lesson -> lesson.id.value.startsWith("es-en-a2-p0-") }
        assertEquals("El piloto A2 de F0 debe tener 10 lecciones", 10, pilotA2Lessons.size)
        assertTrue(pilotA2Lessons.all { lesson -> lesson.exercises.size == 7 })
        assertTrue(pilotA2Lessons.all { lesson -> lesson.microLesson != null })
        assertTrue(pilotA2Lessons.all { lesson -> lesson.glossary.isNotEmpty() })
        assertTrue(
            "Todos los ejercicios del piloto A2 deben tener feedbackOnError",
            pilotA2Lessons.flatMap { lesson -> lesson.exercises }.all { exercise -> exercise.feedbackOnError != null },
        )

        val allExercises = lessons.flatMap { lesson -> lesson.exercises }
        assertTrue(
            "El curso es-en debe conservar al menos los 1.069 ejercicios históricos",
            allExercises.size >= 1069,
        )
        assertEquals(allExercises.size, allExercises.map { exercise -> exercise.id.value }.distinct().size)

        val c1Lessons = lessons.filter { lesson -> lesson.cefrLevel == CefrLevel.C1 }
        assertEquals("es-en debe tener 24 lecciones C1", 24, c1Lessons.size)
        assertEquals(
            (1..264).map { index -> "es-en-c1-e$index" },
            c1Lessons.flatMap { lesson -> lesson.exercises }.map { exercise -> exercise.id.value },
        )
        assertTrue("Cada lección C1 debe tener 11 ejercicios", c1Lessons.all { lesson -> lesson.exercises.size == 11 })
        assertTrue(
            "Cada lección C1 debe usar la banda 8000-12000",
            c1Lessons.all { lesson -> lesson.vocabFrequencyBand == VocabFrequencyBand.BAND_8000_12000 },
        )
        assertTrue(
            "Todos los ejercicios C1 deben tener feedbackOnError",
            c1Lessons.flatMap { lesson -> lesson.exercises }.all { exercise -> exercise.feedbackOnError != null },
        )
        assertEquals(
            "Cada lección C1 debe tener un FreeSpeakingPrompt",
            24,
            c1Lessons.sumOf { lesson -> lesson.exercises.count { exercise -> exercise is Exercise.FreeSpeakingPrompt } },
        )

        val c2Lessons = lessons.filter { lesson -> lesson.cefrLevel == CefrLevel.C2 }
        assertEquals("es-en debe tener 18 lecciones C2", 18, c2Lessons.size)
        assertEquals(
            (1..216).map { index -> "es-en-c2-e$index" },
            c2Lessons.flatMap { lesson -> lesson.exercises }.map { exercise -> exercise.id.value },
        )
        assertTrue("Cada lección C2 debe tener 12 ejercicios", c2Lessons.all { lesson -> lesson.exercises.size == 12 })
        assertTrue(
            "Cada lección C2 debe usar la banda 12000+",
            c2Lessons.all { lesson -> lesson.vocabFrequencyBand == VocabFrequencyBand.BAND_12000_PLUS },
        )
        assertTrue(
            "Todos los ejercicios C2 deben tener feedbackOnError",
            c2Lessons.flatMap { lesson -> lesson.exercises }.all { exercise -> exercise.feedbackOnError != null },
        )
        assertEquals(
            "Cada lección C2 debe tener un FreeSpeakingPrompt",
            18,
            c2Lessons.sumOf { lesson -> lesson.exercises.count { exercise -> exercise is Exercise.FreeSpeakingPrompt } },
        )

        for (lesson in b2Lessons) {
            assertEquals("La lección ${lesson.id.value} debe tener 10 ejercicios", 10, lesson.exercises.size)
            assertEquals(VocabFrequencyBand.BAND_3000_5000, lesson.vocabFrequencyBand)
            assertTrue(lesson.exercises.all { exercise -> exercise.feedbackOnError != null })
            assertEquals(1, lesson.exercises.count { exercise -> exercise is Exercise.FreeSpeakingPrompt })
        }
        assertEquals(44, b2Lessons.flatMap { it.exercises }.count { it is Exercise.ClozeDragDrop })
        assertEquals(44, b2Lessons.flatMap { it.exercises }.count { it is Exercise.SemanticMatch })
        assertEquals(44, b2Lessons.flatMap { it.exercises }.count { it is Exercise.AudioOnlyPrompt })
    }

    @Test
    fun cursos_de_arranque_estrenan_b2() {
        for (courseId in listOf("es-fr", "es-pt", "es-ja")) {
            val course = CourseJson.parseCourse(readAsset(courseId, COURSE_FILE))
            assertTrue("$courseId debe decodificar en v2", course != null)
            val lessons = course!!.levels.flatMap { level -> level.units.flatMap { unit -> unit.lessons } }
            val b2Lessons = lessons.filter { lesson -> lesson.cefrLevel == CefrLevel.B2 }
            assertEquals("$courseId debe tener 22 lecciones B2", 22, b2Lessons.size)
            assertEquals(
                (1..3).flatMap { unit -> (1..4).map { lesson -> "$courseId-b2-un-$unit-ls-$lesson" } } +
                    (4..5).flatMap { unit -> (1..5).map { lesson -> "$courseId-b2-un-$unit-ls-$lesson" } },
                b2Lessons.map { lesson -> lesson.id.value },
            )
            assertEquals(
                (1..220).map { index -> "$courseId-b2-e$index" },
                b2Lessons.flatMap { lesson -> lesson.exercises }.map { exercise -> exercise.id.value },
            )
            assertTrue(
                "Cada lección B2 de $courseId debe tener 10 ejercicios",
                b2Lessons.all { lesson -> lesson.exercises.size == 10 },
            )
            assertTrue(
                "Cada lección B2 de $courseId debe tener scenario y canDo",
                b2Lessons.all { lesson -> !lesson.scenario.isNullOrBlank() && !lesson.canDo.isNullOrBlank() },
            )
            assertTrue(
                "Cada lección B2 de $courseId debe tener 2 focos gramaticales",
                b2Lessons.all { lesson -> lesson.grammarPoints.size == 2 },
            )
            assertTrue(
                "Cada lección B2 de $courseId debe tener nota cultural",
                b2Lessons.all { lesson -> lesson.culturalNotes.isNotEmpty() },
            )
            assertTrue(
                "Cada lección B2 de $courseId debe tener microLesson y glosario",
                b2Lessons.all { lesson -> lesson.microLesson != null && lesson.glossary.isNotEmpty() },
            )
            assertTrue(
                "Todos los ejercicios B2 de $courseId deben tener feedbackOnError",
                b2Lessons.flatMap { lesson -> lesson.exercises }.all { exercise -> exercise.feedbackOnError != null },
            )
            assertEquals(
                "Cada lección B2 de $courseId debe tener un FreeSpeakingPrompt",
                22,
                b2Lessons.sumOf { lesson -> lesson.exercises.count { exercise -> exercise is Exercise.FreeSpeakingPrompt } },
            )
        }
    }

    @Test
    fun todos_los_free_speaking_model_answers_no_marcan_fallo_gramatical_o_cobertura() {
        val failures = mutableListOf<String>()
        for (courseId in listOf("es-en", "es-pt", "es-fr", "es-ja")) {
            val course = CourseJson.parseCourse(readAsset(courseId, COURSE_FILE))
            assertTrue("El curso $courseId no decodifica", course != null)
            val parsed = course!!
            val lessons =
                parsed.levels.flatMap { level ->
                    level.units.flatMap { unit -> unit.lessons }
                }
            for (lesson in lessons) {
                for (exercise in lesson.exercises) {
                    if (exercise !is Exercise.FreeSpeakingPrompt) continue
                    val approximateTokens = approximateTokens(exercise.modelAnswer, parsed.toLanguage)
                    val feedback =
                        FreeSpeakingHeuristicGrader.evaluate(
                            exercise = exercise,
                            grammarPoints = lesson.grammarPoints,
                            response =
                                SpokenFreeResponse(
                                    transcript = exercise.modelAnswer,
                                    durationMillis =
                                        (approximateTokens * 60_000L / 100L).coerceIn(
                                            MODEL_ANSWER_MIN_DURATION_MILLIS,
                                            MODEL_ANSWER_MAX_DURATION_MILLIS,
                                        ),
                                ),
                            language = parsed.toLanguage,
                        )
                    val reasons =
                        (feedback.verdict as? FreeSpeakingVerdict.NeedsWork)
                            ?.reasons
                            ?.map { reason -> reason.toString() }
                            .orEmpty()
                    val contentReasons =
                        reasons.filter {
                            it == FreeSpeakingReason.MISSING_GRAMMAR.toString() ||
                                it == FreeSpeakingReason.LOW_COVERAGE.toString()
                        }
                    if (contentReasons.isNotEmpty()) {
                        val refs =
                            exercise.grammarRuleRefs
                                .map { ref ->
                                    lesson.grammarPoints.firstOrNull { point -> point.id == ref }?.label ?: ref.value
                                }.joinToString(", ")
                        failures +=
                            "${exercise.id.value} ($courseId/${lesson.id.value}) " +
                            "tokens=${feedback.tokenCount}, clusters=${feedback.coveredClusters}/${feedback.totalClusters}, " +
                            "grammar=${feedback.detectedGrammarRefs.map {
                                it.value
                            }}, refs=[$refs], reasons=${contentReasons.joinToString(
                                ",",
                            )}"
                    }
                }
            }
        }
        assertTrue(
            "Model answers con fallo gramatical o de cobertura:\n${failures.joinToString("\n")}",
            failures.isEmpty(),
        )
    }

    @Test
    fun cursos_de_arranque_amplian_b1() {
        for (courseId in listOf("es-fr", "es-pt", "es-ja")) {
            val course = CourseJson.parseCourse(readAsset(courseId, COURSE_FILE))
            assertTrue("El curso $courseId no decodifica", course != null)
            val lessons = course!!.levels.flatMap { level -> level.units.flatMap { unit -> unit.lessons } }
            val b1Lessons = lessons.filter { lesson -> lesson.cefrLevel == CefrLevel.B1 }
            assertEquals("$courseId debe tener 20 lecciones B1", 20, b1Lessons.size)
            assertEquals(
                (1..5).flatMap { unit -> (1..4).map { lesson -> "$courseId-b1-un-$unit-ls-$lesson" } },
                b1Lessons.map { lesson -> lesson.id.value },
            )
            assertEquals(
                (1..180).map { index -> "$courseId-b1-e$index" },
                b1Lessons.flatMap { lesson -> lesson.exercises }.map { exercise -> exercise.id.value },
            )
            assertTrue(
                "Cada lección B1 de $courseId debe tener 9 ejercicios",
                b1Lessons.all { lesson -> lesson.exercises.size == 9 },
            )
            assertTrue(
                "Cada lección B1 de $courseId debe tener scenario y canDo",
                b1Lessons.all { lesson -> !lesson.scenario.isNullOrBlank() && !lesson.canDo.isNullOrBlank() },
            )
            assertTrue(
                "Cada lección B1 de $courseId debe tener 2 focos gramaticales",
                b1Lessons.all { lesson -> lesson.grammarPoints.size == 2 },
            )
            assertTrue(
                "Cada lección B1 de $courseId debe tener nota cultural",
                b1Lessons.all { lesson -> lesson.culturalNotes.isNotEmpty() },
            )
            assertTrue(
                "Cada lección B1 de $courseId debe tener microLesson y glosario",
                b1Lessons.all { lesson -> lesson.microLesson != null && lesson.glossary.isNotEmpty() },
            )
            assertTrue(
                "Todos los ejercicios B1 de $courseId deben tener feedbackOnError",
                b1Lessons.flatMap { lesson -> lesson.exercises }.all { exercise -> exercise.feedbackOnError != null },
            )
            assertEquals(
                "Cada lección B1 de $courseId debe tener un FreeSpeakingPrompt",
                20,
                b1Lessons.sumOf { lesson -> lesson.exercises.count { exercise -> exercise is Exercise.FreeSpeakingPrompt } },
            )
        }
    }

    @Test
    fun es_en_a1_estrena_unidad_de_vida_cotidiana() {
        val course = CourseJson.parseCourse(readAsset("es-en", COURSE_FILE))
        assertTrue("es-en debe decodificar en v2", course != null)

        val lessons = course!!.levels.flatMap { level -> level.units.flatMap { unit -> unit.lessons } }
        val a1Lessons = lessons.filter { lesson -> lesson.cefrLevel == CefrLevel.A1 }
        assertTrue("A1 debe conservar al menos 10 lecciones", a1Lessons.size >= 10)
        assertTrue("Cada lección A1 debe tener 7 ejercicios", a1Lessons.all { lesson -> lesson.exercises.size == 7 })
        assertTrue("Cada lección A1 debe tener microLesson", a1Lessons.all { lesson -> lesson.microLesson != null })
        assertTrue("Cada lección A1 debe tener canDo", a1Lessons.all { lesson -> !lesson.canDo.isNullOrBlank() })

        val newA1Lessons =
            lessons.filter { lesson ->
                lesson.id.value.startsWith("es-en-a1-") && !lesson.id.value.contains("-p0-")
            }
        assertEquals(5, newA1Lessons.size)
        assertEquals(
            (1..35).map { index -> "es-en-a1-e$index" },
            newA1Lessons.flatMap { lesson -> lesson.exercises }.map { exercise -> exercise.id.value },
        )
        assertTrue(newA1Lessons.all { lesson -> lesson.microLesson != null })
        assertTrue(newA1Lessons.all { lesson -> lesson.glossary.isNotEmpty() })
        assertTrue(
            "Todos los ejercicios A1 nuevos deben tener feedbackOnError",
            newA1Lessons.flatMap { lesson -> lesson.exercises }.all { exercise -> exercise.feedbackOnError != null },
        )

        val pilotLessons = lessons.filter { lesson -> lesson.id.value.startsWith("es-en-a1-p0-") }
        assertEquals("El piloto A1 de F0 debe tener 10 lecciones", 10, pilotLessons.size)
        assertTrue(pilotLessons.all { lesson -> lesson.exercises.size == 7 })
        assertTrue(pilotLessons.all { lesson -> lesson.microLesson != null })
        assertTrue(pilotLessons.all { lesson -> lesson.glossary.isNotEmpty() })
        assertTrue(
            "Todos los ejercicios del piloto A1 deben tener feedbackOnError",
            pilotLessons.flatMap { lesson -> lesson.exercises }.all { exercise -> exercise.feedbackOnError != null },
        )
    }

    @Test
    fun es_en_v2_conserva_idioms_b2_sin_duplicados() {
        val course = CourseJson.parseCourse(readAsset("es-en", COURSE_FILE))
        assertTrue("es-en debe decodificar en v2", course != null)

        val lessons = course!!.levels.flatMap { level -> level.units.flatMap { unit -> unit.lessons } }
        val b2Lessons = lessons.filter { lesson -> lesson.cefrLevel == CefrLevel.B2 }
        assertTrue(
            "Cada lección B2 debe aportar vocabulario revisable (al menos su modismo)",
            b2Lessons.all { lesson -> VocabularyExtractor.fromLesson(lesson).isNotEmpty() },
        )

        // El vocabulario puede repetirse entre lecciones: FSRS lo comparte por
        // (courseId, word), y el extractor ya deduplica dentro de cada lección.
        // Lo que no debe repetirse es el modismo cultural dentro del bloque B2.
        val idioms = b2Lessons.flatMap { lesson -> lesson.culturalNotes }.map { note -> note.idiom.lowercase() }
        assertEquals(idioms.size, idioms.distinct().size)
    }

    @Test
    fun cursos_de_arranque_migran_a_v2_y_amplian_a1() {
        val expectations =
            mapOf(
                "es-pt" to expandedStarterExpectation("es-pt", CourseLocale.PORTUGUESE_BR),
                "es-fr" to expandedStarterExpectation("es-fr", CourseLocale.FRENCH_FR),
                "es-ja" to expandedStarterExpectation("es-ja", CourseLocale.JAPANESE_JP),
            )
        for ((courseId, expected) in expectations) {
            val course = CourseJson.parseCourse(readAsset(courseId, COURSE_FILE))
            assertTrue("El curso $courseId no decodifica", course != null)
            assertEquals("$courseId debe estar en v2", 2, course!!.schemaVersion)
            assertEquals("Locale inesperado en $courseId", expected.locale, course.locale)

            val lessons = course.levels.flatMap { level -> level.units.flatMap { unit -> unit.lessons } }
            val a1Lessons = lessons.filter { lesson -> lesson.cefrLevel == CefrLevel.A1 }
            assertEquals("Número de lecciones A1 inesperado en $courseId", expected.lessonIds.size, a1Lessons.size)
            assertEquals(expected.lessonIds, a1Lessons.map { lesson -> lesson.id.value })
            assertEquals(
                "Ids de ejercicio A1 inesperados en $courseId",
                expected.exerciseIds,
                a1Lessons.flatMap { lesson -> lesson.exercises }.map { exercise -> exercise.id.value },
            )
            assertTrue(
                "Cada lección A1 de $courseId debe tener 7 ejercicios",
                a1Lessons.all { lesson -> lesson.exercises.size == 7 },
            )
            assertTrue(
                "Cada lección A1 de $courseId debe tener microLesson",
                a1Lessons.all { lesson -> lesson.microLesson != null },
            )
            assertTrue(
                "Cada lección A1 de $courseId debe tener glosario curado",
                a1Lessons.all { lesson -> lesson.glossary.isNotEmpty() },
            )
            assertTrue(
                "Todos los ejercicios A1 de $courseId deben tener feedbackOnError",
                a1Lessons.flatMap { lesson -> lesson.exercises }.all { exercise -> exercise.feedbackOnError != null },
            )
        }
    }

    @Test
    fun cursos_de_arranque_amplian_a2() {
        for (courseId in listOf("es-fr", "es-pt", "es-ja")) {
            val course = CourseJson.parseCourse(readAsset(courseId, COURSE_FILE))
            assertTrue("El curso $courseId no decodifica", course != null)
            val lessons = course!!.levels.flatMap { level -> level.units.flatMap { unit -> unit.lessons } }
            val a2Lessons = lessons.filter { lesson -> lesson.cefrLevel == CefrLevel.A2 }
            assertEquals("$courseId debe tener 8 lecciones A2", 8, a2Lessons.size)
            assertEquals(
                (1..2).flatMap { unit -> (1..4).map { lesson -> "$courseId-a2-un-$unit-ls-$lesson" } },
                a2Lessons.map { lesson -> lesson.id.value },
            )
            assertEquals(
                (1..56).map { index -> "$courseId-a2-e$index" },
                a2Lessons.flatMap { lesson -> lesson.exercises }.map { exercise -> exercise.id.value },
            )
            assertTrue(
                "Cada lección A2 de $courseId debe tener 7 ejercicios",
                a2Lessons.all { lesson -> lesson.exercises.size == 7 },
            )
            assertTrue(
                "Cada lección A2 de $courseId debe tener microLesson",
                a2Lessons.all { lesson -> lesson.microLesson != null },
            )
            assertTrue(
                "Cada lección A2 de $courseId debe tener glosario curado",
                a2Lessons.all { lesson -> lesson.glossary.isNotEmpty() },
            )
            assertTrue(
                "Todos los ejercicios A2 de $courseId deben tener feedbackOnError",
                a2Lessons.flatMap { lesson -> lesson.exercises }.all { exercise -> exercise.feedbackOnError != null },
            )
        }
    }

    @Test
    fun los_cursos_de_arranque_estrenan_c1() {
        for (courseId in listOf("es-fr", "es-pt", "es-ja")) {
            val course = CourseJson.parseCourse(readAsset(courseId, COURSE_FILE))
            assertTrue("$courseId debe decodificar en v2", course != null)
            val lessons = course!!.levels.flatMap { level -> level.units.flatMap { unit -> unit.lessons } }
            val c1Lessons = lessons.filter { lesson -> lesson.cefrLevel == CefrLevel.C1 }
            assertEquals("$courseId debe tener 24 lecciones C1", 24, c1Lessons.size)
            assertEquals(
                (1..6).flatMap { unit -> (1..4).map { lesson -> "$courseId-c1-un-$unit-ls-$lesson" } },
                c1Lessons.map { lesson -> lesson.id.value },
            )
            assertEquals(
                (1..264).map { index -> "$courseId-c1-e$index" },
                c1Lessons.flatMap { lesson -> lesson.exercises }.map { exercise -> exercise.id.value },
            )
            assertTrue(
                "Cada lección C1 de $courseId debe tener 11 ejercicios",
                c1Lessons.all { lesson -> lesson.exercises.size == 11 },
            )
            assertTrue(
                "Cada lección C1 de $courseId debe usar la banda 8000-12000",
                c1Lessons.all { lesson -> lesson.vocabFrequencyBand == VocabFrequencyBand.BAND_8000_12000 },
            )
            assertTrue(
                "Cada lección C1 de $courseId debe tener microLesson y glosario",
                c1Lessons.all { lesson -> lesson.microLesson != null && lesson.glossary.isNotEmpty() },
            )
            assertTrue(
                "Todos los ejercicios C1 de $courseId deben tener feedbackOnError",
                c1Lessons.flatMap { lesson -> lesson.exercises }.all { exercise -> exercise.feedbackOnError != null },
            )
            assertEquals(
                "Cada lección C1 de $courseId debe tener un FreeSpeakingPrompt",
                24,
                c1Lessons.sumOf { lesson -> lesson.exercises.count { exercise -> exercise is Exercise.FreeSpeakingPrompt } },
            )
        }
    }

    @Test
    fun los_cursos_de_arranque_estrenan_c2() {
        for (courseId in listOf("es-fr", "es-pt", "es-ja")) {
            val course = CourseJson.parseCourse(readAsset(courseId, COURSE_FILE))
            assertTrue("$courseId debe decodificar en v2", course != null)
            val lessons = course!!.levels.flatMap { level -> level.units.flatMap { unit -> unit.lessons } }
            val c2Lessons = lessons.filter { lesson -> lesson.cefrLevel == CefrLevel.C2 }
            assertEquals("$courseId debe tener 18 lecciones C2", 18, c2Lessons.size)
            assertEquals(
                (1..3).flatMap { unit -> (1..4).map { lesson -> "$courseId-c2-un-$unit-ls-$lesson" } } +
                    (1..6).map { lesson -> "$courseId-c2-un-4-ls-$lesson" },
                c2Lessons.map { lesson -> lesson.id.value },
            )
            assertEquals(
                (1..216).map { index -> "$courseId-c2-e$index" },
                c2Lessons.flatMap { lesson -> lesson.exercises }.map { exercise -> exercise.id.value },
            )
            assertTrue(
                "Cada lección C2 de $courseId debe tener 12 ejercicios",
                c2Lessons.all { lesson -> lesson.exercises.size == 12 },
            )
            assertTrue(
                "Cada lección C2 de $courseId debe usar la banda 12000+",
                c2Lessons.all { lesson -> lesson.vocabFrequencyBand == VocabFrequencyBand.BAND_12000_PLUS },
            )
            assertTrue(
                "Cada lección C2 de $courseId debe tener microLesson y glosario",
                c2Lessons.all { lesson -> lesson.microLesson != null && lesson.glossary.isNotEmpty() },
            )
            assertTrue(
                "Todos los ejercicios C2 de $courseId deben tener feedbackOnError",
                c2Lessons.flatMap { lesson -> lesson.exercises }.all { exercise -> exercise.feedbackOnError != null },
            )
            assertEquals(
                "Cada lección C2 de $courseId debe tener un FreeSpeakingPrompt",
                18,
                c2Lessons.sumOf { lesson -> lesson.exercises.count { exercise -> exercise is Exercise.FreeSpeakingPrompt } },
            )
        }
    }

    private fun expandedStarterExpectation(
        courseId: String,
        locale: CourseLocale,
    ): StarterExpectation =
        StarterExpectation(
            locale = locale,
            lessonIds =
                historicLessonIds(courseId) +
                    (2..3).flatMap { unit -> (1..4).map { lesson -> "$courseId-a1-un-$unit-ls-$lesson" } },
            exerciseIds =
                historicExerciseIds(courseId) +
                    (1..56).map { index -> "$courseId-a1-e$index" },
        )

    private fun historicLessonIds(courseId: String): List<String> = (1..2).map { index -> "$courseId-lv-1-un-1-ls-$index" }

    private fun historicExerciseIds(courseId: String): List<String> =
        (1..6).map { index -> "$courseId-e$index" } +
            "$courseId-e13" +
            (7..12).map { index -> "$courseId-e$index" } +
            "$courseId-e14"

    private data class StarterExpectation(
        val locale: CourseLocale,
        val lessonIds: List<String>,
        val exerciseIds: List<String>,
    )

    /** Layout del repo relativo al directorio de trabajo del módulo. */
    private fun approximateTokens(
        text: String,
        language: Language,
    ): Long {
        val tokens =
            if (language == Language.JAPANESE) {
                text.filter { char -> char.isLetterOrDigit() }.length
            } else {
                text
                    .trim()
                    .split(Regex("\\s+"))
                    .filter { token -> token.isNotBlank() }
                    .size
            }
        return tokens.toLong().coerceAtLeast(1L)
    }

    private fun readAsset(
        courseId: String,
        fileName: String,
    ): String {
        val path = File(contentRoot(), "content-$courseId/src/main/assets/courses/$courseId/$fileName")
        assertTrue("Falta el asset del contenido: ${path.path}", path.exists())
        return path.readText()
    }

    private fun contentRoot(): File {
        System.getProperty("linguago.content.dir")?.let { explicit -> return File(explicit) }
        val userDir = System.getProperty("user.dir") ?: error("No hay user.dir en ambiente de test")
        var dir = File(userDir)
        var depth = 0
        while (depth < SEARCH_DEPTH_UP) {
            val candidate = File(dir, "content")
            if (candidate.isDirectory) return candidate
            dir = dir.parentFile ?: break
            depth++
        }
        error("No se ubicó el directorio content desde $userDir")
    }

    private companion object {
        private const val MANIFEST_FILE = "manifest.json"
        private const val COURSE_FILE = "course.json"
        private const val MODEL_ANSWER_MIN_DURATION_MILLIS = 10_000L
        private const val MODEL_ANSWER_MAX_DURATION_MILLIS = 90_000L
        private const val SEARCH_DEPTH_UP = 4
        private const val HISTORIC_LESSON_PREFIX = "es-en-lv-1-"

        private val HISTORIC_LESSON_IDS =
            listOf(
                "es-en-lv-1-un-1-ls-1",
                "es-en-lv-1-un-1-ls-2",
                "es-en-lv-1-un-1-ls-3",
                "es-en-lv-1-un-2-ls-1",
                "es-en-lv-1-un-2-ls-2",
                "es-en-lv-1-un-2-ls-3",
            )

        private val HISTORIC_EXERCISE_IDS =
            listOf(
                "es-en-e1",
                "es-en-e2",
                "es-en-e3",
                "es-en-e4",
                "es-en-e5",
                "es-en-e6",
                "es-en-e37",
                "es-en-e7",
                "es-en-e8",
                "es-en-e9",
                "es-en-e10",
                "es-en-e11",
                "es-en-e12",
                "es-en-e38",
                "es-en-e13",
                "es-en-e14",
                "es-en-e15",
                "es-en-e16",
                "es-en-e17",
                "es-en-e18",
                "es-en-e39",
                "es-en-e19",
                "es-en-e20",
                "es-en-e21",
                "es-en-e22",
                "es-en-e23",
                "es-en-e24",
                "es-en-e40",
                "es-en-e25",
                "es-en-e26",
                "es-en-e27",
                "es-en-e28",
                "es-en-e29",
                "es-en-e30",
                "es-en-e41",
                "es-en-e31",
                "es-en-e32",
                "es-en-e33",
                "es-en-e34",
                "es-en-e35",
                "es-en-e36",
                "es-en-e42",
            )
    }
}

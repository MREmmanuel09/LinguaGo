package dev.linguago.core.content

import dev.linguago.core.domain.model.CourseLocale
import dev.linguago.core.domain.model.Exercise
import dev.linguago.core.domain.model.ExerciseId
import dev.linguago.core.domain.model.Language
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Fija el formato JSON del contenido (ADR-009): el discriminador "type" usa
 * el nombre simple de cada tipo de ejercicio, Language serializa por código
 * ISO y MatchPairs serializa cada par como arreglo [izquierda, derecha].
 */
class CourseJsonTest {
    @Test
    fun parse_manifest_ligero() {
        val manifest = CourseJson.parseManifest(MANIFEST_VALIDO)

        assertNotNull(manifest)
        assertEquals("demo", manifest?.id?.value)
        assertEquals(Language.SPANISH, manifest?.fromLanguage)
        assertEquals(Language.ENGLISH, manifest?.toLanguage)
        assertEquals("Demo", manifest?.title)
    }

    @Test
    fun parse_curso_completo_con_todos_los_tipos_base() {
        val course = CourseJson.parseCourse(CURSO_VALIDO)

        assertNotNull(course)
        assertEquals(
            6,
            course
                ?.levels
                ?.first()
                ?.units
                ?.first()
                ?.lessons
                ?.first()
                ?.exercises
                ?.size,
        )
    }

    @Test
    fun match_pairs_decodifica_arreglos_de_par() {
        val course = CourseJson.parseCourse(CURSO_VALIDO)
        val exercise =
            course
                ?.levels
                ?.first()
                ?.units
                ?.first()
                ?.lessons
                ?.first()
                ?.exercises
                ?.firstOrNull { it.id.value == "e4" }

        assertEquals(
            Exercise.MatchPairs(
                id = ExerciseId("e4"),
                pairs =
                    listOf(
                        Exercise.MatchPair(left = "uno", right = "one"),
                        Exercise.MatchPair(left = "dos", right = "two"),
                    ),
            ),
            exercise,
        )
    }

    @Test
    fun resuelve_variantes_dialectales_v2() {
        assertEquals(CourseLocale.ENGLISH_US, CourseLocale.fromBcp47("en-US"))
        assertEquals(CourseLocale.ENGLISH_GB, CourseLocale.fromBcp47("en-GB"))
        assertEquals(CourseLocale.PORTUGUESE_BR, CourseLocale.fromBcp47("pt-BR"))
        assertEquals(CourseLocale.PORTUGUESE_PT, CourseLocale.fromBcp47("pt-PT"))
        assertNull(CourseLocale.fromBcp47("en-AU"))

        val british = CourseJson.parseCourse(CURSO_V2_EN_GB)
        assertNotNull(british)
        assertEquals(CourseLocale.ENGLISH_GB, british!!.locale)
        assertEquals(2, british.schemaVersion)
    }

    @Test
    fun json_malformado_devuelve_null_sin_excepciones() {
        assertNull(CourseJson.parseCourse(CURSO_JSON_NO_JSON))
        assertNull(CourseJson.parseManifest(MANIFEST_SIN_TITULO))
        assertNull(CourseJson.parseManifest(MANIFEST_IDIOMA_INVALIDO))
        assertNull(CourseJson.parseCourse(CURSO_SUBTIPO_INVALIDO))
    }

    private companion object {
        private const val MANIFEST_VALIDO =
            """{"id":"demo","from":"es","to":"en","title":"Demo"}"""

        private val CURSO_VALIDO =
            """
            {
              "id": "demo",
              "from": "es",
              "to": "en",
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
                          "title": "Lección",
                          "exercises": [
                            {"type": "MultipleChoice", "id": "e1", "question": "q",
                             "options": ["a", "b"], "correctIndex": 0},
                            {"type": "Translate", "id": "e2", "sourcePhrase": "s",
                             "correctTokens": ["x"], "distractorTokens": ["y"]},
                            {"type": "OrderWords", "id": "e3", "tokens": ["a", "b"],
                             "answerTokens": ["b", "a"]},
                            {"type": "MatchPairs", "id": "e4",
                             "pairs": [{"left": "uno", "right": "one"},
                                       {"left": "dos", "right": "two"}]},
                            {"type": "ListenType", "id": "e5", "transcription": "t",
                             "audioText": "t"},
                            {"type": "KanaChoose", "id": "e6", "kana": "あ",
                             "romajiOptions": ["a"], "correctRomaji": "a"}
                          ]
                        }
                      ]
                    }
                  ]
                }
              ]
            }
            """.trimIndent()

        private val CURSO_V2_EN_GB =
            """
            {
              "id": "es-en",
              "from": "es",
              "to": "en",
              "schemaVersion": 2,
              "locale": "en-GB",
              "levels": [
                {
                  "id": "lv-1",
                  "name": "Nivel",
                  "units": [
                    {
                      "id": "un-1",
                      "title": "Unidad",
                      "lessons": [
                        {
                          "id": "ls-1",
                          "title": "Lección",
                          "exercises": [
                            {"type": "MultipleChoice", "id": "e1", "question": "q",
                             "options": ["a", "b"], "correctIndex": 0}
                          ]
                        }
                      ]
                    }
                  ]
                }
              ]
            }
            """.trimIndent()

        private const val CURSO_JSON_NO_JSON = "{ no es un json válido " // runCatching → null

        private const val MANIFEST_SIN_TITULO =
            """{"id":"demo","from":"es","to":"en"}"""

        private const val MANIFEST_IDIOMA_INVALIDO =
            """{"id":"demo","from":"xx","to":"en","title":"Demo"}"""

        private val CURSO_SUBTIPO_INVALIDO =
            """
            {
              "id": "demo",
              "from": "es",
              "to": "en",
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
                          "title": "Lección",
                          "exercises": [
                            {"type": "ExercicioInexistente123", "id": "e1"}
                          ]
                        }
                      ]
                    }
                  ]
                }
              ]
            }
            """.trimIndent()
    }
}

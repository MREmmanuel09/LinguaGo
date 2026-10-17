package dev.linguago.core.content

import dev.linguago.core.domain.model.Course
import dev.linguago.core.domain.model.CourseId
import dev.linguago.core.domain.model.Exercise
import dev.linguago.core.domain.model.ExerciseId
import dev.linguago.core.domain.model.Language
import dev.linguago.core.domain.model.Lesson
import dev.linguago.core.domain.model.LessonId
import dev.linguago.core.domain.model.Level
import dev.linguago.core.domain.model.LevelId
import dev.linguago.core.domain.model.Unit
import dev.linguago.core.domain.model.UnitId
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

    private fun mcCorrecta(id: String = "e1"): Exercise =
        Exercise.MultipleChoice(
            id = ExerciseId(id),
            question = "q",
            options = listOf("a", "b"),
            correctIndex = 0,
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

package dev.linguago.core.domain.placement

import dev.linguago.core.domain.model.CefrLevel
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
import dev.linguago.core.domain.usecase.ExerciseAnswer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

/** Evaluación por bloques independientes y cierre anticipado (ADR-058). */
public class PlacementTest {
    @Test
    public fun `un nivel superior desbloquea aunque A1 no se apruebe`() {
        val exam =
            PlacementExam(
                courseId = "es-en",
                blocks =
                    listOf(
                        block(CefrLevel.A1, 2),
                        block(CefrLevel.B1, 2),
                    ),
            )
        val answers = mutableMapOf<Pair<Int, Int>, ExerciseAnswer>()
        answers[1 to 0] = ExerciseAnswer.OptionPicked(0)
        answers[1 to 1] = ExerciseAnswer.OptionPicked(0)

        val result = PlacementEvaluator.evaluate(exam, answers, course())

        assertFalse(result.levelResults.first { it.level == CefrLevel.A1 }.passed)
        assertTrue(result.levelResults.first { it.level == CefrLevel.B1 }.passed)
        assertEquals(CefrLevel.B1, result.achievedLevel)
        assertEquals(listOf("a1", "b1"), result.unlockedLessonIds.map { it.value })
    }

    @Test
    public fun `cerrar con el 70 por ciento del bloque desbloquea ese nivel`() {
        val exam = PlacementExam("es-en", listOf(block(CefrLevel.A1, 10)))
        val answers = (0 until 7).associate { (0 to it) to ExerciseAnswer.OptionPicked(0) }

        val result = PlacementEvaluator.evaluate(exam, answers, course())

        assertTrue(result.levelResults.single().passed)
        assertEquals(CefrLevel.A1, result.achievedLevel)
    }

    @Test
    public fun `responder poco aunque sea correcto no desbloquea`() {
        val exam = PlacementExam("es-en", listOf(block(CefrLevel.A1, 10)))
        val answers = (0 until 5).associate { (0 to it) to ExerciseAnswer.OptionPicked(0) }

        val result = PlacementEvaluator.evaluate(exam, answers, course())

        assertFalse(result.levelResults.single().passed)
        assertEquals(null, result.achievedLevel)
        assertTrue(result.unlockedLessonIds.isEmpty())
    }

    @Test
    public fun `el generador completa los ejercicios de opcion multiple a cinco opciones`() {
        val exercises =
            (0 until 3).map { index ->
                Exercise.MultipleChoice(
                    id = ExerciseId("q$index"),
                    question = "Question $index",
                    options = listOf("a$index", "b$index", "c$index"),
                    correctIndex = 0,
                )
            }
        val course =
            course(
                levels =
                    listOf(
                        CefrLevel.A1 to listOf(exercises),
                    ),
            )

        val exam = PlacementExamGenerator.generate(course, questionsPerLevel = 10, random = Random(7))

        val questions = exam.blocks.single().questions
        assertEquals(3, questions.size)
        questions.forEach { question ->
            val exercise = question.exercise as Exercise.MultipleChoice
            assertEquals(5, exercise.options.size)
            assertEquals(5, exercise.options.distinct().size)
            assertEquals(0, exercise.correctIndex)
        }
    }

    @Test
    public fun `el generador incluye escucha y dictado cuando hay contenido`() {
        val exercises =
            listOf(
                Exercise.MultipleChoice(
                    id = ExerciseId("q-mc"),
                    question = "Question",
                    options = listOf("a", "b", "c"),
                    correctIndex = 0,
                ),
                Exercise.ListenChoose(
                    id = ExerciseId("q-lc"),
                    options = listOf("one", "two", "three"),
                    correctIndex = 0,
                    audioText = "one",
                ),
                Exercise.ListenType(
                    id = ExerciseId("q-lt"),
                    transcription = "hello",
                    audioText = "hello",
                ),
            )
        val course = course(levels = listOf(CefrLevel.A1 to listOf(exercises)))

        val exam = PlacementExamGenerator.generate(course, questionsPerLevel = 3, random = Random(3))

        val types =
            exam.blocks
                .single()
                .questions
                .map { it.exercise::class.simpleName }
                .toSet()
        assertEquals(setOf("MultipleChoice", "ListenChoose", "ListenType"), types)
    }

    private fun block(
        level: CefrLevel,
        questions: Int,
    ): PlacementLevelBlock =
        PlacementLevelBlock(
            level = level,
            questions =
                (0 until questions).map { index ->
                    PlacementQuestion(
                        lessonId = LessonId(level.name.lowercase()),
                        cefrLevel = level,
                        exercise =
                            Exercise.MultipleChoice(
                                id = ExerciseId("${level.name}-$index"),
                                question = "Question $index",
                                options = listOf("correct", "wrong-a", "wrong-b"),
                                correctIndex = 0,
                            ),
                    )
                },
        )

    private fun course(
        levels: List<Pair<CefrLevel, List<List<Exercise>>>> =
            listOf(
                CefrLevel.A1 to listOf(emptyList()),
                CefrLevel.B1 to listOf(emptyList()),
            ),
    ): Course =
        Course(
            id = CourseId("es-en"),
            fromLanguage = Language.SPANISH,
            toLanguage = Language.ENGLISH,
            levels =
                levels.mapIndexed { index, (cefr, lessons) ->
                    Level(
                        id = LevelId("level-$index"),
                        name = cefr.name,
                        units =
                            listOf(
                                Unit(
                                    id = UnitId("unit-$index"),
                                    title = "Unit",
                                    lessons =
                                        lessons.mapIndexed { lessonIndex, exercises ->
                                            Lesson(
                                                id = LessonId(cefr.name.lowercase() + if (lessonIndex == 0) "" else "-$lessonIndex"),
                                                title = "Lesson",
                                                exercises = exercises,
                                                cefrLevel = cefr,
                                            )
                                        },
                                ),
                            ),
                    )
                },
        )
}

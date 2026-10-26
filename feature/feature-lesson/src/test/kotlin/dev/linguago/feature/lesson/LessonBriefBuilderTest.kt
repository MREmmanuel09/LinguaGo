package dev.linguago.feature.lesson

import dev.linguago.core.domain.model.CulturalNote
import dev.linguago.core.domain.model.Exercise
import dev.linguago.core.domain.model.ExerciseId
import dev.linguago.core.domain.model.GrammarPoint
import dev.linguago.core.domain.model.GrammarPointId
import dev.linguago.core.domain.model.Lesson
import dev.linguago.core.domain.model.LessonId
import dev.linguago.core.domain.model.MicroLesson
import dev.linguago.core.domain.model.MicroLessonExample
import dev.linguago.core.domain.model.MicroLessonSection
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LessonBriefBuilderTest {
    @Test
    fun `usa la micro-clase autorizada`() {
        val lesson =
            lesson(
                microLesson =
                    MicroLesson(
                        title = "Condicionales",
                        goals = listOf("Reconocer la condición.", "Elegir la consecuencia."),
                        sections =
                            listOf(
                                MicroLessonSection(
                                    heading = "Regla",
                                    body = "If + pasado, would + infinitivo.",
                                    examples =
                                        listOf(
                                            MicroLessonExample("Si tuviera tiempo", "If I had time", "Condición irreal."),
                                        ),
                                ),
                            ),
                    ),
            )

        val brief = LessonBriefBuilder.build(lesson)

        assertEquals("Condicionales", brief.title)
        assertEquals(2, brief.goals.size)
        assertEquals("Regla", brief.sections.single().heading)
        assertEquals(
            "If I had time",
            brief.sections
                .single()
                .examples
                .single()
                .target,
        )
        assertEquals("Elegir la opción", brief.mechanics.single().title)
    }

    @Test
    fun `deriva la micro-clase de los metadatos de la leccion`() {
        val lesson =
            lesson(
                scenario = "Pedir una prórroga en una reunión.",
                canDo = "Puedo justificar una concesión.",
                grammarPoints =
                    listOf(
                        GrammarPoint(
                            id = GrammarPointId("gp-hedging"),
                            label = "Hedging",
                            description = "Suaviza una negativa con 'I'm afraid'.",
                        ),
                    ),
                culturalNotes = listOf(CulturalNote("play it by ear", "Decidir sobre la marcha.")),
                exercises = listOf(matchPairs()),
            )

        val brief = LessonBriefBuilder.build(lesson)

        assertEquals(lesson.title, brief.title)
        assertEquals(listOf("Puedo justificar una concesión."), brief.goals)
        assertEquals(
            listOf("Situación", "Regla: Hedging", "Cultura"),
            brief.sections.map { section -> section.heading },
        )
        assertTrue(brief.sections[1].body.contains("I'm afraid"))
        assertTrue(brief.sections[2].body.contains("play it by ear"))
        assertEquals("Emparejar", brief.mechanics.single().title)
    }

    @Test
    fun `agrupa mecanicas repetidas y conserva el orden de aparicion`() {
        val lesson =
            lesson(
                exercises =
                    listOf(
                        multipleChoice("e1"),
                        orderWords("e2"),
                        multipleChoice("e3"),
                    ),
            )

        val brief = LessonBriefBuilder.build(lesson)

        assertEquals(listOf("Elegir la opción", "Ordenar la frase"), brief.mechanics.map { it.title })
    }

    @Test
    fun `sin metadatos sigue ofreciendo una micro-clase util`() {
        val brief = LessonBriefBuilder.build(lesson())

        assertEquals(listOf("Practicar «Lección de prueba» con ejercicios cortos."), brief.goals)
        assertEquals("En esta lección", brief.sections.single().heading)
        assertEquals("Elegir la opción", brief.mechanics.single().title)
    }

    private fun lesson(
        microLesson: MicroLesson? = null,
        scenario: String? = null,
        canDo: String? = null,
        grammarPoints: List<GrammarPoint> = emptyList(),
        culturalNotes: List<CulturalNote> = emptyList(),
        exercises: List<Exercise> = listOf(multipleChoice("e1")),
    ): Lesson =
        Lesson(
            id = LessonId("ls-test"),
            title = "Lección de prueba",
            exercises = exercises,
            scenario = scenario,
            canDo = canDo,
            grammarPoints = grammarPoints,
            culturalNotes = culturalNotes,
            microLesson = microLesson,
        )

    private fun multipleChoice(id: String): Exercise.MultipleChoice =
        Exercise.MultipleChoice(
            id = ExerciseId(id),
            question = "Elige",
            options = listOf("a", "b"),
            correctIndex = 0,
        )

    private fun orderWords(id: String): Exercise.OrderWords =
        Exercise.OrderWords(
            id = ExerciseId(id),
            tokens = listOf("How", "are", "you"),
            answerTokens = listOf("How", "are", "you"),
        )

    private fun matchPairs(): Exercise.MatchPairs =
        Exercise.MatchPairs(
            id = ExerciseId("e-match"),
            pairs = listOf(Exercise.MatchPair("hola", "Hello")),
        )
}

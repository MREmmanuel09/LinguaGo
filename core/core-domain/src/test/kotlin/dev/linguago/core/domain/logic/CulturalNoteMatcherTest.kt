package dev.linguago.core.domain.logic

import dev.linguago.core.domain.model.CulturalNote
import dev.linguago.core.domain.model.Exercise
import dev.linguago.core.domain.model.ExerciseId
import dev.linguago.core.domain.model.Lesson
import dev.linguago.core.domain.model.LessonId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CulturalNoteMatcherTest {
    private val playItByEar =
        CulturalNote(
            idiom = "play it by ear",
            note = "Decidir sobre la marcha.",
            translation = "improvisar",
        )

    @Test
    fun `encuentra el modismo en los tokens de una traduccion`() {
        val exercise =
            Exercise.Translate(
                id = ExerciseId("e1"),
                sourcePhrase = "Improvisemos",
                correctTokens = listOf("let", "us", "play", "it", "by", "ear"),
            )

        assertEquals(playItByEar, CulturalNoteMatcher.noteFor(lesson(playItByEar), exercise))
    }

    @Test
    fun `compara sin distinguir mayusculas`() {
        val exercise =
            Exercise.MultipleChoice(
                id = ExerciseId("e2"),
                question = "¿Qué significa PLAY IT BY EAR?",
                options = listOf("Improvisar", "Escuchar"),
                correctIndex = 0,
            )

        assertEquals(playItByEar, CulturalNoteMatcher.noteFor(lesson(playItByEar), exercise))
    }

    @Test
    fun `encuentra el modismo en un cloze`() {
        val exercise =
            Exercise.ClozeDragDrop(
                id = ExerciseId("e3"),
                passage = "We had no plan, so we had to {{hueco}}.",
                blanks =
                    listOf(
                        Exercise.ClozeBlank(
                            id = "hueco",
                            answer = "play it by ear",
                            options = listOf("play it by ear", "call it a day"),
                        ),
                    ),
            )

        assertEquals(playItByEar, CulturalNoteMatcher.noteFor(lesson(playItByEar), exercise))
    }

    @Test
    fun `devuelve null cuando la leccion no define notas`() {
        val exercise =
            Exercise.SpeakRepeat(
                id = ExerciseId("e4"),
                targetPhrase = "Let us play it by ear",
            )

        assertNull(CulturalNoteMatcher.noteFor(lesson(), exercise))
    }

    @Test
    fun `devuelve null cuando el modismo no aparece en el ejercicio`() {
        val exercise =
            Exercise.OrderWords(
                id = ExerciseId("e5"),
                tokens = listOf("I", "like", "tea"),
                answerTokens = listOf("I", "like", "tea"),
            )

        assertNull(CulturalNoteMatcher.noteFor(lesson(playItByEar), exercise))
    }

    @Test
    fun `gana la primera nota coincidente del orden de la leccion`() {
        val breakALeg = CulturalNote(idiom = "break a leg", note = "Buena suerte.")
        val exercise =
            Exercise.MatchPairs(
                id = ExerciseId("e6"),
                pairs =
                    listOf(
                        Exercise.MatchPair(left = "break a leg", right = "buena suerte"),
                        Exercise.MatchPair(left = "play it by ear", right = "improvisar"),
                    ),
            )

        assertEquals(playItByEar, CulturalNoteMatcher.noteFor(lesson(playItByEar, breakALeg), exercise))
    }

    private fun lesson(vararg notes: CulturalNote): Lesson =
        Lesson(
            id = LessonId("l1"),
            title = "Lección",
            exercises = emptyList(),
            culturalNotes = notes.toList(),
        )
}

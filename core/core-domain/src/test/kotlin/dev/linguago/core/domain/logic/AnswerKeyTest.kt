package dev.linguago.core.domain.logic

import dev.linguago.core.domain.model.Exercise
import dev.linguago.core.domain.model.ExerciseId
import org.junit.Assert.assertEquals
import org.junit.Test

class AnswerKeyTest {
    @Test
    fun `opciones devuelven indice y texto correcto`() {
        val exercise =
            Exercise.MultipleChoice(
                id = ExerciseId("e1"),
                question = "q",
                options = listOf("Hello", "Bye"),
                correctIndex = 0,
            )

        assertEquals(AnswerKey.Options(0, "Hello"), exercise.answerKey())
    }

    @Test
    fun `kana devuelve el indice de su romaji correcto`() {
        val exercise =
            Exercise.KanaChoose(
                id = ExerciseId("e1"),
                kana = "あ",
                romajiOptions = listOf("u", "a", "e"),
                correctRomaji = "a",
            )

        assertEquals(AnswerKey.Options(1, "a"), exercise.answerKey())
    }

    @Test
    fun `traduccion y orden devuelven sus tokens correctos`() {
        val translate =
            Exercise.Translate(
                id = ExerciseId("e1"),
                sourcePhrase = "Buenos días",
                correctTokens = listOf("Good", "morning"),
            )
        val order =
            Exercise.OrderWords(
                id = ExerciseId("e2"),
                tokens = listOf("are", "How", "you"),
                answerTokens = listOf("How", "are", "you"),
            )

        assertEquals(AnswerKey.Tokens(listOf("Good", "morning")), translate.answerKey())
        assertEquals(AnswerKey.Tokens(listOf("How", "are", "you")), order.answerKey())
    }

    @Test
    fun `parejas devuelven la asociacion canonica`() {
        val exercise =
            Exercise.MatchPairs(
                id = ExerciseId("e1"),
                pairs =
                    listOf(
                        Exercise.MatchPair("adiós", "Goodbye"),
                        Exercise.MatchPair("gracias", "Thank you"),
                    ),
            )

        assertEquals(
            AnswerKey.Pairs(listOf("adiós" to "Goodbye", "gracias" to "Thank you")),
            exercise.answerKey(),
        )
    }

    @Test
    fun `dictado y repeticion devuelven el texto objetivo`() {
        val dictation =
            Exercise.ListenType(
                id = ExerciseId("e1"),
                transcription = "Thank you",
                audioText = "Thank you",
            )
        val repeat =
            Exercise.SpeakRepeat(
                id = ExerciseId("e2"),
                targetPhrase = "Good evening",
            )

        assertEquals(AnswerKey.Text("Thank you"), dictation.answerKey())
        assertEquals(AnswerKey.Text("Good evening"), repeat.answerKey())
    }

    @Test
    fun `cloze devuelve la respuesta de cada hueco`() {
        val exercise =
            Exercise.ClozeDragDrop(
                id = ExerciseId("e1"),
                passage = "{{b1}} and {{b2}}",
                blanks =
                    listOf(
                        Exercise.ClozeBlank("b1", "one", listOf("one", "two")),
                        Exercise.ClozeBlank("b2", "three", listOf("three", "four")),
                    ),
            )

        assertEquals(
            AnswerKey.Cloze(linkedMapOf("b1" to "one", "b2" to "three")),
            exercise.answerKey(),
        )
    }

    @Test
    fun `produccion libre devuelve la respuesta modelo`() {
        val exercise =
            Exercise.FreeSpeakingPrompt(
                id = ExerciseId("e1"),
                prompt = "Habla",
                minTokens = 10,
                conceptClusters = listOf(listOf("deadline")),
                modelAnswer = "I'm afraid we can't.",
                selfAssessmentCriteria = listOf("¿Fui cortés?"),
            )

        assertEquals(AnswerKey.Speaking("I'm afraid we can't."), exercise.answerKey())
    }
}

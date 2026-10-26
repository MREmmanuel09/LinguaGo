package dev.linguago.core.ui.components

import dev.linguago.core.domain.logic.AnswerKey
import org.junit.Assert.assertEquals
import org.junit.Test

class AnswerKeyTextTest {
    @Test
    fun `opciones y textos se muestran tal cual`() {
        assertEquals("Hello", AnswerKey.Options(0, "Hello").displayText())
        assertEquals("Thank you", AnswerKey.Text("Thank you").displayText())
        assertEquals("I'm afraid we can't.", AnswerKey.Speaking("I'm afraid we can't.").displayText())
    }

    @Test
    fun `tokens se unen con espacios`() {
        assertEquals(
            "How are you",
            AnswerKey.Tokens(listOf("How", "are", "you")).displayText(),
        )
    }

    @Test
    fun `parejas y cloze se muestran una por linea`() {
        assertEquals(
            "adiós → Goodbye\ngracias → Thank you",
            AnswerKey.Pairs(listOf("adiós" to "Goodbye", "gracias" to "Thank you")).displayText(),
        )
        assertEquals(
            "b1: one\nb2: three",
            AnswerKey.Cloze(linkedMapOf("b1" to "one", "b2" to "three")).displayText(),
        )
    }
}

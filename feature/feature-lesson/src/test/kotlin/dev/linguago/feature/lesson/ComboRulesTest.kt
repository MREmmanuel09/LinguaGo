package dev.linguago.feature.lesson

import org.junit.Assert.assertEquals
import org.junit.Test

class ComboRulesTest {
    @Test
    fun `suma en acierto y reinicia en error`() {
        assertEquals(1, ComboRules.next(current = 0, isCorrect = true))
        assertEquals(3, ComboRules.next(current = 2, isCorrect = true))
        assertEquals(0, ComboRules.next(current = 5, isCorrect = false))
    }
}

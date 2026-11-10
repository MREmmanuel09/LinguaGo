package dev.linguago.feature.review

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Recuperación activa de vidas cada 5 tarjetas (ADR-048). */
class ReviewHeartRulesTest {
    @Test
    fun `las primeras cuatro tarjetas solo avanzan el contador`() {
        var progress = 0
        repeat(4) {
            val result = ReviewHeartRules.afterPassed(progress)
            progress = result.progress
            assertFalse(result.recovered)
        }
        assertEquals(4, progress)
    }

    @Test
    fun `la quinta tarjeta recupera una vida y reinicia el contador`() {
        val result = ReviewHeartRules.afterPassed(4)

        assertTrue(result.recovered)
        assertEquals(0, result.progress)
    }

    @Test
    fun `el contador tolera estados invalidos sin desbordar`() {
        val result = ReviewHeartRules.afterPassed(-3)

        assertFalse(result.recovered)
        assertEquals(1, result.progress)
    }
}

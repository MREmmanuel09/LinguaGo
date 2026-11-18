package dev.linguago.core.domain.placement

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Reglas de las cinco vidas locales del examen de nivelación (ADR-058). */
public class PlacementLivesTest {
    @Test
    public fun `el intento empieza con cinco vidas`() {
        assertEquals(5, PlacementLives.MAX)
    }

    @Test
    public fun `cada fallo resta una vida sin bajar de cero`() {
        assertEquals(4, PlacementLives.afterMistake(5))
        assertEquals(1, PlacementLives.afterMistake(2))
        assertEquals(0, PlacementLives.afterMistake(1))
        assertEquals(0, PlacementLives.afterMistake(0))
    }

    @Test
    public fun `el intento termina al agotar las vidas`() {
        assertFalse(PlacementLives.isExhausted(1))
        assertTrue(PlacementLives.isExhausted(0))
        assertTrue(PlacementLives.isExhausted(-1))
    }
}

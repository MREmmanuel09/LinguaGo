package dev.linguago.core.domain.hearts

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Reglas de vidas (ADR-025): regeneración por tiempo y consumo acotado. */
public class HeartsRulesTest {
    @Test
    public fun `initial devuelve el tanque lleno`() {
        val state = HeartsRules.initial(1_000L)
        assertEquals(HEARTS_MAX, state.hearts)
        assertEquals(1_000L, state.lastRegenEpochSecond)
    }

    @Test
    public fun `consumir desde lleno arranca el reloj de regeneracion`() {
        val state = HeartsRules.consume(HeartsRules.initial(1_000L), 1_000L)
        assertEquals(9, state.hearts)
        assertEquals(1_000L, state.lastRegenEpochSecond)
    }

    @Test
    public fun `una vida vuelve tras 60 segundos`() {
        val consumed = HeartsRules.consume(HeartsRules.initial(1_000L), 1_000L)
        val regen = HeartsRules.regenerated(consumed, 1_060L)
        assertEquals(10, regen.hearts)
        assertEquals(1_060L, regen.lastRegenEpochSecond)
    }

    @Test
    public fun `la regeneracion no pasa del maximo`() {
        val state = HeartsState(hearts = 2, lastRegenEpochSecond = 1_000L)
        val regen = HeartsRules.regenerated(state, 1_000L + 60L * 100)
        assertEquals(HEARTS_MAX, regen.hearts)
    }

    @Test
    public fun `consumir nunca baja de cero`() {
        val empty = HeartsState(hearts = 0, lastRegenEpochSecond = 1_000L)
        val state = HeartsRules.consume(empty, 1_030L)
        assertEquals(0, state.hearts)
    }

    @Test
    public fun `el tiempo fraccionario se conserva entre lecturas`() {
        val state = HeartsState(hearts = 5, lastRegenEpochSecond = 1_000L)
        val first = HeartsRules.regenerated(state, 1_090L)
        assertEquals(6, first.hearts)
        assertEquals(1_060L, first.lastRegenEpochSecond)
        val second = HeartsRules.regenerated(first, 1_115L)
        assertEquals(6, second.hearts)
        val third = HeartsRules.regenerated(second, 1_120L)
        assertEquals(7, third.hearts)
    }

    @Test
    public fun `segundos hasta la proxima vida`() {
        assertEquals(0L, HeartsRules.secondsUntilNextHeart(HeartsRules.initial(1_000L), 1_000L))
        val state = HeartsState(hearts = 9, lastRegenEpochSecond = 1_000L)
        assertEquals(60L, HeartsRules.secondsUntilNextHeart(state, 1_000L))
        assertEquals(30L, HeartsRules.secondsUntilNextHeart(state, 1_030L))
        assertTrue(HeartsRules.secondsUntilNextHeart(state, 1_059L) >= 1L)
    }

    @Test
    public fun `refill restaura el tanque`() {
        val state = HeartsRules.refill(HeartsState(hearts = 0, lastRegenEpochSecond = 1_000L), 2_000L)
        assertEquals(HEARTS_MAX, state.hearts)
        assertEquals(2_000L, state.lastRegenEpochSecond)
    }
}

package dev.linguago.core.audio

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AudioEngineStateTest {
    @Test
    fun `verificacion extraccion y carga cuentan como warmup`() {
        assertTrue(AudioEngineState.Verifying.isWarmingUp)
        assertTrue(AudioEngineState.Extracting.isWarmingUp)
        assertTrue(AudioEngineState.Loading.isWarmingUp)
    }

    @Test
    fun `idle ready y degradado no cuentan como warmup`() {
        assertFalse(AudioEngineState.Idle.isWarmingUp)
        assertFalse(AudioEngineState.Ready.isWarmingUp)
        assertFalse(AudioEngineState.Degraded("sin modelos").isWarmingUp)
    }
}

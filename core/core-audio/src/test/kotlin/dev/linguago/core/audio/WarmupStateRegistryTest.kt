package dev.linguago.core.audio

import dev.linguago.core.domain.model.Language
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.atomic.AtomicInteger

class WarmupStateRegistryTest {
    @Test
    fun `parte en Idle y queda Ready tras el warmup`() {
        val registry = WarmupStateRegistry()

        assertEquals(AudioEngineState.Idle, registry.stateOf(Language.ENGLISH).value)

        runBlocking {
            registry.warmUpOnce(Language.ENGLISH) {
                registry.stateOf(Language.ENGLISH).value = AudioEngineState.Ready
            }
        }

        assertTrue(registry.isReady(Language.ENGLISH))
        assertEquals(AudioEngineState.Ready, registry.stateOf(Language.ENGLISH).value)
    }

    @Test
    fun `warmups concurrentes del mismo idioma ejecutan el bloque una sola vez`() {
        val registry = WarmupStateRegistry()
        val executions = AtomicInteger(0)

        runBlocking {
            List(CONCURRENT_WARMUPS) {
                launch(Dispatchers.Default) {
                    registry.warmUpOnce(Language.ENGLISH) {
                        executions.incrementAndGet()
                        delay(SLOW_BLOCK_MILLIS)
                        registry.stateOf(Language.ENGLISH).value = AudioEngineState.Ready
                    }
                }
            }.joinAll()
        }

        assertEquals(1, executions.get())
        assertTrue(registry.isReady(Language.ENGLISH))
    }

    @Test
    fun `idiomas distintos no comparten estado ni lock`() {
        val registry = WarmupStateRegistry()

        registry.stateOf(Language.ENGLISH).value = AudioEngineState.Ready

        assertTrue(registry.isReady(Language.ENGLISH))
        assertFalse(registry.isReady(Language.JAPANESE))
        assertEquals(AudioEngineState.Idle, registry.stateOf(Language.JAPANESE).value)
    }

    @Test
    fun `Degraded no se considera ready y permite reintentar`() {
        val registry = WarmupStateRegistry()
        val executions = AtomicInteger(0)

        runBlocking {
            repeat(2) {
                registry.warmUpOnce(Language.FRENCH) {
                    executions.incrementAndGet()
                    registry.stateOf(Language.FRENCH).value = AudioEngineState.Degraded("sin modelo")
                }
            }
        }

        assertEquals(2, executions.get())
        assertFalse(registry.isReady(Language.FRENCH))
    }

    private companion object {
        const val CONCURRENT_WARMUPS = 8
        const val SLOW_BLOCK_MILLIS = 50L
    }
}

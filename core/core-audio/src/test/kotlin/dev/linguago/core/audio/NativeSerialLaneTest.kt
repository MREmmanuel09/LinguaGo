package dev.linguago.core.audio

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NativeSerialLaneTest {
    @Test
    fun `close espera a que termine el uso activo`() =
        runBlocking {
            val closed = mutableListOf<String>()
            val lane = NativeSerialLane<String> { value -> closed += value }
            assertTrue(lane.install("model"))

            val entered = CompletableDeferred<Unit>()
            val release = CompletableDeferred<Unit>()
            val use =
                launch {
                    lane.use {
                        entered.complete(Unit)
                        release.await()
                    }
                }
            entered.await()

            val close = launch { lane.close() }
            delay(50)
            assertTrue("close no debe cerrar con un uso activo", close.isActive)
            assertTrue(closed.isEmpty())

            release.complete(Unit)
            use.join()
            close.join()
            assertEquals(listOf("model"), closed)
        }

    @Test
    fun `la cancelacion libera el carril y permite cerrar`() =
        runBlocking {
            val closed = mutableListOf<String>()
            val lane = NativeSerialLane<String> { value -> closed += value }
            assertTrue(lane.install("model"))

            val entered = CompletableDeferred<Unit>()
            val use =
                launch {
                    lane.use {
                        entered.complete(Unit)
                        awaitCancellation()
                    }
                }
            entered.await()
            use.cancelAndJoin()

            lane.close()
            assertEquals(listOf("model"), closed)
            assertNull(lane.use { value -> value })
        }

    @Test
    fun `un recurso instalado tras close se libera al instante`() =
        runBlocking {
            val closed = mutableListOf<String>()
            val lane = NativeSerialLane<String> { value -> closed += value }
            lane.close()

            assertFalse(lane.install("late"))
            assertEquals(listOf("late"), closed)
            assertNull(lane.use { value -> value })
        }
}

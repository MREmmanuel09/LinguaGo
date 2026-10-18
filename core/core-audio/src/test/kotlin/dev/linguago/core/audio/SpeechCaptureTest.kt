package dev.linguago.core.audio

import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SpeechCaptureTest {
    @Test
    fun `emite un update por chunk y cierra con el texto final`() {
        val clock = FakeClock()
        val source = FakeSource(clock) { buffer -> fillConstant(buffer, 3277) }
        val recognizer = FakeRecognizer()
        val speechCapture = capture(clock, source, recognizer)

        val updates = collectUpdates(speechCapture)

        assertEquals(4, updates.size)
        assertEquals(1, source.startCount)
        assertEquals(3, source.readCount)
        assertEquals(1, source.releaseCount)
        assertEquals(1, recognizer.closeCount)
        updates.dropLast(1).forEach { update -> assertNull(update.finalText) }
        assertEquals("", updates.last().finalText)
    }

    @Test
    fun `calcula rmsDb de una onda constante`() {
        val clock = FakeClock()
        val source = FakeSource(clock) { buffer -> fillConstant(buffer, 3277) }

        val updates = collectUpdates(capture(clock, source, FakeRecognizer()))

        assertEquals(-20.0, updates.first().rmsDb.toDouble(), 0.05)
    }

    @Test
    fun `el silencio cae al piso de dB`() {
        val clock = FakeClock()
        val source = FakeSource(clock) { buffer -> fillConstant(buffer, 0) }

        val updates = collectUpdates(capture(clock, source, FakeRecognizer()))

        assertEquals(SpeechCapture.SILENCE_DB.toDouble(), updates.first().rmsDb.toDouble(), 0.0)
    }

    @Test
    fun `propaga los parciales de vosk`() {
        val clock = FakeClock()
        val source = FakeSource(clock) { buffer -> fillConstant(buffer, 3277) }
        val recognizer = FakeRecognizer(partials = listOf("ho", "hola"))

        val updates = collectUpdates(capture(clock, source, recognizer))

        assertEquals("ho", updates[0].partialText)
        assertEquals("hola", updates[1].partialText)
    }

    @Test
    fun `el endpoint con texto detiene la captura`() {
        val clock = FakeClock()
        val source = FakeSource(clock) { buffer -> fillConstant(buffer, 3277) }
        val recognizer =
            FakeRecognizer(
                steps =
                    listOf(
                        FakeRecognizer.Step(endpoint = false),
                        FakeRecognizer.Step(endpoint = true, result = "good morning"),
                    ),
            )

        val updates = collectUpdates(capture(clock, source, recognizer))

        assertEquals(2, source.readCount)
        assertEquals(3, updates.size)
        assertEquals("good morning", updates.last().finalText)
    }

    @Test
    fun `endpoint sin texto no detiene la captura`() {
        val clock = FakeClock()
        val source = FakeSource(clock) { buffer -> fillConstant(buffer, 3277) }
        val recognizer = FakeRecognizer(steps = listOf(FakeRecognizer.Step(endpoint = true)), finalText = "resto")

        val updates = collectUpdates(capture(clock, source, recognizer))

        assertEquals(3, source.readCount)
        assertEquals("resto", updates.last().finalText)
    }

    @Test
    fun `el último parcial gana al finalResult si no hubo endpoint`() {
        val clock = FakeClock()
        val source = FakeSource(clock) { buffer -> fillConstant(buffer, 3277) }
        val recognizer = FakeRecognizer(partials = listOf("hola"), finalText = "variante")

        val updates = collectUpdates(capture(clock, source, recognizer))

        assertEquals("hola", updates.last().finalText)
        assertEquals("hola", updates.last().partialText)
    }

    @Test
    fun `sin parciales ni endpoint usa finalResult`() {
        val clock = FakeClock()
        val source = FakeSource(clock) { buffer -> fillConstant(buffer, 3277) }
        val recognizer = FakeRecognizer(finalText = "despedida")

        val updates = collectUpdates(capture(clock, source, recognizer))

        assertEquals("despedida", updates.last().finalText)
    }

    @Test
    fun `cancelación libera fuente y cierra recognizer`() {
        val clock = FakeClock()
        val source = FakeSource(clock) { buffer -> fillConstant(buffer, 3277) }
        val recognizer = FakeRecognizer()
        val speechCapture = capture(clock, source, recognizer)

        runBlocking {
            val job =
                launch {
                    var emitted = 0
                    speechCapture.run {
                        emitted++
                        if (emitted == 2) cancel()
                    }
                }
            job.join()
            assertTrue(job.isCancelled)
        }

        assertEquals(1, source.releaseCount)
        assertEquals(1, recognizer.closeCount)
    }

    @Test
    fun `excepción de la fuente cierra todo una sola vez`() {
        val clock = FakeClock()
        val source = FakeSource(clock) { error("audio roto") }
        val recognizer = FakeRecognizer()
        val speechCapture = capture(clock, source, recognizer)

        val failure = runCatching { runBlocking { speechCapture.run { } } }

        assertTrue(failure.exceptionOrNull() is IllegalStateException)
        assertEquals(1, source.releaseCount)
        assertEquals(1, recognizer.closeCount)
    }

    private fun capture(
        clock: FakeClock,
        source: PcmSource,
        recognizer: StreamRecognizer,
        maxCaptureNanos: Long = 3 * CHUNK_NANOS,
    ): SpeechCapture = SpeechCapture(source, recognizer, clock::nanoTime, maxCaptureNanos)

    private fun collectUpdates(capture: SpeechCapture): List<SpeechCaptureUpdate> =
        runBlocking {
            val updates = mutableListOf<SpeechCaptureUpdate>()
            capture.run { update -> updates.add(update) }
            updates
        }

    private fun fillConstant(
        buffer: ByteArray,
        sample: Int,
    ): Int {
        var index = 0
        while (index + 1 < buffer.size) {
            buffer[index] = (sample and 0xFF).toByte()
            buffer[index + 1] = ((sample shr 8) and 0xFF).toByte()
            index += 2
        }
        return buffer.size
    }

    private class FakeClock {
        private var now = 0L

        fun advance(by: Long) {
            now += by
        }

        fun nanoTime(): Long = now
    }

    private class FakeSource(
        private val clock: FakeClock,
        private val fill: (ByteArray) -> Int,
    ) : PcmSource {
        var startCount = 0
        var readCount = 0
        var releaseCount = 0

        override fun start() {
            startCount++
        }

        override fun read(buffer: ByteArray): Int {
            readCount++
            val length = fill(buffer)
            clock.advance(CHUNK_NANOS)
            return length
        }

        override fun release() {
            releaseCount++
        }
    }

    private class FakeRecognizer(
        private val steps: List<Step> = emptyList(),
        private val partials: List<String> = emptyList(),
        private val finalText: String = "",
    ) : StreamRecognizer {
        var closeCount = 0
        private var reads = 0
        private var lastPartial = ""
        private var endpointResult = ""

        override fun acceptWaveForm(
            buffer: ByteArray,
            length: Int,
        ): Boolean {
            val step = steps.getOrElse(reads) { Step(endpoint = false) }
            reads++
            endpointResult = step.result
            return step.endpoint
        }

        override fun resultText(): String = endpointResult

        override fun partialText(): String {
            lastPartial = partials.getOrElse(reads - 1) { lastPartial }
            return lastPartial
        }

        override fun finalText(): String = finalText

        override fun close() {
            closeCount++
        }

        data class Step(
            val endpoint: Boolean,
            val result: String = "",
        )
    }

    private companion object {
        const val CHUNK_NANOS = 50_000_000L
    }
}

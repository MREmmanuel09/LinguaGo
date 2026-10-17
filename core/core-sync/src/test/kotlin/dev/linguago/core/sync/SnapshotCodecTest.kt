package dev.linguago.core.sync

import dev.linguago.core.common.LinguaError
import dev.linguago.core.common.Result
import dev.linguago.core.domain.model.SyncSnapshot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Codec del snapshot v1 (ADR-013): ida y vuelta + rechazo de formatos. */
public class SnapshotCodecTest {
    private val codec = SnapshotCodec()

    private val snapshot =
        SyncSnapshot(
            exportedAtMillis = 1_700_000_000_000,
            streakCurrent = 3,
            streakLongest = 7,
            streakLastDateEpochDay = 20_000,
            totalXp = 240,
            lessons =
                listOf(
                    SyncSnapshot.LessonEntry(
                        lessonId = "es-en-l1",
                        completed = true,
                        xp = 40,
                        attempts = 1,
                    ),
                ),
        )

    @Test
    public fun `encode-decode round-trip preserva los campos`() {
        val encoded = (codec.encode(snapshot) as Result.Success).value
        val decoded = (codec.decode(encoded) as Result.Success).value

        assertEquals(snapshot, decoded)
    }

    @Test
    public fun `encode incluye la version del formato`() {
        val encoded = String((codec.encode(snapshot) as Result.Success).value, Charsets.UTF_8)
        assertTrue(encoded.contains("\"schemaVersion\":1"))
    }

    @Test
    public fun `version desconocida se rechaza como InvalidContent`() {
        val payload = """{"schemaVersion":99,"exportedAtMillis":0}""".toByteArray()
        val result = codec.decode(payload)

        assertEquals(LinguaError.InvalidContent, (result as Result.Error).error)
    }

    @Test
    public fun `payload basura se rechaza como InvalidContent`() {
        val result = codec.decode("esto no es json".toByteArray())

        assertEquals(LinguaError.InvalidContent, (result as Result.Error).error)
    }

    @Test
    public fun `claves desconocidas se rechazan por strict mode`() {
        val payload =
            """{"schemaVersion":1,"exportedAtMillis":0,"extra":true}""".toByteArray()
        val result = codec.decode(payload)

        assertEquals(LinguaError.InvalidContent, (result as Result.Error).error)
    }

    @Test
    public fun `campos faltantes se rechazan`() {
        val payload = """{"schemaVersion":1}""".toByteArray()
        val result = codec.decode(payload)

        assertEquals(LinguaError.InvalidContent, (result as Result.Error).error)
    }
}

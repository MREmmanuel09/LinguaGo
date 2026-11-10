package dev.linguago.core.sync

import dev.linguago.core.common.LinguaError
import dev.linguago.core.common.Result
import dev.linguago.core.domain.hearts.HEARTS_MAX
import dev.linguago.core.domain.model.SyncSnapshot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Codec del snapshot v3 (ADR-013/ADR-025/ADR-050): ida y vuelta y compatibilidad v1/v2. */
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
        assertTrue(encoded.contains("\"schemaVersion\":3"))
    }

    @Test
    public fun `un snapshot v3 conserva las tarjetas FSRS`() {
        val withWords =
            snapshot.copy(
                words =
                    listOf(
                        SyncSnapshot.WordEntry(
                            word = "hola",
                            courseId = "es-en",
                            translation = "hello",
                            reviewCount = 2,
                            lastReviewEpochMillis = 100,
                            nextReviewEpochMillis = 200,
                            stability = 2.5f,
                            difficulty = 4f,
                            culturalNote = "Saludo",
                        ),
                    ),
            )
        val encoded = (codec.encode(withWords) as Result.Success).value
        val decoded = (codec.decode(encoded) as Result.Success).value

        assertEquals(withWords.words, decoded.words)
        assertEquals("Saludo", decoded.words.single().culturalNote)
    }

    @Test
    public fun `un snapshot v2 decodifica sin tarjetas y conserva vidas`() {
        val payload =
            """
            {"schemaVersion":2,"exportedAtMillis":0,"streakCurrent":1,"streakLongest":1,
             "streakLastDateEpochDay":0,"totalXp":10,"lessons":[],
             "heartsCurrent":4,"heartsLastRegenEpochSecond":99}
            """.trimIndent().toByteArray()

        val decoded = (codec.decode(payload) as Result.Success).value

        assertEquals(2, decoded.schemaVersion)
        assertEquals(4, decoded.heartsCurrent)
        assertEquals(99L, decoded.heartsLastRegenEpochSecond)
        assertTrue(decoded.words.isEmpty())
    }

    @Test
    public fun `un snapshot v1 decodifica con diez vidas por defecto`() {
        val payload =
            """
            {"schemaVersion":1,"exportedAtMillis":0,"streakCurrent":0,"streakLongest":0,
             "streakLastDateEpochDay":0,"totalXp":0,"lessons":[]}
            """.trimIndent().toByteArray()

        val decoded = (codec.decode(payload) as Result.Success).value

        assertEquals(1, decoded.schemaVersion)
        assertEquals(HEARTS_MAX, decoded.heartsCurrent)
        assertEquals(0L, decoded.heartsLastRegenEpochSecond)
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

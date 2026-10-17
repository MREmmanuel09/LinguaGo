package dev.linguago.core.domain.sync

import dev.linguago.core.domain.model.SyncSnapshot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Merge monótono de snapshots (ADR-013, I4: nunca borra progreso). */
public class SnapshotMergeTest {
    private fun entry(
        lessonId: String,
        completed: Boolean = true,
        xp: Int = 10,
        attempts: Int = 1,
    ): SyncSnapshot.LessonEntry =
        SyncSnapshot.LessonEntry(
            lessonId = lessonId,
            completed = completed,
            xp = xp,
            attempts = attempts,
        )

    private fun snapshot(
        streakCurrent: Int = 0,
        streakLongest: Int = 0,
        lastDay: Long = 0,
        totalXp: Int = 0,
        lessons: List<SyncSnapshot.LessonEntry> = emptyList(),
        exportedAtMillis: Long = 0,
    ): SyncSnapshot =
        SyncSnapshot(
            exportedAtMillis = exportedAtMillis,
            streakCurrent = streakCurrent,
            streakLongest = streakLongest,
            streakLastDateEpochDay = lastDay,
            totalXp = totalXp,
            lessons = lessons,
        )

    @Test
    public fun `remoto mas avanzado gana campo por campo`() {
        val local = snapshot(streakCurrent = 2, streakLongest = 2, lastDay = 10, totalXp = 50)
        val remote = snapshot(streakCurrent = 5, streakLongest = 9, lastDay = 20, totalXp = 300)

        val merged = local.mergedWith(remote)

        assertEquals(5, merged.streakCurrent)
        assertEquals(9, merged.streakLongest)
        assertEquals(20, merged.streakLastDateEpochDay)
        assertEquals(300, merged.totalXp)
    }

    @Test
    public fun `local mas avanzado no se pierde al recibir remoto viejo`() {
        val local = snapshot(streakCurrent = 8, streakLongest = 8, lastDay = 99, totalXp = 900)
        val remote = snapshot(streakCurrent = 1, streakLongest = 1, lastDay = 3, totalXp = 10)

        val merged = local.mergedWith(remote)

        assertEquals(8, merged.streakCurrent)
        assertEquals(8, merged.streakLongest)
        assertEquals(99, merged.streakLastDateEpochDay)
        assertEquals(900, merged.totalXp)
    }

    @Test
    public fun `lecciones se unen y cada campo toma el maximo`() {
        val local =
            snapshot(lessons = listOf(entry("l1", xp = 40, attempts = 2), entry("l2", completed = true, xp = 10)))
        val remote =
            snapshot(lessons = listOf(entry("l1", xp = 70, attempts = 1), entry("l3", xp = 15)))

        val merged = local.mergedWith(remote)

        assertEquals(
            listOf(
                entry("l1", completed = true, xp = 70, attempts = 2),
                entry("l2", completed = true, xp = 10, attempts = 1),
                entry("l3", completed = true, xp = 15, attempts = 1),
            ),
            merged.lessons,
        )
    }

    @Test
    public fun `completed nunca baja a false en el merge`() {
        val local = snapshot(lessons = listOf(entry("l1", completed = false, xp = 0, attempts = 0)))
        val remote = snapshot(lessons = listOf(entry("l1", completed = true, xp = 30, attempts = 3)))

        val merged = local.mergedWith(remote)

        assertTrue(merged.lessons.single().completed)
    }

    @Test
    public fun `merge es conmutativo`() {
        val local = snapshot(streakCurrent = 4, totalXp = 120, lessons = listOf(entry("l1")))
        val remote =
            snapshot(streakCurrent = 6, totalXp = 80, lastDay = 15, lessons = listOf(entry("l2", xp = 5)))

        assertEquals(local.mergedWith(remote), remote.mergedWith(local))
    }
}

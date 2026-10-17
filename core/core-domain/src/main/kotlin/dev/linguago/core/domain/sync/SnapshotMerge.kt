package dev.linguago.core.domain.sync

import dev.linguago.core.domain.model.SyncSnapshot

/**
 * Merge monótono de snapshots (ADR-013, I4): cada campo toma el máximo entre
 * local y remoto (lecciones: `completed` es OR; XP/attempts máximo). Nunca
 * elimina progreso, incluso si el remoto fuera corrupto o más antiguo.
 */
public fun SyncSnapshot.mergedWith(remote: SyncSnapshot): SyncSnapshot {
    val merged = LinkedHashMap<String, SyncSnapshot.LessonEntry>()
    lessons.forEach { entry -> merged[entry.lessonId] = entry }
    remote.lessons.forEach { remoteEntry ->
        merged[remoteEntry.lessonId] =
            merged[remoteEntry.lessonId]?.mergedWith(remoteEntry) ?: remoteEntry
    }
    return SyncSnapshot(
        schemaVersion = SyncSnapshot.SCHEMA_VERSION,
        exportedAtMillis = maxOf(exportedAtMillis, remote.exportedAtMillis),
        streakCurrent = maxOf(streakCurrent, remote.streakCurrent),
        streakLongest = maxOf(streakLongest, remote.streakLongest),
        streakLastDateEpochDay = maxOf(streakLastDateEpochDay, remote.streakLastDateEpochDay),
        totalXp = maxOf(totalXp, remote.totalXp),
        lessons = merged.values.sortedBy { it.lessonId },
    )
}

private fun SyncSnapshot.LessonEntry.mergedWith(remote: SyncSnapshot.LessonEntry): SyncSnapshot.LessonEntry =
    SyncSnapshot.LessonEntry(
        lessonId = lessonId,
        completed = completed || remote.completed,
        xp = maxOf(xp, remote.xp),
        attempts = maxOf(attempts, remote.attempts),
    )

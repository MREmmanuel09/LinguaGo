package dev.linguago.core.domain.sync

import dev.linguago.core.domain.model.SyncSnapshot

/**
 * Merge monótono de snapshots (ADR-013/ADR-050, I4): cada campo toma el máximo
 * entre local y remoto (lecciones: `completed` es OR; XP/attempts máximo).
 * Nunca elimina progreso, incluso si el remoto fuera corrupto o más antiguo.
 *
 * Vidas (ADR-025) son la excepción deliberada: toman el **mínimo** para que
 * una penalización no desaparezca al sincronizar, y el ancla de regeneración
 * toma el **máximo** para no regalar vidas por relojes desfasados.
 *
 * Tarjetas FSRS (ADR-050): para cada clave `(word, courseId)` gana la entrada
 * con más repasos y, en empate, la de `lastReviewEpochMillis` más reciente.
 * Así una tarjeta aprendida nunca desaparece ni retrocede a no aprendida.
 */
public fun SyncSnapshot.mergedWith(remote: SyncSnapshot): SyncSnapshot {
    val mergedLessons = LinkedHashMap<String, SyncSnapshot.LessonEntry>()
    lessons.forEach { entry -> mergedLessons[entry.lessonId] = entry }
    remote.lessons.forEach { remoteEntry ->
        mergedLessons[remoteEntry.lessonId] =
            mergedLessons[remoteEntry.lessonId]?.mergedWith(remoteEntry) ?: remoteEntry
    }
    val mergedWords = LinkedHashMap<String, SyncSnapshot.WordEntry>()
    words.forEach { entry -> mergedWords[entry.key] = entry }
    remote.words.forEach { remoteEntry ->
        mergedWords[remoteEntry.key] =
            mergedWords[remoteEntry.key]?.mergedWith(remoteEntry) ?: remoteEntry
    }
    return SyncSnapshot(
        schemaVersion = SyncSnapshot.SCHEMA_VERSION,
        exportedAtMillis = maxOf(exportedAtMillis, remote.exportedAtMillis),
        streakCurrent = maxOf(streakCurrent, remote.streakCurrent),
        streakLongest = maxOf(streakLongest, remote.streakLongest),
        streakLastDateEpochDay = maxOf(streakLastDateEpochDay, remote.streakLastDateEpochDay),
        totalXp = maxOf(totalXp, remote.totalXp),
        lessons = mergedLessons.values.sortedBy { it.lessonId },
        heartsCurrent = minOf(heartsCurrent, remote.heartsCurrent),
        heartsLastRegenEpochSecond = maxOf(heartsLastRegenEpochSecond, remote.heartsLastRegenEpochSecond),
        words = mergedWords.values.sortedWith(compareBy({ it.courseId }, { it.word })),
    )
}

private fun SyncSnapshot.LessonEntry.mergedWith(remote: SyncSnapshot.LessonEntry): SyncSnapshot.LessonEntry =
    SyncSnapshot.LessonEntry(
        lessonId = lessonId,
        completed = completed || remote.completed,
        xp = maxOf(xp, remote.xp),
        attempts = maxOf(attempts, remote.attempts),
        speakingAttempts = maxOf(speakingAttempts, remote.speakingAttempts),
        speakingPassedAttempts = maxOf(speakingPassedAttempts, remote.speakingPassedAttempts),
        speakingBestWpm = maxOf(speakingBestWpm, remote.speakingBestWpm),
        speakingLastScore = maxOf(speakingLastScore, remote.speakingLastScore),
        bestScorePercent = maxOf(bestScorePercent, remote.bestScorePercent),
        lastScorePercent = maxOf(lastScorePercent, remote.lastScorePercent),
    )

private fun SyncSnapshot.WordEntry.mergedWith(remote: SyncSnapshot.WordEntry): SyncSnapshot.WordEntry {
    val recent = if (isMoreAdvancedThan(remote)) this else remote
    val other = if (recent === this) remote else this
    return recent.copy(
        translation = recent.translation.ifBlank { other.translation },
        errorCount = maxOf(errorCount, remote.errorCount),
        lastErrorType = recent.lastErrorType ?: other.lastErrorType,
        culturalNote = recent.culturalNote ?: other.culturalNote,
    )
}

private fun SyncSnapshot.WordEntry.isMoreAdvancedThan(remote: SyncSnapshot.WordEntry): Boolean =
    when {
        reviewCount != remote.reviewCount -> reviewCount > remote.reviewCount
        lastReviewEpochMillis != remote.lastReviewEpochMillis -> lastReviewEpochMillis > remote.lastReviewEpochMillis
        else -> stability >= remote.stability
    }

private val SyncSnapshot.WordEntry.key: String
    get() = "$courseId\u0000$word"

package dev.linguago.core.domain.model

import dev.linguago.core.domain.hearts.HEARTS_MAX
import kotlinx.serialization.Serializable

/**
 * Snapshot de progreso para respaldo/sincronización (ADR-013/ADR-050). Es un
 * documento JSON autocontenido: racha, lecciones, vidas y mazo FSRS. La nube
 * es opcional (I2) y el merge resultante es monótono (I4: nunca borra
 * progreso). v2 añadió vidas (ADR-025); v3 añade tarjetas FSRS y métricas de
 * voz/precisión. Los snapshots v1/v2 siguen decodificando.
 */
@Serializable
public data class SyncSnapshot(
    val schemaVersion: Int = SCHEMA_VERSION,
    val exportedAtMillis: Long,
    val streakCurrent: Int,
    val streakLongest: Int,
    val streakLastDateEpochDay: Long,
    val totalXp: Int,
    val lessons: List<LessonEntry>,
    /** Vidas efectivas en el momento del snapshot (ADR-025). */
    val heartsCurrent: Int = HEARTS_MAX,
    /** Ancla epoch (segundos) de la regeneración de vidas (ADR-025). */
    val heartsLastRegenEpochSecond: Long = 0L,
    /** Mazo FSRS completo (ADR-050); vacío en snapshots v1/v2. */
    val words: List<WordEntry> = emptyList(),
) {
    /** Fila de progreso de una lección dentro del snapshot. */
    @Serializable
    public data class LessonEntry(
        val lessonId: String,
        val completed: Boolean,
        val xp: Int,
        val attempts: Int,
        val speakingAttempts: Int = 0,
        val speakingPassedAttempts: Int = 0,
        val speakingBestWpm: Float = 0f,
        val speakingLastScore: Float = 0f,
        val bestScorePercent: Int = 0,
        val lastScorePercent: Int = 0,
    )

    /** Tarjeta FSRS dentro del snapshot v3; los campos nuevos tienen defaults. */
    @Serializable
    public data class WordEntry(
        val word: String,
        val courseId: String,
        val translation: String,
        val reviewCount: Int,
        val lastReviewEpochMillis: Long,
        val nextReviewEpochMillis: Long,
        val stability: Float,
        val difficulty: Float,
        val errorCount: Int = 0,
        val lastErrorType: String? = null,
        val culturalNote: String? = null,
    )

    public companion object {
        /** Versión del formato de snapshot: v3 añade `words` (ADR-050). */
        public const val SCHEMA_VERSION: Int = 3
    }
}

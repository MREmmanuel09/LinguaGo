package dev.linguago.core.domain.model

import dev.linguago.core.domain.hearts.HEARTS_MAX
import kotlinx.serialization.Serializable

/**
 * Snapshot de progreso para respaldo/sincronización (Fase 3, ADR-013). Es un
 * documento JSON autocontenido: racha + progreso por lección + vidas. La nube
 * es opcional (I2) y el merge resultante es monótono (I4: nunca borra
 * progreso). v2 añade vidas (ADR-025); un snapshot v1 decodifica con 10 vidas.
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
) {
    /** Fila de progreso de una lección dentro del snapshot. */
    @Serializable
    public data class LessonEntry(
        val lessonId: String,
        val completed: Boolean,
        val xp: Int,
        val attempts: Int,
    )

    public companion object {
        /** Versión del formato de snapshot: v2 añade vidas (ADR-013/025). */
        public const val SCHEMA_VERSION: Int = 2
    }
}

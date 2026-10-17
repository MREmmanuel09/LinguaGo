package dev.linguago.core.domain.model

import kotlinx.serialization.Serializable

/**
 * Snapshot de progreso para respaldo/sincronización (Fase 3, ADR-013). Es un
 * documento JSON v1 autocontenido: racha + progreso por lección. La nube es
 * opcional (I2) y el merge resultante es monótono (I4: nunca borra progreso).
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
        /** Versión del formato de snapshot (ADR-013). */
        public const val SCHEMA_VERSION: Int = 1
    }
}

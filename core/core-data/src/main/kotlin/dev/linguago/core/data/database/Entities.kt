package dev.linguago.core.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidades Room del progreso (ADR-005). [StreakProgressEntity] es un
 * registro singleton (id fijo): la racha del usuario es única. El id de
 * lección es su clave primaria: completar dos veces actualiza la fila.
 */
@Entity(tableName = "streak_progress")
public data class StreakProgressEntity(
    @PrimaryKey public val id: Long,
    public val current: Int,
    public val longest: Int,
    /** Día de la última actividad en epoch days; 0 significa "sin actividad". */
    public val lastDateEpochDay: Long,
    public val totalXp: Int,
)

@Entity(tableName = "lesson_progress")
public data class LessonProgressEntity(
    @PrimaryKey public val lessonId: String,
    public val completed: Boolean,
    public val xp: Int,
    public val attempts: Int,
)

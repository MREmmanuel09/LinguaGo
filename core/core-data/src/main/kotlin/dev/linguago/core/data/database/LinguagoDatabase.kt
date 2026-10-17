package dev.linguago.core.data.database

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * Base local del progreso (ADR-005: Room sobre el estado del usuario).
 * El contenido pedagógico vive en assets (core-content), no aquí.
 */
@Database(
    entities = [StreakProgressEntity::class, LessonProgressEntity::class],
    version = 1,
    exportSchema = true,
)
public abstract class LinguagoDatabase : RoomDatabase() {
    public abstract fun streakProgressDao(): StreakProgressDao

    public abstract fun lessonProgressDao(): LessonProgressDao
}

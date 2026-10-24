package dev.linguago.core.data.database

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * Base local del progreso (ADR-005: Room sobre el estado del usuario).
 * El contenido pedagógico vive en assets (core-content), no aquí.
 * v2: agrega `word_progress` para el repaso espaciado FSRS (ADR-016).
 * v3: agrega métricas de producción oral y errores frecuentes (ADR-021).
 * v4: agrega `hearts_progress` para las vidas con regeneración (ADR-025).
 */
@Database(
    entities = [
        StreakProgressEntity::class,
        LessonProgressEntity::class,
        WordProgressEntity::class,
        HeartsProgressEntity::class,
    ],
    version = 4,
    exportSchema = true,
)
public abstract class LinguagoDatabase : RoomDatabase() {
    public abstract fun streakProgressDao(): StreakProgressDao

    public abstract fun lessonProgressDao(): LessonProgressDao

    public abstract fun wordProgressDao(): WordProgressDao

    public abstract fun heartsProgressDao(): HeartsProgressDao
}

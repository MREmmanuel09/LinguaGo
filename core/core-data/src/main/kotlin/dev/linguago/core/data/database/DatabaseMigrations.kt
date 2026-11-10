package dev.linguago.core.data.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Migraciones explícitas de `linguago.db` (ADR-005/ADR-016/ADR-021/ADR-050). Se
 * exponen para que el test instrumentado de preservación de progreso pueda
 * ejecutarlas con `MigrationTestHelper`; la app las registra en `DataModule`.
 */
public object DatabaseMigrations {
    /** v1→v2 (ADR-016): crea la tabla FSRS `word_progress` sin perder nada. */
    public val MIGRATION_1_2: Migration =
        object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `word_progress` (" +
                        "`word` TEXT NOT NULL, " +
                        "`courseId` TEXT NOT NULL, " +
                        "`translation` TEXT NOT NULL, " +
                        "`reviewCount` INTEGER NOT NULL, " +
                        "`lastReviewEpochMillis` INTEGER NOT NULL, " +
                        "`nextReviewEpochMillis` INTEGER NOT NULL, " +
                        "`stability` REAL NOT NULL, " +
                        "`difficulty` REAL NOT NULL, " +
                        "PRIMARY KEY(`word`, `courseId`))",
                )
            }
        }

    /**
     * v2→v3 (ADR-021): añade métricas locales de producción oral a
     * `lesson_progress` y el registro de errores frecuentes a
     * `word_progress`. Las columnas nuevas tienen defaults para que el
     * progreso existente de v0.2.0 quede intacto (I4).
     */
    public val MIGRATION_2_3: Migration =
        object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE `lesson_progress` ADD COLUMN `speakingAttempts` INTEGER NOT NULL DEFAULT 0",
                )
                db.execSQL(
                    "ALTER TABLE `lesson_progress` ADD COLUMN `speakingBestWpm` REAL NOT NULL DEFAULT 0",
                )
                db.execSQL(
                    "ALTER TABLE `lesson_progress` ADD COLUMN `speakingLastScore` REAL NOT NULL DEFAULT 0",
                )
                db.execSQL(
                    "ALTER TABLE `word_progress` ADD COLUMN `errorCount` INTEGER NOT NULL DEFAULT 0",
                )
                db.execSQL(
                    "ALTER TABLE `word_progress` ADD COLUMN `lastErrorType` TEXT",
                )
            }
        }

    /**
     * v3→v4 (ADR-025): crea la tabla singleton `hearts_progress`. Sin seed:
     * la primera lectura publica 10 vidas, y el progreso existente queda
     * intacto (I4).
     */
    public val MIGRATION_3_4: Migration =
        object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `hearts_progress` (" +
                        "`id` INTEGER NOT NULL, " +
                        "`hearts` INTEGER NOT NULL, " +
                        "`lastRegenEpochSecond` INTEGER NOT NULL, " +
                        "PRIMARY KEY(`id`))",
                )
            }
        }

    /**
     * v4→v5 (ADR-050): añade métricas de voz/precisión, la nota cultural de
     * cada tarjeta y `daily_activity`. Las columnas nuevas tienen defaults y
     * ninguna fila existente se borra ni se trunca (I4). La actividad se
     * siembra con el día de la última racha para no perder el histórico.
     */
    public val MIGRATION_4_5: Migration =
        object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE `lesson_progress` ADD COLUMN `speakingPassedAttempts` INTEGER NOT NULL DEFAULT 0",
                )
                db.execSQL(
                    "ALTER TABLE `lesson_progress` ADD COLUMN `bestScorePercent` INTEGER NOT NULL DEFAULT 0",
                )
                db.execSQL(
                    "ALTER TABLE `lesson_progress` ADD COLUMN `lastScorePercent` INTEGER NOT NULL DEFAULT 0",
                )
                db.execSQL(
                    "ALTER TABLE `word_progress` ADD COLUMN `culturalNote` TEXT",
                )
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `daily_activity` (" +
                        "`epochDay` INTEGER NOT NULL, " +
                        "`xpEarned` INTEGER NOT NULL, " +
                        "`lessonsCompleted` INTEGER NOT NULL, " +
                        "`reviewsCompleted` INTEGER NOT NULL, " +
                        "PRIMARY KEY(`epochDay`))",
                )
                db.execSQL(
                    "INSERT OR IGNORE INTO `daily_activity` " +
                        "(`epochDay`, `xpEarned`, `lessonsCompleted`, `reviewsCompleted`) " +
                        "SELECT lastDateEpochDay, totalXp, " +
                        "(SELECT COUNT(*) FROM lesson_progress WHERE completed = 1), " +
                        "(SELECT COALESCE(SUM(reviewCount), 0) FROM word_progress) " +
                        "FROM streak_progress WHERE id = 1 AND current > 0",
                )
            }
        }
}

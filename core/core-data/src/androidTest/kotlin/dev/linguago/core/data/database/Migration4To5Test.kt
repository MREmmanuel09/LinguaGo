package dev.linguago.core.data.database

import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Regresión bloqueante de v5 (ADR-050): una base v4 poblada conserva racha,
 * lecciones, tarjetas y vidas; las columnas nuevas quedan con defaults y la
 * actividad se siembra con el último día de racha.
 */
@RunWith(AndroidJUnit4::class)
public class Migration4To5Test {
    @get:Rule
    public val helper: MigrationTestHelper =
        MigrationTestHelper(
            InstrumentationRegistry.getInstrumentation(),
            LinguagoDatabase::class.java,
        )

    @Test
    public fun migracion_4_5_preserva_progreso_y_crea_actividad() {
        helper.createDatabase(DB_V4, 4).use { db ->
            db.execSQL(
                "INSERT INTO streak_progress (id, current, longest, lastDateEpochDay, totalXp) " +
                    "VALUES (1, 4, 9, 20010, 640)",
            )
            db.execSQL(
                "INSERT INTO lesson_progress " +
                    "(lessonId, completed, xp, attempts, speakingAttempts, speakingBestWpm, speakingLastScore) " +
                    "VALUES ('es-en-lv-1-un-1-ls-1', 1, 30, 1, 2, 85.5, 0.8)",
            )
            db.execSQL(
                "INSERT INTO word_progress " +
                    "(word, courseId, translation, reviewCount, lastReviewEpochMillis, " +
                    "nextReviewEpochMillis, stability, difficulty, errorCount, lastErrorType) " +
                    "VALUES ('hola', 'es-en', 'hello', 3, 1000, 2000, 2.5, 4.0, 2, 'grammar')",
            )
            db.execSQL(
                "INSERT INTO word_progress " +
                    "(word, courseId, translation, reviewCount, lastReviewEpochMillis, " +
                    "nextReviewEpochMillis, stability, difficulty, errorCount, lastErrorType) " +
                    "VALUES ('perro', 'es-en', 'dog', 2, 1100, 2100, 3.5, 3.0, 0, NULL)",
            )
            db.execSQL(
                "INSERT INTO hearts_progress (id, hearts, lastRegenEpochSecond) VALUES (1, 7, 5000)",
            )
        }

        helper.runMigrationsAndValidate(DB_V4, 5, true, DatabaseMigrations.MIGRATION_4_5).use { db ->
            db.query("SELECT current, longest, totalXp FROM streak_progress WHERE id = 1").use { cursor ->
                assertTrue(cursor.moveToFirst())
                assertEquals(4, cursor.getInt(0))
                assertEquals(9, cursor.getInt(1))
                assertEquals(640, cursor.getInt(2))
            }
            db
                .query(
                    "SELECT xp, speakingAttempts, speakingBestWpm, speakingPassedAttempts, " +
                        "bestScorePercent, lastScorePercent FROM lesson_progress " +
                        "WHERE lessonId = 'es-en-lv-1-un-1-ls-1'",
                ).use { cursor ->
                    assertTrue(cursor.moveToFirst())
                    assertEquals(30, cursor.getInt(0))
                    assertEquals(2, cursor.getInt(1))
                    assertEquals(85.5f, cursor.getFloat(2), 0.001f)
                    assertEquals(0, cursor.getInt(3))
                    assertEquals(0, cursor.getInt(4))
                    assertEquals(0, cursor.getInt(5))
                }
            db.query("SELECT COUNT(*) FROM word_progress").use { cursor ->
                assertTrue(cursor.moveToFirst())
                assertEquals(2, cursor.getInt(0))
            }
            db.query("SELECT translation, errorCount, lastErrorType, culturalNote FROM word_progress WHERE word = 'hola'").use { cursor ->
                assertTrue(cursor.moveToFirst())
                assertEquals("hello", cursor.getString(0))
                assertEquals(2, cursor.getInt(1))
                assertEquals("grammar", cursor.getString(2))
                assertNull(cursor.getString(3))
            }
            db.query("SELECT hearts, lastRegenEpochSecond FROM hearts_progress WHERE id = 1").use { cursor ->
                assertTrue(cursor.moveToFirst())
                assertEquals(7, cursor.getInt(0))
                assertEquals(5000L, cursor.getLong(1))
            }
            db
                .query(
                    "SELECT epochDay, xpEarned, lessonsCompleted, reviewsCompleted FROM daily_activity",
                ).use { cursor ->
                    assertTrue(cursor.moveToFirst())
                    assertEquals(20010L, cursor.getLong(0))
                    assertEquals(640, cursor.getInt(1))
                    assertEquals(1, cursor.getInt(2))
                    assertEquals(5, cursor.getInt(3))
                    assertEquals(1, cursor.count)
                }
        }
    }

    private companion object {
        private const val DB_V4 = "migration-v2_5_0-v4.db"
    }
}

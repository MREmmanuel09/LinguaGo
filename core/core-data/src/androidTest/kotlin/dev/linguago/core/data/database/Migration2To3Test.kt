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
 * Regresión bloqueante de progreso (ADR-021, I4): un usuario de v0.2.0 con
 * 8/17 lecciones, 3 tarjetas FSRS en `learning` y racha de 5 días debe
 * conservar su estado íntegro tras la migración 2→3.
 */
@RunWith(AndroidJUnit4::class)
class Migration2To3Test {
    @get:Rule
    val helper: MigrationTestHelper =
        MigrationTestHelper(
            InstrumentationRegistry.getInstrumentation(),
            LinguagoDatabase::class.java,
        )

    @Test
    fun migracion_2_3_preserva_usuario_v020() {
        helper.createDatabase(DB_V2, 2).use { db ->
            db.execSQL(
                "INSERT INTO streak_progress (id, current, longest, lastDateEpochDay, totalXp) " +
                    "VALUES (1, 5, 5, 20000, 750)",
            )
            for (index in 1..8) {
                db.execSQL(
                    "INSERT INTO lesson_progress (lessonId, completed, xp, attempts) " +
                        "VALUES ('es-en-lv-1-un-1-ls-$index', 1, 10, 1)",
                )
            }
            for (index in 1..3) {
                db.execSQL(
                    "INSERT INTO word_progress " +
                        "(word, courseId, translation, reviewCount, lastReviewEpochMillis, " +
                        "nextReviewEpochMillis, stability, difficulty) " +
                        "VALUES ('word-$index', 'es-en', 'palabra-$index', 1, 1000, 2000, " +
                        "${index.toFloat() / 2f}, 5.0)",
                )
            }
        }

        helper.runMigrationsAndValidate(DB_V2, 3, true, DatabaseMigrations.MIGRATION_2_3).use { db ->
            db
                .query(
                    "SELECT current, longest, lastDateEpochDay, totalXp FROM streak_progress WHERE id = 1",
                ).use { cursor ->
                    assertTrue(cursor.moveToFirst())
                    assertEquals(5, cursor.getInt(0))
                    assertEquals(5, cursor.getInt(1))
                    assertEquals(20000L, cursor.getLong(2))
                    assertEquals(750, cursor.getInt(3))
                }

            db.query("SELECT COUNT(*) FROM lesson_progress WHERE completed = 1").use { cursor ->
                assertTrue(cursor.moveToFirst())
                assertEquals(8, cursor.getInt(0))
            }

            db.query("SELECT COUNT(*) FROM word_progress WHERE reviewCount = 1 AND stability < 2.5").use { cursor ->
                assertTrue(cursor.moveToFirst())
                assertEquals(3, cursor.getInt(0))
            }

            db
                .query(
                    "SELECT speakingAttempts, speakingBestWpm, speakingLastScore " +
                        "FROM lesson_progress ORDER BY lessonId LIMIT 1",
                ).use { cursor ->
                    assertTrue(cursor.moveToFirst())
                    assertEquals(0, cursor.getInt(0))
                    assertEquals(0f, cursor.getFloat(1), 0.0f)
                    assertEquals(0f, cursor.getFloat(2), 0.0f)
                }

            db.query("SELECT errorCount, lastErrorType FROM word_progress ORDER BY word LIMIT 1").use { cursor ->
                assertTrue(cursor.moveToFirst())
                assertEquals(0, cursor.getInt(0))
                assertTrue(cursor.isNull(1))
                assertNull(cursor.getString(1))
            }
        }
    }

    @Test
    fun migracion_1_2_3_conserva_progreso_y_crea_columnas() {
        helper.createDatabase(DB_V1, 1).use { db ->
            db.execSQL(
                "INSERT INTO streak_progress (id, current, longest, lastDateEpochDay, totalXp) " +
                    "VALUES (1, 2, 2, 19000, 40)",
            )
            for (index in 1..2) {
                db.execSQL(
                    "INSERT INTO lesson_progress (lessonId, completed, xp, attempts) " +
                        "VALUES ('es-en-lv-1-un-1-ls-$index', 1, 20, 2)",
                )
            }
        }

        helper
            .runMigrationsAndValidate(
                DB_V1,
                3,
                true,
                DatabaseMigrations.MIGRATION_1_2,
                DatabaseMigrations.MIGRATION_2_3,
            ).use { db ->
                db.query("SELECT current, totalXp FROM streak_progress WHERE id = 1").use { cursor ->
                    assertTrue(cursor.moveToFirst())
                    assertEquals(2, cursor.getInt(0))
                    assertEquals(40, cursor.getInt(1))
                }
                db.query("SELECT COUNT(*) FROM lesson_progress WHERE completed = 1").use { cursor ->
                    assertTrue(cursor.moveToFirst())
                    assertEquals(2, cursor.getInt(0))
                }
                db.query("SELECT COUNT(*) FROM word_progress").use { cursor ->
                    assertTrue(cursor.moveToFirst())
                    assertEquals(0, cursor.getInt(0))
                }
                db.query("SELECT speakingAttempts FROM lesson_progress LIMIT 1").use { cursor ->
                    assertTrue(cursor.moveToFirst())
                    assertEquals(0, cursor.getInt(0))
                }
                val wordColumns = mutableSetOf<String>()
                db.query("PRAGMA table_info(word_progress)").use { cursor ->
                    val nameIndex = cursor.getColumnIndexOrThrow("name")
                    while (cursor.moveToNext()) {
                        wordColumns += cursor.getString(nameIndex)
                    }
                }
                assertTrue(wordColumns.containsAll(listOf("errorCount", "lastErrorType")))
            }
    }

    private companion object {
        private const val DB_V2 = "migration-v0_2_0.db"
        private const val DB_V1 = "migration-v0_1_0.db"
    }
}

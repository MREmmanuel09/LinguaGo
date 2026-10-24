package dev.linguago.core.data.database

import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Regresión de v4 (ADR-025): la migración 3→4 crea `hearts_progress` y
 * conserva intactos la racha y el progreso de lecciones (I4).
 */
@RunWith(AndroidJUnit4::class)
public class Migration3To4Test {
    @get:Rule
    public val helper: MigrationTestHelper =
        MigrationTestHelper(
            InstrumentationRegistry.getInstrumentation(),
            LinguagoDatabase::class.java,
        )

    @Test
    public fun migracion_3_4_preserva_progreso_y_crea_vidas() {
        helper.createDatabase(DB_V3, 3).use { db ->
            db.execSQL(
                "INSERT INTO streak_progress (id, current, longest, lastDateEpochDay, totalXp) " +
                    "VALUES (1, 4, 9, 20010, 640)",
            )
            db.execSQL(
                "INSERT INTO lesson_progress (lessonId, completed, xp, attempts) " +
                    "VALUES ('es-en-lv-1-un-1-ls-1', 1, 30, 1)",
            )
        }

        helper.runMigrationsAndValidate(DB_V3, 4, true, DatabaseMigrations.MIGRATION_3_4).use { db ->
            db.query("SELECT current, longest, totalXp FROM streak_progress WHERE id = 1").use { cursor ->
                assertTrue(cursor.moveToFirst())
                assertEquals(4, cursor.getInt(0))
                assertEquals(9, cursor.getInt(1))
                assertEquals(640, cursor.getInt(2))
            }
            db.query("SELECT COUNT(*) FROM lesson_progress WHERE completed = 1").use { cursor ->
                assertTrue(cursor.moveToFirst())
                assertEquals(1, cursor.getInt(0))
            }
            db.query("SELECT COUNT(*) FROM hearts_progress").use { cursor ->
                assertTrue(cursor.moveToFirst())
                assertEquals(0, cursor.getInt(0))
            }
            val columns = mutableSetOf<String>()
            db.query("PRAGMA table_info(hearts_progress)").use { cursor ->
                val nameIndex = cursor.getColumnIndexOrThrow("name")
                while (cursor.moveToNext()) {
                    columns += cursor.getString(nameIndex)
                }
            }
            assertTrue(columns.containsAll(listOf("id", "hearts", "lastRegenEpochSecond")))
        }
    }

    private companion object {
        private const val DB_V3 = "migration-v0_2_0-v3.db"
    }
}

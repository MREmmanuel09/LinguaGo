package dev.linguago.core.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * DAO de la racha y del XP total. Todas las escrituras son REPLACE sobre el
 * registro singleton: el progreso solo se acumula, nunca se resta (I4).
 */
@Dao
public interface StreakProgressDao {
    @Query("SELECT * FROM streak_progress WHERE id = 1")
    public suspend fun load(): StreakProgressEntity?

    @Query("SELECT * FROM streak_progress WHERE id = 1")
    public fun observe(): Flow<StreakProgressEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public suspend fun upsert(entity: StreakProgressEntity)
}

/** DAO del progreso por lección. */
@Dao
public interface LessonProgressDao {
    @Query("SELECT * FROM lesson_progress ORDER BY lessonId ASC")
    public fun observeAll(): Flow<List<LessonProgressEntity>>

    /** Lectura puntual de todas las filas (snapshots de sync, ADR-013). */
    @Query("SELECT * FROM lesson_progress ORDER BY lessonId ASC")
    public suspend fun snapshotAll(): List<LessonProgressEntity>

    @Query("SELECT * FROM lesson_progress WHERE lessonId = :lessonId")
    public suspend fun load(lessonId: String): LessonProgressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public suspend fun upsert(entity: LessonProgressEntity)
}

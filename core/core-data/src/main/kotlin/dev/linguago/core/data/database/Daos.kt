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

/** DAO del repaso espaciado (WordProgress FSRS, ADR-016). */
@Dao
public interface WordProgressDao {
    @Query("SELECT * FROM word_progress WHERE courseId = :courseId")
    public fun observeAll(courseId: String): Flow<List<WordProgressEntity>>

    @Query("SELECT * FROM word_progress WHERE courseId = :courseId ORDER BY nextReviewEpochMillis ASC")
    public suspend fun loadAll(courseId: String): List<WordProgressEntity>

    @Query("SELECT * FROM word_progress WHERE courseId = :courseId AND word = :word")
    public suspend fun load(
        courseId: String,
        word: String,
    ): WordProgressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public suspend fun upsert(entity: WordProgressEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    public suspend fun insertAll(entities: List<WordProgressEntity>)
}

package dev.linguago.core.data.database

import androidx.room.ColumnInfo
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
    /** Intentos de producción oral libre registrados en la lección. */
    @ColumnInfo(defaultValue = "0")
    public val speakingAttempts: Int = 0,
    /** Mejor fluidez observada en la lección (palabras por minuto). */
    @ColumnInfo(defaultValue = "0")
    public val speakingBestWpm: Float = 0f,
    /** Última puntuación heurística de producción oral (0..1). */
    @ColumnInfo(defaultValue = "0")
    public val speakingLastScore: Float = 0f,
)

/**
 * Vidas del alumno (ADR-025). Registro singleton: máximo 10, +1 cada 60 s.
 * [lastRegenEpochSecond] ancla la regeneración por tiempo, de modo que el
 * paso del tiempo con la app cerrada también recupera vidas.
 */
@Entity(tableName = "hearts_progress")
public data class HeartsProgressEntity(
    @PrimaryKey public val id: Long,
    public val hearts: Int,
    public val lastRegenEpochSecond: Long,
)

/**
 * Estado FSRS de una tarjeta de vocabulario (ADR-016). Clave compuesta
 * (word, courseId): la misma palabra puede existir en cursos distintos.
 * [errorCount] y [lastErrorType] alimentan el feedback de errores frecuentes
 * del curso (ADR-021).
 */
@Entity(
    tableName = "word_progress",
    primaryKeys = ["word", "courseId"],
)
public data class WordProgressEntity(
    public val word: String,
    public val courseId: String,
    public val translation: String,
    public val reviewCount: Int,
    public val lastReviewEpochMillis: Long,
    public val nextReviewEpochMillis: Long,
    public val stability: Float,
    public val difficulty: Float,
    /** Veces que el usuario falló esta tarjeta. */
    @ColumnInfo(defaultValue = "0")
    public val errorCount: Int = 0,
    /** Última categoría de error registrada (grammar, pronunciation...). */
    public val lastErrorType: String? = null,
)

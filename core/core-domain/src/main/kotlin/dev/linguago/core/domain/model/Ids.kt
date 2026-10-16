package dev.linguago.core.domain.model

/**
 * Identificadores estables de las entidades del contenido. Son value classes
 * para imposibilitar mezclas de IDs accidentalmente (I7).
 */
@JvmInline
public value class CourseId(
    public val value: String,
)

@JvmInline
public value class LevelId(
    public val value: String,
)

@JvmInline
public value class UnitId(
    public val value: String,
)

@JvmInline
public value class LessonId(
    public val value: String,
)

@JvmInline
public value class ExerciseId(
    public val value: String,
)

package dev.linguago.core.domain.model

import kotlinx.serialization.Serializable

/**
 * Identificadores estables de las entidades del contenido. Son value classes
 * para imposibilitar mezclas de IDs accidentalmente (I7). Se serializan
 * de forma transparente como la cadena subyacente (ADR-009).
 */
@JvmInline
@Serializable
public value class CourseId(
    public val value: String,
)

@JvmInline
@Serializable
public value class LevelId(
    public val value: String,
)

@JvmInline
@Serializable
public value class UnitId(
    public val value: String,
)

@JvmInline
@Serializable
public value class LessonId(
    public val value: String,
)

@JvmInline
@Serializable
public value class ExerciseId(
    public val value: String,
)

@JvmInline
@Serializable
public value class GrammarPointId(
    public val value: String,
)

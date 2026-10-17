package dev.linguago.core.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Jerarquía inmutable del contenido pedagógico (RFC §3.1). Un Curso tiene
 * Niveles, cada Nivel Unidades, cada Unidad Lecciones, cada Lección
 * Ejercicios. La inmutabilidad hace el curso compartible entre pantallas
 * sin riesgos de mutación. Se serializa directamente desde el JSON de los
 * assets (ADR-009): JERARQUÍA JSON = JERARQUÍA DE DOMINIO.
 */
@Serializable
public data class Course(
    val id: CourseId,
    @SerialName("from") public val fromLanguage: Language,
    @SerialName("to") public val toLanguage: Language,
    val levels: List<Level>,
    /** Versión del formato de contenido (ADR-012); v1 por defecto. */
    val schemaVersion: Int = 1,
)

@Serializable
public data class Level(
    val id: LevelId,
    val name: String,
    val units: List<Unit>,
)

@Serializable
public data class Unit(
    val id: UnitId,
    val title: String,
    val lessons: List<Lesson>,
)

@Serializable
public data class Lesson(
    val id: LessonId,
    val title: String,
    val exercises: List<Exercise>,
)

package dev.linguago.core.domain.model

/**
 * Jerarquía inmutable del contenido pedagógico (RFC §3.1). Un Curso tiene
 * Niveles, cada Nivel Unidades, cada Unidad Lecciones, cada Lección
 * Ejercicios. La inmutabilidad hace el curso compartible entre pantallas
 * sin riesgos de mutación.
 */
public data class Course(
    val id: CourseId,
    val fromLanguage: Language,
    val toLanguage: Language,
    val levels: List<Level>,
)

public data class Level(
    val id: LevelId,
    val name: String,
    val units: List<Unit>,
)

public data class Unit(
    val id: UnitId,
    val title: String,
    val lessons: List<Lesson>,
)

public data class Lesson(
    val id: LessonId,
    val title: String,
    val exercises: List<Exercise>,
)

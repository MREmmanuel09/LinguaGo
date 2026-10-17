package dev.linguago.core.domain.model

/**
 * Lección abierta con contexto de curso (RFC §3.2): incluye el idioma de
 * destino para audio (ADR-003) y el título de lección para la cabecera.
 */
public data class LessonSession(
    val course: Course,
    val lesson: Lesson,
)

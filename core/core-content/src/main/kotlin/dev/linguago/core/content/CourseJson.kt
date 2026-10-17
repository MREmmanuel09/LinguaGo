package dev.linguago.core.content

import dev.linguago.core.domain.model.Course
import dev.linguago.core.domain.model.CourseSummary
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

/**
 * Decodificación JSON de los assets de curso (ADR-009: el árbol de dominio
 * ES el contrato del JSON, no hay DTOs intermedios). La decodificación
 * fallida se comunica como `null` (sin excepciones cruzando capas, I7).
 */
public object CourseJson {
    /** Configuración estricta: el contenido malformado debe fallar ruidoso. */
    public val json: Json = Json

    public fun parseCourse(jsonText: String): Course? =
        try {
            json.decodeFromString<Course>(jsonText)
        } catch (e: SerializationException) {
            null
        } catch (e: IllegalArgumentException) {
            null
        }

    public fun parseManifest(jsonText: String): CourseSummary? =
        try {
            json.decodeFromString<CourseSummary>(jsonText)
        } catch (e: SerializationException) {
            null
        } catch (e: IllegalArgumentException) {
            null
        }
}

package dev.linguago.core.content

/**
 * Contratos del pipeline de contenido (RFC §3.1, ADR-009 pendiente).
 * El parser JSON y el validador llegan en la Fase 1; aquí queda la interfaz
 * del cargador sobre la que se construye el repositorio de cursos.
 */
interface CourseAssetLoader {
    /**
     * Carga el curso ubicado en `assets/courses/<courseId>/manifest.json`.
     */
    suspend fun loadManifest(courseId: String): Result<Unit>
}

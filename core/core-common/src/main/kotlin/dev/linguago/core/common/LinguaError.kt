package dev.linguago.core.common

/**
 * Error transversal del sistema (invariante I7: testabilidad — sustituye
 * excepciones cruzadas entre capas por un valor explícito).
 */
public sealed interface LinguaError {
    /** Error no clasificado (última línea de defensa). */
    public data object Unknown : LinguaError

    /** Recurso solicitado que no existe (curso/lección/entidad inexistente). */
    public data object NotFound : LinguaError

    /** Contenido que no supera la validación estructural (core-content). */
    public data object InvalidContent : LinguaError

    /** Respaldo/sync solicitado sin una cuenta cloud vinculada (Fase 4). */
    public data object NotLinked : LinguaError

    /** Integración (OAuth/Drive) falta de configuración (client id, red). */
    public data object NotConfigured : LinguaError

    /** Fallas de red del proveedor cloud (drive/oauth endpoints, Fase 4). */
    public data object Network : LinguaError
}

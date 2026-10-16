package dev.linguago.core.common

/**
 * Error transversal del sistema (invariante I7: testabilidad — sustituye
 * excepciones cruzadas entre capas por un valor explícito).
 */
public sealed interface LinguaError {
    /** Error no clasificado (última línea de defensa). */
    public data object Unknown : LinguaError
}

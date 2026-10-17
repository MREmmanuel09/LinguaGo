package dev.linguago.core.auth

/**
 * Lectura de la sesión OAuth para los motores que necesitan credenciales
 * (I7: abstracción para sustituir por fakes en tests unitarios).
 */
public interface TokenReader {
    /** Sesión persistida actual (o `null` si no hay cuenta vinculada). */
    public suspend fun tokens(): StoredTokens?

    /** Consulta sincrónica sin I/O (para `CloudProvider.isLinked()`). */
    public fun hasTokens(): Boolean
}

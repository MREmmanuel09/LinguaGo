package dev.linguago.core.domain.placement

/**
 * Vidas locales del examen de nivelación (ADR-058): cinco corazones por
 * intento, sin regeneración. Cada respuesta incorrecta consume una vida y, al
 * llegar a cero, el examen se cierra con lo respondido hasta ese momento.
 */
public object PlacementLives {
    /** Corazones disponibles en cada intento del examen. */
    public const val MAX: Int = 5

    /** Devuelve las vidas restantes tras un fallo, sin bajar de cero. */
    public fun afterMistake(current: Int): Int = (current - 1).coerceAtLeast(0)

    /** Indica si el intento debe cerrarse por falta de vidas. */
    public fun isExhausted(current: Int): Boolean = current <= 0
}

package dev.linguago.core.domain.hearts

/**
 * Estado de vidas del alumno (ADR-025). La penalización es NO bloqueante: se
 * recupera 1 vida cada [HEARTS_REGEN_SECONDS] hasta [HEARTS_MAX] y el acceso a
 * las lecciones nunca se cierra de forma permanente.
 */
public data class HeartsState(
    public val hearts: Int,
    public val max: Int = HEARTS_MAX,
    /** Epoch en segundos del último consumo o del último tramo de regeneración. */
    public val lastRegenEpochSecond: Long,
)

/** Vidas máximas del alumno (ADR-025). */
public const val HEARTS_MAX: Int = 10

/** Segundos para recuperar una vida (ADR-025). */
public const val HEARTS_REGEN_SECONDS: Long = 60L

/**
 * Reglas puras de vidas (ADR-025): sin Android, deterministas y testeables en
 * JVM. La regeneración se calcula por diferencia de epoch, así que el paso del
 * tiempo con la app cerrada también cuenta.
 */
public object HeartsRules {
    /** Estado inicial con el tanque lleno. */
    public fun initial(nowEpochSecond: Long): HeartsState =
        HeartsState(hearts = HEARTS_MAX, max = HEARTS_MAX, lastRegenEpochSecond = nowEpochSecond)

    /** Aplica la regeneración por tiempo transcurrido (idempotente). */
    public fun regenerated(
        state: HeartsState,
        nowEpochSecond: Long,
    ): HeartsState {
        if (state.hearts >= state.max) return state
        val elapsed = (nowEpochSecond - state.lastRegenEpochSecond).coerceAtLeast(0L)
        val gained = (elapsed / HEARTS_REGEN_SECONDS).toInt()
        if (gained <= 0) return state
        val hearts = (state.hearts + gained).coerceAtMost(state.max)
        val lastRegen =
            if (hearts >= state.max) {
                nowEpochSecond
            } else {
                state.lastRegenEpochSecond + gained * HEARTS_REGEN_SECONDS
            }
        return state.copy(hearts = hearts, lastRegenEpochSecond = lastRegen)
    }

    /**
     * Consume una vida; nunca baja de 0. Si el tanque estaba lleno, el reloj de
     * regeneración arranca en este instante (evita una vida "gratis" al perder
     * la primera tras mucho tiempo sin jugar).
     */
    public fun consume(
        state: HeartsState,
        nowEpochSecond: Long,
    ): HeartsState {
        val current = regenerated(state, nowEpochSecond)
        val wasFull = current.hearts >= current.max
        return current.copy(
            hearts = (current.hearts - 1).coerceAtLeast(0),
            lastRegenEpochSecond = if (wasFull) nowEpochSecond else current.lastRegenEpochSecond,
        )
    }

    /** Rellena el tanque (seed de migración o recompensa explícita). */
    public fun refill(
        state: HeartsState,
        nowEpochSecond: Long,
    ): HeartsState = state.copy(hearts = state.max, lastRegenEpochSecond = nowEpochSecond)

    /** Segundos restantes para la próxima vida; 0 si el tanque está lleno. */
    public fun secondsUntilNextHeart(
        state: HeartsState,
        nowEpochSecond: Long,
    ): Long {
        if (state.hearts >= state.max) return 0L
        val current = regenerated(state, nowEpochSecond)
        if (current.hearts >= current.max) return 0L
        val elapsed = (nowEpochSecond - current.lastRegenEpochSecond).coerceAtLeast(0L)
        return (HEARTS_REGEN_SECONDS - elapsed % HEARTS_REGEN_SECONDS)
            .coerceIn(1L, HEARTS_REGEN_SECONDS)
    }
}

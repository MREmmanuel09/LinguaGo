package dev.linguago.core.domain.repository

import dev.linguago.core.common.Result
import dev.linguago.core.domain.hearts.HeartsState
import kotlinx.coroutines.flow.Flow

/**
 * Contrato de vidas (ADR-025): observable desde el HUD y consumible desde la
 * lección o el repaso. No forma parte del progreso "intocable" de I4; la
 * regeneración por tiempo impide que una penalización bloquee el aprendizaje.
 */
public interface HeartsRepository {
    /** Observa el estado efectivo de vidas (regeneración aplicada al emitir). */
    public fun observe(): Flow<HeartsState>

    /** Fuerza la regeneración por tiempo y devuelve el estado persistido. */
    public suspend fun current(): Result<HeartsState>

    /** Consume una vida (respuesta incorrecta u "Otra vez" en repaso). */
    public suspend fun consume(): Result<HeartsState>

    /** Rellena el tanque (seed de migración o recompensa explícita). */
    public suspend fun refill(): Result<HeartsState>
}

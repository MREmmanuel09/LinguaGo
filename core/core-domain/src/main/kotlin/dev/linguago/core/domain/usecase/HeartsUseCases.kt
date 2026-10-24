package dev.linguago.core.domain.usecase

import dev.linguago.core.common.Result
import dev.linguago.core.domain.hearts.HeartsState
import kotlinx.coroutines.flow.Flow

/** Observa las vidas para el HUD del camino y de la lección (ADR-025). */
public interface ObserveHeartsUseCase {
    public operator fun invoke(): Flow<HeartsState>
}

/** Consume una vida por una respuesta incorrecta o un "Otra vez" (ADR-025). */
public interface ConsumeHeartUseCase {
    public suspend operator fun invoke(): Result<HeartsState>
}

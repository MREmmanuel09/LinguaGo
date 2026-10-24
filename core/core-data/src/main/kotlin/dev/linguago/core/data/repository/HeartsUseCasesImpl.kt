package dev.linguago.core.data.repository

import dev.linguago.core.common.Result
import dev.linguago.core.domain.hearts.HeartsState
import dev.linguago.core.domain.repository.HeartsRepository
import dev.linguago.core.domain.usecase.ConsumeHeartUseCase
import dev.linguago.core.domain.usecase.ObserveHeartsUseCase
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/** Implementación del HUD de vidas (ADR-025). */
@Singleton
public class ObserveHeartsUseCaseImpl
    @Inject
    constructor(
        private val repository: HeartsRepository,
    ) : ObserveHeartsUseCase {
        override fun invoke(): Flow<HeartsState> = repository.observe()
    }

/** Implementación del consumo de una vida (ADR-025). */
@Singleton
public class ConsumeHeartUseCaseImpl
    @Inject
    constructor(
        private val repository: HeartsRepository,
    ) : ConsumeHeartUseCase {
        override suspend fun invoke(): Result<HeartsState> = repository.consume()
    }

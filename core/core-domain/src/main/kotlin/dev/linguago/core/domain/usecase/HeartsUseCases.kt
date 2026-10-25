package dev.linguago.core.domain.usecase

import dev.linguago.core.common.Result
import dev.linguago.core.domain.hearts.HeartsState
import dev.linguago.core.domain.logic.GradingResult
import dev.linguago.core.domain.model.Exercise
import dev.linguago.core.domain.model.GrammarPoint
import kotlinx.coroutines.flow.Flow

/** Observa las vidas para el HUD del camino y de la lección (ADR-025). */
public interface ObserveHeartsUseCase {
    public operator fun invoke(): Flow<HeartsState>
}

/** Consume una vida por una respuesta incorrecta o un "Otra vez" (ADR-025). */
public interface ConsumeHeartUseCase {
    public suspend operator fun invoke(): Result<HeartsState>
}

/** Resultado de registrar una respuesta (ADR-025): calificación + vidas. */
public data class AnswerOutcome(
    public val grading: GradingResult,
    /** Vidas resultantes; `null` si el HUD no pudo actualizarse (no bloquea). */
    public val hearts: HeartsState?,
    /** `true` cuando la respuesta agotó las vidas y la lección se cancela. */
    public val lessonCancelled: Boolean,
)

/**
 * Califica la respuesta y aplica el coste de vidas (ADR-025).
 * [consumeHeart] se desactiva en los SKIP de voz, que nunca penalizan (I3).
 */
public interface RegisterAnswerUseCase {
    public suspend operator fun invoke(
        exercise: Exercise,
        answer: ExerciseAnswer,
        grammarPoints: List<GrammarPoint> = emptyList(),
        consumeHeart: Boolean = true,
    ): Result<AnswerOutcome>
}

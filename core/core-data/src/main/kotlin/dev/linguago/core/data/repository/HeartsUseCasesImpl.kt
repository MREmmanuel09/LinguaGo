package dev.linguago.core.data.repository

import dev.linguago.core.common.Result
import dev.linguago.core.common.getOrNull
import dev.linguago.core.domain.hearts.HeartsState
import dev.linguago.core.domain.model.Exercise
import dev.linguago.core.domain.model.GrammarPoint
import dev.linguago.core.domain.repository.HeartsRepository
import dev.linguago.core.domain.usecase.AnswerOutcome
import dev.linguago.core.domain.usecase.ConsumeHeartUseCase
import dev.linguago.core.domain.usecase.ExerciseAnswer
import dev.linguago.core.domain.usecase.ObserveHeartsUseCase
import dev.linguago.core.domain.usecase.RegisterAnswerUseCase
import dev.linguago.core.domain.usecase.SubmitAnswerUseCase
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

/**
 * Califica y aplica el coste de vidas (ADR-025). Un fallo de persistencia de
 * vidas nunca bloquea la respuesta: la calificación viaja igual y el HUD
 * mantiene su último valor.
 */
@Singleton
public class RegisterAnswerUseCaseImpl
    @Inject
    constructor(
        private val submitAnswerUseCase: SubmitAnswerUseCase,
        private val repository: HeartsRepository,
    ) : RegisterAnswerUseCase {
        override suspend fun invoke(
            exercise: Exercise,
            answer: ExerciseAnswer,
            grammarPoints: List<GrammarPoint>,
            consumeHeart: Boolean,
        ): Result<AnswerOutcome> =
            when (val graded = submitAnswerUseCase(exercise, answer, grammarPoints)) {
                is Result.Error -> Result.error(graded.error)
                is Result.Success -> {
                    val grading = graded.value
                    if (grading.isCorrect || !consumeHeart) {
                        Result.success(
                            AnswerOutcome(
                                grading = grading,
                                hearts = repository.current().getOrNull(),
                                lessonCancelled = false,
                            ),
                        )
                    } else {
                        when (val consumed = repository.consume()) {
                            is Result.Error ->
                                Result.success(
                                    AnswerOutcome(grading = grading, hearts = null, lessonCancelled = false),
                                )
                            is Result.Success ->
                                Result.success(
                                    AnswerOutcome(
                                        grading = grading,
                                        hearts = consumed.value,
                                        lessonCancelled = consumed.value.hearts == 0,
                                    ),
                                )
                        }
                    }
                }
            }
    }

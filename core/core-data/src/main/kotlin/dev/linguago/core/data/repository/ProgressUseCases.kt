package dev.linguago.core.data.repository

import dev.linguago.core.common.Result
import dev.linguago.core.common.flatMap
import dev.linguago.core.common.map
import dev.linguago.core.domain.logic.ExerciseGrader
import dev.linguago.core.domain.logic.GradingResult
import dev.linguago.core.domain.model.Exercise
import dev.linguago.core.domain.model.GrammarPoint
import dev.linguago.core.domain.model.LessonId
import dev.linguago.core.domain.model.StreakState
import dev.linguago.core.domain.repository.ProgressRepository
import dev.linguago.core.domain.usecase.CompleteLessonUseCase
import dev.linguago.core.domain.usecase.ExerciseAnswer
import dev.linguago.core.domain.usecase.SubmitAnswerUseCase
import dev.linguago.core.domain.usecase.UpdateStreakUseCase
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Casos de uso de progreso concretados (RFC §3.2, Fase 1, ADR-025): cada
 * respuesta se califica sin tocar el XP persistido; el XP de la sesión se
 * persiste UNA sola vez al completar la lección, junto con la racha. La
 * decisión acertada/fallida vive en el dominio (ExerciseGrader); aquí solo se
 * orquesta la calificación o el efecto sobre el repositorio.
 */
@Singleton
public class SubmitAnswerUseCaseImpl
    @Inject
    constructor() : SubmitAnswerUseCase {
        override suspend operator fun invoke(
            exercise: Exercise,
            answer: ExerciseAnswer,
            grammarPoints: List<GrammarPoint>,
        ): Result<GradingResult> = Result.success(ExerciseGrader.grade(exercise, answer, grammarPoints))
    }

/**
 * Persiste la finalización de la lección (ADR-025): progreso, XP de la sesión
 * y actividad diaria. Es el ÚNICO punto donde se suma XP al total; si la
 * lección se cancela por quedarse sin vidas, este caso de uso no se llama y
 * el XP en memoria se descarta.
 */
@Singleton
public class CompleteLessonUseCaseImpl
    @Inject
    constructor(
        private val progressRepository: ProgressRepository,
    ) : CompleteLessonUseCase {
        override suspend operator fun invoke(
            lessonId: LessonId,
            xp: Int,
        ): Result<Unit> =
            progressRepository
                .markLessonCompleted(lessonId, xp)
                .flatMap { progressRepository.addXp(xp) }
                .flatMap { progressRepository.applyDailyActivity(LocalDate.now()) }
                .map { Unit }
    }

@Singleton
public class UpdateStreakUseCaseImpl
    @Inject
    constructor(
        private val progressRepository: ProgressRepository,
    ) : UpdateStreakUseCase {
        override suspend operator fun invoke(): Result<StreakState> = progressRepository.applyDailyActivity(LocalDate.now())
    }

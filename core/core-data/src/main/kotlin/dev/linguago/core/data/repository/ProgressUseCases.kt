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
import dev.linguago.core.domain.model.XP_PER_CORRECT_ANSWER
import dev.linguago.core.domain.repository.ProgressRepository
import dev.linguago.core.domain.usecase.CompleteLessonUseCase
import dev.linguago.core.domain.usecase.ExerciseAnswer
import dev.linguago.core.domain.usecase.SubmitAnswerUseCase
import dev.linguago.core.domain.usecase.UpdateStreakUseCase
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Casos de uso de progreso concretados (RFC §3.2, Fase 1): cada respuesta
 * correcta suma XP (+10) y completar una lección marca progreso y racha del
 * día. La decisión acertada/fallida vive en el dominio (ExerciseGrader);
 * aquí solo se orquesta el efecto sobre el repositorio.
 */
@Singleton
public class SubmitAnswerUseCaseImpl
    @Inject
    constructor(
        private val progressRepository: ProgressRepository,
    ) : SubmitAnswerUseCase {
        override suspend operator fun invoke(
            exercise: Exercise,
            answer: ExerciseAnswer,
            grammarPoints: List<GrammarPoint>,
        ): Result<GradingResult> {
            val grading = ExerciseGrader.grade(exercise, answer, grammarPoints)
            if (!grading.isCorrect) return Result.success(grading)
            return when (val xpResult = progressRepository.addXp(XP_PER_CORRECT_ANSWER)) {
                is Result.Success -> Result.success(grading)
                is Result.Error -> Result.error(xpResult.error)
            }
        }
    }

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

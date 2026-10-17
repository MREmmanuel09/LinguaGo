package dev.linguago.core.data.repository

import dev.linguago.core.common.Result
import dev.linguago.core.domain.logic.ReviewRating
import dev.linguago.core.domain.model.CourseId
import dev.linguago.core.domain.model.WordProgress
import dev.linguago.core.domain.repository.ReviewRepository
import dev.linguago.core.domain.usecase.GetNextReviewUseCase
import dev.linguago.core.domain.usecase.GradeReviewUseCase
import javax.inject.Inject
import javax.inject.Singleton

/** Casos de uso del repaso espaciado (ADR-016, Fase 5). */
@Singleton
public class GetNextReviewUseCaseImpl
    @Inject
    constructor(
        private val reviewRepository: ReviewRepository,
    ) : GetNextReviewUseCase {
        override suspend operator fun invoke(
            courseId: CourseId,
            limit: Int,
        ): Result<List<WordProgress>> = reviewRepository.due(courseId, limit)
    }

@Singleton
public class GradeReviewUseCaseImpl
    @Inject
    constructor(
        private val reviewRepository: ReviewRepository,
    ) : GradeReviewUseCase {
        override suspend operator fun invoke(
            courseId: CourseId,
            word: String,
            rating: ReviewRating,
        ): Result<WordProgress> = reviewRepository.grade(courseId, word, rating)
    }

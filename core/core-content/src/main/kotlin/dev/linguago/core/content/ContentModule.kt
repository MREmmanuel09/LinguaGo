package dev.linguago.core.content

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.linguago.core.domain.repository.CourseRepository
import dev.linguago.core.domain.usecase.GetAvailableCoursesUseCase
import dev.linguago.core.domain.usecase.LoadCourseUseCase
import dev.linguago.core.domain.usecase.StartLessonUseCase

/**
 * Módulo de Hilt de content: publica las implementaciones solo dentro de
 * sus interfaces de dominio. Los use cases de progreso (Submit/Complete/
 * UpdateStreak) los publica core-data en su propio módulo (ADR-005).
 */
@Module
@InstallIn(SingletonComponent::class)
public abstract class ContentModule {
    @Binds
    public abstract fun bindsCourseRepository(impl: CourseRepositoryImpl): CourseRepository

    @Binds
    public abstract fun bindsLoadCourseUseCase(impl: LoadCourseUseCaseImpl): LoadCourseUseCase

    @Binds
    public abstract fun bindsGetAvailableCoursesUseCase(impl: GetAvailableCoursesUseCaseImpl): GetAvailableCoursesUseCase

    @Binds
    public abstract fun bindsStartLessonUseCase(impl: StartLessonUseCaseImpl): StartLessonUseCase
}

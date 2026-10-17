package dev.linguago.core.content

import dev.linguago.core.common.LinguaError
import dev.linguago.core.common.Result
import dev.linguago.core.common.flatMap
import dev.linguago.core.common.map
import dev.linguago.core.domain.model.Course
import dev.linguago.core.domain.model.CourseId
import dev.linguago.core.domain.model.CourseSummary
import dev.linguago.core.domain.model.Lesson
import dev.linguago.core.domain.model.LessonId
import dev.linguago.core.domain.repository.CourseRepository
import dev.linguago.core.domain.usecase.GetAvailableCoursesUseCase
import dev.linguago.core.domain.usecase.LoadCourseUseCase
import dev.linguago.core.domain.usecase.StartLessonUseCase
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Contratos de dominio concretados sobre los assets (RFC §3.1-3.2). Las
 * respuestas táctiles del plan (validación exhaustiva, fails rápidos del
 * contenido) ocurren una sola vez dentro de [CourseLoader]; las capas
 * superiores consumen resultados puros.
 */
@Singleton
public class CourseRepositoryImpl
    @Inject
    constructor(
        private val loader: CourseLoader,
    ) : CourseRepository {
        override suspend fun loadCourse(courseId: CourseId): Result<Course> = loader.loadCourse(courseId)
    }

@Singleton
public class LoadCourseUseCaseImpl
    @Inject
    constructor(
        private val loader: CourseLoader,
    ) : LoadCourseUseCase {
        override suspend operator fun invoke(courseId: CourseId): Result<Course> = loader.loadCourse(courseId)
    }

@Singleton
public class GetAvailableCoursesUseCaseImpl
    @Inject
    constructor(
        private val loader: CourseLoader,
    ) : GetAvailableCoursesUseCase {
        override suspend operator fun invoke(): Result<List<CourseSummary>> =
            loader.listManifests().map { manifests -> manifests.values.toList() }
    }

@Singleton
public class StartLessonUseCaseImpl
    @Inject
    constructor(
        private val loader: CourseLoader,
    ) : StartLessonUseCase {
        override suspend operator fun invoke(
            courseId: CourseId,
            lessonId: LessonId,
        ): Result<Lesson> =
            loader
                .loadCourse(courseId)
                .flatMap { course -> findLesson(course, lessonId) }

        private fun findLesson(
            course: Course,
            lessonId: LessonId,
        ): Result<Lesson> =
            course.levels
                .flatMap { it.units }
                .flatMap { it.lessons }
                .firstOrNull { it.id == lessonId }
                ?.let { lesson -> Result.success(lesson) }
                ?: Result.error(LinguaError.NotFound)
    }

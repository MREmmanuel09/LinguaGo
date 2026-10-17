package dev.linguago.core.content

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.linguago.core.common.DispatcherProvider
import dev.linguago.core.common.LinguaError
import dev.linguago.core.common.Result
import dev.linguago.core.domain.model.Course
import dev.linguago.core.domain.model.CourseId
import dev.linguago.core.domain.model.CourseSummary
import kotlinx.coroutines.withContext
import java.io.FileNotFoundException
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Acceso físico a los assets de cursos (I1: offline-first — todo el
 * contenido vive empaquetado en la app). Lee
 * `assets/courses/<courseId>/{manifest.json,course.json}` y cachea lo
 * decodificado: la primera lectura es IO y las siguientes son de memoria.
 * Los errores cruzan capas como [Result]/[LinguaError], no como
 * excepciones (I7), y el contenido defectuoso se detecta en la carga
 * (InvalidContent), nunca durante la sesión del usuario (I4).
 */
@Singleton
public class CourseLoader
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
        private val dispatchers: DispatcherProvider,
    ) {
        @Volatile
        private var cachedManifests: Map<String, CourseSummary>? = null

        private val courseCache = ConcurrentHashMap<String, Result<Course>>()

        /** Manifests ligeros de todos los cursos empaquetados (orden estable). */
        public suspend fun listManifests(): Result<Map<String, CourseSummary>> {
            cachedManifests?.let { manifests -> return Result.success(manifests) }
            return withContext(dispatchers.io) {
                val dirs = readCourseDirs()
                if (dirs == null) {
                    return@withContext Result.error(LinguaError.Unknown)
                }
                if (dirs.isEmpty()) {
                    return@withContext Result.error(LinguaError.NotFound)
                }
                val manifests = LinkedHashMap<String, CourseSummary>()
                for (dir in dirs) {
                    val manifest =
                        when (val text = readAssetText(manifestPath(dir))) {
                            is Result.Success -> CourseJson.parseManifest(text.value)
                            is Result.Error -> return@withContext Result.error(text.error)
                        }
                    if (manifest == null || manifest.id.value != dir) {
                        return@withContext Result.error(LinguaError.InvalidContent)
                    }
                    manifests[dir] = manifest
                }
                val snapshot = manifests.toMap()
                cachedManifests = snapshot
                Result.success(snapshot)
            }
        }

        /** Manifest de un curso concreto. */
        public suspend fun loadManifest(courseId: String): Result<CourseSummary> =
            when (val manifests = listManifests()) {
                is Result.Error -> manifests
                is Result.Success ->
                    when (val manifest = manifests.value[courseId]) {
                        null -> Result.error(LinguaError.NotFound)
                        else -> Result.success(manifest)
                    }
            }

        /** Curso completo decodificado, validado y cacheado por courseId. */
        public suspend fun loadCourse(courseId: CourseId): Result<Course> {
            courseCache[courseId.value]?.let { cached -> return cached }
            return withContext(dispatchers.io) {
                val parsed =
                    try {
                        CourseJson.parseCourse(readCourseFileText(courseId.value))
                    } catch (e: FileNotFoundException) {
                        return@withContext Result.error(LinguaError.NotFound)
                    } catch (e: IOException) {
                        return@withContext Result.error(LinguaError.Unknown)
                    }
                val verified =
                    when {
                        parsed == null -> Result.error(LinguaError.InvalidContent)
                        parsed.id != courseId -> Result.error(LinguaError.InvalidContent)
                        else ->
                            when (CourseValidator.validate(parsed)) {
                                is CourseValidation.Invalid -> Result.error(LinguaError.InvalidContent)
                                is CourseValidation.Valid -> Result.success(parsed)
                            }
                    }
                courseCache[courseId.value] = verified
                verified
            }
        }

        private fun readCourseDirs(): List<String>? =
            try {
                context.assets
                    .list(COURSE_ROOT)
                    .orEmpty()
                    .filter { it.isNotBlank() }
                    .sorted()
            } catch (e: IOException) {
                null
            }

        private suspend fun readCourseFileText(courseId: String): String =
            withContext(dispatchers.io) {
                context.assets.open(coursePath(courseId)).use { stream ->
                    stream.readBytes().decodeToString()
                }
            }

        private fun readAssetText(path: String): Result<String> =
            try {
                Result.success(context.assets.open(path).use { stream -> stream.readBytes().decodeToString() })
            } catch (e: FileNotFoundException) {
                Result.error(LinguaError.NotFound)
            } catch (e: IOException) {
                Result.error(LinguaError.Unknown)
            }

        public companion object {
            private const val COURSE_ROOT = "courses"

            private const val MANIFEST_FILE = "manifest.json"

            private const val COURSE_FILE = "course.json"

            private fun manifestPath(courseId: String): String = "$COURSE_ROOT/$courseId/$MANIFEST_FILE"

            private fun coursePath(courseId: String): String = "$COURSE_ROOT/$courseId/$COURSE_FILE"
        }
    }

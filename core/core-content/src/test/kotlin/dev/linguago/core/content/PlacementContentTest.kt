package dev.linguago.core.content

import dev.linguago.core.domain.model.Exercise
import dev.linguago.core.domain.placement.PlacementExamGenerator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import kotlin.random.Random

/**
 * Verifica el examen de nivelación sobre el contenido REAL empaquetado:
 * variedad de mecánicas, audio/dictado presentes y al menos 5 opciones en los
 * ejercicios de selección (ADR-058).
 */
public class PlacementContentTest {
    @Test
    public fun `cada curso genera un examen con audio dictado y cinco opciones`() {
        val courseIds = listOf("es-en", "es-pt", "es-fr", "es-ja")
        for (courseId in courseIds) {
            val course = CourseJson.parseCourse(readAsset(courseId, COURSE_FILE))
            assertTrue("El curso $courseId no decodifica", course != null)
            val exam = PlacementExamGenerator.generate(course!!, questionsPerLevel = 10, random = Random(42))
            assertEquals("Bloques CEFR inesperados en $courseId", 6, exam.blocks.size)
            exam.blocks.forEach { block ->
                assertEquals("Preguntas por nivel en $courseId/${block.level}", 10, block.questions.size)
                val levelExercises = block.questions.map { it.exercise }
                assertTrue(
                    "Sin escucha en $courseId/${block.level}",
                    levelExercises.any { it is Exercise.ListenChoose || it is Exercise.AudioOnlyPrompt },
                )
                assertTrue(
                    "Sin dictado en $courseId/${block.level}",
                    levelExercises.any { it is Exercise.ListenType },
                )
            }

            val exercises = exam.allQuestions.map { it.exercise }
            assertTrue(
                "El examen de $courseId no incluye escucha",
                exercises.any { it is Exercise.ListenChoose || it is Exercise.AudioOnlyPrompt },
            )
            assertTrue(
                "El examen de $courseId no incluye dictado",
                exercises.any { it is Exercise.ListenType },
            )

            exercises.forEach { exercise ->
                when (exercise) {
                    is Exercise.MultipleChoice ->
                        assertOptions(courseId, exercise.options, exercise.correctIndex)
                    is Exercise.ListenChoose -> {
                        assertTrue("ListenChoose sin audio en $courseId", exercise.audioText.isNotBlank())
                        assertOptions(courseId, exercise.options, exercise.correctIndex)
                    }
                    is Exercise.AudioOnlyPrompt -> {
                        assertTrue("AudioOnlyPrompt sin audio en $courseId", exercise.audioText.isNotBlank())
                        assertOptions(courseId, exercise.options, exercise.correctIndex)
                    }
                    is Exercise.ListenType ->
                        assertTrue("ListenType sin audio en $courseId", exercise.audioText.isNotBlank())
                    else -> Unit
                }
            }
        }
    }

    private fun assertOptions(
        courseId: String,
        options: List<String>,
        correctIndex: Int,
    ) {
        assertTrue("Menos de 5 opciones en $courseId: $options", options.size >= 5)
        assertEquals("Opciones repetidas en $courseId: $options", options.size, options.distinct().size)
        assertTrue("Índice correcto fuera de rango en $courseId", correctIndex in options.indices)
    }

    private fun readAsset(
        courseId: String,
        fileName: String,
    ): String {
        val path = File(contentRoot(), "content-$courseId/src/main/assets/courses/$courseId/$fileName")
        assertTrue("Falta el asset del contenido: ${path.path}", path.exists())
        return path.readText()
    }

    private fun contentRoot(): File {
        System.getProperty("linguago.content.dir")?.let { explicit -> return File(explicit) }
        val userDir = System.getProperty("user.dir") ?: error("No hay user.dir en ambiente de test")
        var dir = File(userDir)
        var depth = 0
        while (depth < SEARCH_DEPTH_UP) {
            val candidate = File(dir, "content")
            if (candidate.isDirectory) return candidate
            dir = dir.parentFile ?: break
            depth++
        }
        error("No se ubicó el directorio content desde $userDir")
    }

    private companion object {
        private const val COURSE_FILE = "course.json"
        private const val SEARCH_DEPTH_UP = 4
    }
}

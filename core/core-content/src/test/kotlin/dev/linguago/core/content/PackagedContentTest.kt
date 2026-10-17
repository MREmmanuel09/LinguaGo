package dev.linguago.core.content

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Golpe de caja del contenido REAL empaquetado en los módulos content: cada
 * curso del repo se decodifica con la MISMA maquinaria de runtime
 * (CourseJson + CourseValidator) y debe pasar limpio. Si este test falla en
 * CI, el contenido se rompió antes de llegar al usuario.
 */
class PackagedContentTest {
    @Test
    fun los_cursos_empaquetados_parsean_y_validan() {
        val expectations =
            listOf(
                "es-en" to "Inglés esencial",
                "es-pt" to "Portugués de arranque",
                "es-fr" to "Francés de cabeza",
                "es-ja" to "Japonés de sillón",
            )
        for ((courseId, expectedTitle) in expectations) {
            val manifest = CourseJson.parseManifest(readAsset(courseId, MANIFEST_FILE))
            assertTrue("El manifest de $courseId no decodifica", manifest != null)
            assertEquals("Título inesperado en $courseId", expectedTitle, manifest?.title)

            val parsed = CourseJson.parseCourse(readAsset(courseId, COURSE_FILE))
            assertTrue("El curso $courseId no decodifica", parsed != null)
            val validation = CourseValidator.validate(parsed!!)
            assertTrue(
                "El curso $courseId falla la validación: $validation",
                validation == CourseValidation.Valid,
            )
        }
    }

    /** Layout del repo relativo al directorio de trabajo del módulo. */
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
        private const val MANIFEST_FILE = "manifest.json"
        private const val COURSE_FILE = "course.json"
        private const val SEARCH_DEPTH_UP = 4
    }
}

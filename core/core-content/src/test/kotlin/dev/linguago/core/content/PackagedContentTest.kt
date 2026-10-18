package dev.linguago.core.content

import dev.linguago.core.domain.model.CourseLocale
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

    @Test
    fun es_en_v2_conserva_ids_historicos_y_feedback() {
        val course = CourseJson.parseCourse(readAsset("es-en", COURSE_FILE))
        assertTrue("es-en debe seguir decodificando en v2", course != null)
        assertEquals(2, course!!.schemaVersion)
        assertEquals(CourseLocale.ENGLISH_US, course.locale)

        val lessons = course.levels.flatMap { level -> level.units.flatMap { unit -> unit.lessons } }
        assertEquals(HISTORIC_LESSON_IDS, lessons.map { lesson -> lesson.id.value })

        val exercises = lessons.flatMap { lesson -> lesson.exercises }
        assertEquals(HISTORIC_EXERCISE_IDS, exercises.map { exercise -> exercise.id.value })
        assertTrue(
            "La migración debe enriquecer feedbackOnError en todos los ejercicios",
            exercises.all { exercise -> exercise.feedbackOnError != null },
        )
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

        private val HISTORIC_LESSON_IDS =
            listOf(
                "es-en-lv-1-un-1-ls-1",
                "es-en-lv-1-un-1-ls-2",
                "es-en-lv-1-un-1-ls-3",
                "es-en-lv-1-un-2-ls-1",
                "es-en-lv-1-un-2-ls-2",
                "es-en-lv-1-un-2-ls-3",
            )

        private val HISTORIC_EXERCISE_IDS =
            listOf(
                "es-en-e1",
                "es-en-e2",
                "es-en-e3",
                "es-en-e4",
                "es-en-e5",
                "es-en-e6",
                "es-en-e37",
                "es-en-e7",
                "es-en-e8",
                "es-en-e9",
                "es-en-e10",
                "es-en-e11",
                "es-en-e12",
                "es-en-e38",
                "es-en-e13",
                "es-en-e14",
                "es-en-e15",
                "es-en-e16",
                "es-en-e17",
                "es-en-e18",
                "es-en-e39",
                "es-en-e19",
                "es-en-e20",
                "es-en-e21",
                "es-en-e22",
                "es-en-e23",
                "es-en-e24",
                "es-en-e40",
                "es-en-e25",
                "es-en-e26",
                "es-en-e27",
                "es-en-e28",
                "es-en-e29",
                "es-en-e30",
                "es-en-e41",
                "es-en-e31",
                "es-en-e32",
                "es-en-e33",
                "es-en-e34",
                "es-en-e35",
                "es-en-e36",
                "es-en-e42",
            )
    }
}

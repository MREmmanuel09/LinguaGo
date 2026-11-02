package dev.linguago.core.content

import dev.linguago.core.domain.model.CefrLevel
import dev.linguago.core.domain.model.Course
import dev.linguago.core.domain.model.Exercise
import dev.linguago.core.domain.model.Lesson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Rampa pedagógica del curso es-en (ADR-018/ADR-040): el camino no retrocede
 * de nivel, los ejercicios y la gramática escalan por CEFR y las frases
 * respetan un tope de longitud por nivel. Protege contra saltos bruscos al
 * añadir contenido (p. ej. una lección B2 colada en el bloque A2).
 */
class ContentRampTest {
    @Test
    fun `cada nivel agrupa un solo cefr`() {
        for (level in course.levels) {
            val levels =
                level.units
                    .flatMap { unit -> unit.lessons }
                    .map { lesson -> lesson.cefrLevel }
                    .distinct()
            assertEquals(
                "El nivel ${level.id.value} mezcla niveles CEFR: $levels",
                1,
                levels.size,
            )
        }
    }

    @Test
    fun `la rampa de niveles no retrocede`() {
        val ranks = lessons.map { lesson -> lesson.cefrLevel.rank() }
        assertEquals("La secuencia de niveles debe ser no decreciente", ranks.sorted(), ranks)
    }

    @Test
    fun `los ejercicios por leccion escalan por nivel`() {
        val expected = mapOf(CefrLevel.A1 to 7, CefrLevel.A2 to 7, CefrLevel.B1 to 9, CefrLevel.B2 to 10)
        for (lesson in lessons) {
            val count = expected.getValue(lesson.cefrLevel)
            assertEquals(
                "Lección ${lesson.id.value} debe tener $count ejercicios",
                count,
                lesson.exercises.size,
            )
        }
    }

    @Test
    fun `la gramatica por leccion es limitada`() {
        for (lesson in lessons) {
            val points = lesson.grammarPoints.size
            when (lesson.cefrLevel) {
                CefrLevel.A1 -> assertEquals("A1 no declara gramática (${lesson.id.value})", 0, points)
                CefrLevel.A2 ->
                    assertTrue(
                        "A2 con más de 2 focos gramaticales (${lesson.id.value})",
                        points <= 2,
                    )
                CefrLevel.B1, CefrLevel.B2 ->
                    assertEquals(
                        "${lesson.cefrLevel} declara 2 focos (${lesson.id.value})",
                        2,
                        points,
                    )
            }
        }
    }

    @Test
    fun `las frases respetan el tope de longitud por nivel`() {
        val caps = mapOf(CefrLevel.A1 to 8, CefrLevel.A2 to 12, CefrLevel.B1 to 24, CefrLevel.B2 to 30)
        for (lesson in lessons) {
            val cap = caps.getValue(lesson.cefrLevel)
            val longest = lesson.longestSentenceTokens()
            assertTrue(
                "Lección ${lesson.id.value} (${lesson.cefrLevel}) tiene una frase de $longest tokens (tope $cap)",
                longest <= cap,
            )
        }
    }

    @Test
    fun `los cursos de arranque respetan la rampa A1`() {
        for (courseId in STARTER_COURSES) {
            val starterLessons = lessonsFor(courseId).filter { lesson -> lesson.cefrLevel == CefrLevel.A1 }
            assertTrue("$courseId debe tener lecciones A1", starterLessons.isNotEmpty())
            for (lesson in starterLessons) {
                assertEquals("$courseId debe ser A1 (${lesson.id.value})", CefrLevel.A1, lesson.cefrLevel)
                assertEquals("${lesson.id.value} debe tener 7 ejercicios", 7, lesson.exercises.size)
                assertEquals("${lesson.id.value} no declara gramática", 0, lesson.grammarPoints.size)
                assertTrue(
                    "${lesson.id.value} supera el tope A1 de 8 tokens",
                    lesson.longestSentenceTokens() <= 8,
                )
                assertTrue(
                    "${lesson.id.value} usa tipos no permitidos en A1: " +
                        lesson.exercises
                            .filterNot { exercise -> exercise.isAllowedFor(CefrLevel.A1) }
                            .joinToString { exercise -> exercise.typeName() },
                    lesson.exercises.all { exercise -> exercise.isAllowedFor(CefrLevel.A1) },
                )
            }
        }
    }

    @Test
    fun `los cursos de arranque amplian la rampa A2`() {
        for (courseId in STARTER_COURSES) {
            val a2Lessons = lessonsFor(courseId).filter { lesson -> lesson.cefrLevel == CefrLevel.A2 }
            assertEquals("$courseId debe tener 8 lecciones A2", 8, a2Lessons.size)
            for (lesson in a2Lessons) {
                assertEquals("${lesson.id.value} debe tener 7 ejercicios", 7, lesson.exercises.size)
                assertEquals("${lesson.id.value} no declara gramática", 0, lesson.grammarPoints.size)
                assertTrue(
                    "${lesson.id.value} supera el tope A2 de 12 tokens",
                    lesson.longestSentenceTokens() <= 12,
                )
                assertTrue(
                    "${lesson.id.value} usa tipos no permitidos en A2: " +
                        lesson.exercises
                            .filterNot { exercise -> exercise.isAllowedFor(CefrLevel.A2) }
                            .joinToString { exercise -> exercise.typeName() },
                    lesson.exercises.all { exercise -> exercise.isAllowedFor(CefrLevel.A2) },
                )
            }
        }
    }

    @Test
    fun `los cursos de arranque amplian la rampa B1`() {
        for (courseId in STARTER_COURSES) {
            val b1Lessons = lessonsFor(courseId).filter { lesson -> lesson.cefrLevel == CefrLevel.B1 }
            assertEquals("$courseId debe tener 20 lecciones B1", 20, b1Lessons.size)
            for (lesson in b1Lessons) {
                assertEquals("${lesson.id.value} debe tener 9 ejercicios", 9, lesson.exercises.size)
                assertEquals("${lesson.id.value} debe declarar 2 focos gramaticales", 2, lesson.grammarPoints.size)
                assertTrue(
                    "${lesson.id.value} supera el tope B1 de 24 tokens",
                    lesson.longestSentenceTokens() <= 24,
                )
                assertTrue(
                    "${lesson.id.value} usa tipos no permitidos en B1: " +
                        lesson.exercises
                            .filterNot { exercise -> exercise.isAllowedFor(CefrLevel.B1) }
                            .joinToString { exercise -> exercise.typeName() },
                    lesson.exercises.all { exercise -> exercise.isAllowedFor(CefrLevel.B1) },
                )
                assertEquals(
                    "${lesson.id.value} debe tener un FreeSpeakingPrompt",
                    1,
                    lesson.exercises.count { exercise -> exercise is Exercise.FreeSpeakingPrompt },
                )
            }
        }
    }

    @Test
    fun `los cursos de arranque amplian la rampa B2`() {
        for (courseId in STARTER_COURSES) {
            val b2Lessons = lessonsFor(courseId).filter { lesson -> lesson.cefrLevel == CefrLevel.B2 }
            assertEquals("$courseId debe tener 22 lecciones B2", 22, b2Lessons.size)
            for (lesson in b2Lessons) {
                assertEquals("${lesson.id.value} debe tener 10 ejercicios", 10, lesson.exercises.size)
                assertEquals("${lesson.id.value} debe declarar 2 focos gramaticales", 2, lesson.grammarPoints.size)
                assertTrue(
                    "${lesson.id.value} supera el tope B2 de 30 tokens",
                    lesson.longestSentenceTokens() <= 30,
                )
                assertTrue(
                    "${lesson.id.value} usa tipos no permitidos en B2: " +
                        lesson.exercises
                            .filterNot { exercise -> exercise.isAllowedFor(CefrLevel.B2) }
                            .joinToString { exercise -> exercise.typeName() },
                    lesson.exercises.all { exercise -> exercise.isAllowedFor(CefrLevel.B2) },
                )
                assertEquals(
                    "${lesson.id.value} debe tener un FreeSpeakingPrompt",
                    1,
                    lesson.exercises.count { exercise -> exercise is Exercise.FreeSpeakingPrompt },
                )
            }
        }
    }

    @Test
    fun `los tipos de ejercicio son los del nivel`() {
        for (lesson in lessons) {
            val unexpected = lesson.exercises.filter { exercise -> !exercise.isAllowedFor(lesson.cefrLevel) }
            assertTrue(
                "Lección ${lesson.id.value} usa tipos no permitidos en ${lesson.cefrLevel}: " +
                    unexpected.joinToString { exercise -> exercise.typeName() },
                unexpected.isEmpty(),
            )
        }
    }

    private val course: Course by lazy {
        val parsed: Course? = CourseJson.parseCourse(readAsset("es-en", COURSE_FILE))
        assertTrue("es-en debe decodificar", parsed != null)
        parsed!!
    }

    private val lessons: List<Lesson> by lazy {
        course.levels.flatMap { level -> level.units.flatMap { unit -> unit.lessons } }
    }

    private fun lessonsFor(courseId: String): List<Lesson> {
        val parsed: Course? = CourseJson.parseCourse(readAsset(courseId, COURSE_FILE))
        assertTrue("$courseId debe decodificar", parsed != null)
        return parsed!!.levels.flatMap { level -> level.units.flatMap { unit -> unit.lessons } }
    }

    private fun CefrLevel.rank(): Int =
        when (this) {
            CefrLevel.A1 -> 1
            CefrLevel.A2 -> 2
            CefrLevel.B1 -> 3
            CefrLevel.B2 -> 4
        }

    private fun Lesson.longestSentenceTokens(): Int {
        val sentences = mutableListOf<List<String>>()
        for (exercise in exercises) {
            when (exercise) {
                is Exercise.Translate -> sentences += exercise.correctTokens
                is Exercise.OrderWords -> sentences += exercise.answerTokens
                is Exercise.ListenType -> sentences += exercise.transcription.words()
                is Exercise.SpeakRepeat -> sentences += exercise.targetPhrase.words()
                is Exercise.AudioOnlyPrompt -> sentences += exercise.audioText.sentences()
                is Exercise.ClozeDragDrop ->
                    sentences += exercise.passage.replace(PLACEHOLDER, "x").sentences()
                is Exercise.FreeSpeakingPrompt -> sentences += exercise.modelAnswer.sentences()
                is Exercise.SemanticMatch ->
                    sentences += exercise.pairs.map { pair -> pair.left.words() }
                else -> Unit
            }
        }
        return sentences.maxOfOrNull { words -> words.size } ?: 0
    }

    private fun String.words(): List<String> = trim().split(WHITESPACE).filter { word -> word.isNotBlank() }

    private fun String.sentences(): List<List<String>> =
        split(SENTENCE_SPLIT).map { sentence -> sentence.words() }.filter { words -> words.isNotEmpty() }

    private fun Exercise.typeName(): String = this::class.simpleName ?: "?"

    private fun Exercise.isAllowedFor(level: CefrLevel): Boolean =
        when (level) {
            CefrLevel.A1, CefrLevel.A2 ->
                this is Exercise.MultipleChoice ||
                    this is Exercise.Translate ||
                    this is Exercise.OrderWords ||
                    this is Exercise.MatchPairs ||
                    this is Exercise.ListenChoose ||
                    this is Exercise.ListenType ||
                    this is Exercise.SpeakRepeat ||
                    this is Exercise.KanaChoose ||
                    this is Exercise.KanjiReading ||
                    this is Exercise.ParticleFill
            CefrLevel.B1 ->
                this is Exercise.MultipleChoice ||
                    this is Exercise.Translate ||
                    this is Exercise.OrderWords ||
                    this is Exercise.MatchPairs ||
                    this is Exercise.ListenChoose ||
                    this is Exercise.ListenType ||
                    this is Exercise.SpeakRepeat ||
                    this is Exercise.KanaChoose ||
                    this is Exercise.KanjiReading ||
                    this is Exercise.ParticleFill ||
                    this is Exercise.ClozeDragDrop ||
                    this is Exercise.FreeSpeakingPrompt
            CefrLevel.B2 ->
                this is Exercise.MultipleChoice ||
                    this is Exercise.OrderWords ||
                    this is Exercise.ListenType ||
                    this is Exercise.SpeakRepeat ||
                    this is Exercise.ClozeDragDrop ||
                    this is Exercise.SemanticMatch ||
                    this is Exercise.AudioOnlyPrompt ||
                    this is Exercise.FreeSpeakingPrompt
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
        private val STARTER_COURSES = listOf("es-fr", "es-pt", "es-ja")
        private val WHITESPACE = Regex("\\s+")
        private val SENTENCE_SPLIT = Regex("[.?!。！？]+")
        private val PLACEHOLDER = Regex("\\{\\{[^}]+\\}\\}")
    }
}

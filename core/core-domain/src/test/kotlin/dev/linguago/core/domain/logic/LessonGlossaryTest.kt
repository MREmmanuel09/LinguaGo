package dev.linguago.core.domain.logic

import dev.linguago.core.domain.model.Exercise
import dev.linguago.core.domain.model.ExerciseId
import dev.linguago.core.domain.model.GlossaryEntry
import dev.linguago.core.domain.model.Lesson
import dev.linguago.core.domain.model.LessonId
import dev.linguago.core.domain.model.MicroLesson
import dev.linguago.core.domain.model.MicroLessonExample
import dev.linguago.core.domain.model.MicroLessonSection
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** Glosario de lección para el long-press de palabras (ADR-040). */
public class LessonGlossaryTest {
    @Test
    public fun `fusiona contenido ejemplos y vocabulario con varias traducciones`() {
        val lesson =
            Lesson(
                id = LessonId("ls-1"),
                title = "Banco",
                exercises =
                    listOf(
                        Exercise.MatchPairs(
                            id = ExerciseId("e1"),
                            pairs = listOf(Exercise.MatchPair("banco", "bank")),
                        ),
                    ),
                microLesson =
                    MicroLesson(
                        title = "Micro",
                        sections =
                            listOf(
                                MicroLessonSection(
                                    heading = "Sección",
                                    body = "Cuerpo",
                                    examples = listOf(MicroLessonExample(source = "orilla", target = "bank")),
                                ),
                            ),
                    ),
                glossary =
                    listOf(
                        GlossaryEntry(term = "bank", translations = listOf("banco", "orilla")),
                        GlossaryEntry(term = "House", translations = listOf("casa")),
                    ),
            )

        val glossary = LessonGlossary.fromLesson(lesson)

        assertEquals(listOf("banco", "orilla"), glossary["bank"])
        assertEquals(listOf("casa"), glossary["house"])
        // Reverso de traducciones de una sola palabra (español→inglés).
        assertEquals(listOf("bank"), glossary["banco"])
        assertEquals(listOf("bank"), glossary["orilla"])
        assertEquals(listOf("House"), glossary["casa"])
    }

    @Test
    public fun `busca con puntuacion y fallback de singular`() {
        val glossary =
            mapOf(
                "house" to listOf("casa"),
                "city" to listOf("ciudad"),
                "box" to listOf("caja"),
            )

        assertEquals(listOf("casa"), LessonGlossary.lookup(glossary, "House,"))
        assertEquals(listOf("casa"), LessonGlossary.lookup(glossary, "houses"))
        assertEquals(listOf("ciudad"), LessonGlossary.lookup(glossary, "cities"))
        assertEquals(listOf("caja"), LessonGlossary.lookup(glossary, "boxes"))
        assertNull(LessonGlossary.lookup(glossary, "banana"))
        assertNull(LessonGlossary.lookup(glossary, "—"))
    }

    @Test
    public fun `busca formas verbales simples por inflexion`() {
        val glossary =
            mapOf(
                "start" to listOf("empezar"),
                "work" to listOf("trabajar"),
                "approve" to listOf("aprobar"),
                "run" to listOf("correr"),
            )

        assertEquals(listOf("empezar"), LessonGlossary.lookup(glossary, "started"))
        assertEquals(listOf("trabajar"), LessonGlossary.lookup(glossary, "working"))
        assertEquals(listOf("aprobar"), LessonGlossary.lookup(glossary, "approved"))
        assertEquals(listOf("correr"), LessonGlossary.lookup(glossary, "running"))
    }

    @Test
    public fun `normaliza mayusculas y puntuacion de borde`() {
        assertEquals("hello", LessonGlossary.normalize("  ¡Hello!  "))
        assertEquals("good morning", LessonGlossary.normalize("«Good   morning»"))
    }

    @Test
    public fun `encuentra frases y modismos exactos`() {
        val glossary =
            mapOf(
                "play it by ear" to listOf("improvisar según las circunstancias"),
                "ballpark figure" to listOf("cifra aproximada"),
            )

        assertEquals(
            listOf("improvisar según las circunstancias"),
            LessonGlossary.lookupExact(glossary, "Play it by ear."),
        )
        assertEquals(listOf("cifra aproximada"), LessonGlossary.lookupExact(glossary, "ballpark figure"))
        assertNull(LessonGlossary.lookupExact(glossary, "by ear"))
    }
}

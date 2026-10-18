package dev.linguago.core.domain.logic

import dev.linguago.core.domain.model.CulturalNote
import dev.linguago.core.domain.model.Exercise
import dev.linguago.core.domain.model.ExerciseId
import dev.linguago.core.domain.model.Lesson
import dev.linguago.core.domain.model.LessonId
import org.junit.Assert.assertEquals
import org.junit.Test

/** Extracción de vocabulario revisable desde el contenido (ADR-016). */
public class VocabularyExtractorTest {
    @Test
    public fun `extrae pares de match pairs kana y kanji`() {
        val lesson =
            Lesson(
                id = LessonId("ls-1"),
                title = "Hiragana",
                exercises =
                    listOf(
                        Exercise.MatchPairs(
                            id = ExerciseId("e1"),
                            pairs =
                                listOf(
                                    Exercise.MatchPair("hermano", "brother"),
                                    Exercise.MatchPair("hermana", "sister"),
                                ),
                        ),
                        Exercise.KanaChoose(
                            id = ExerciseId("e2"),
                            kana = "あ",
                            romajiOptions = listOf("a", "i", "u"),
                            correctRomaji = "a",
                        ),
                        Exercise.KanjiReading(
                            id = ExerciseId("e3"),
                            kanji = "山",
                            readingOptions = listOf("やま", "かわ"),
                            correctReading = "やま",
                        ),
                    ),
            )

        val pairs = VocabularyExtractor.fromLesson(lesson)

        assertEquals(4, pairs.size)
        assertEquals(VocabularyPair("hermano", "brother"), pairs[0])
        assertEquals(VocabularyPair("あ", "a"), pairs[2])
        assertEquals(VocabularyPair("山", "やま"), pairs[3])
    }

    @Test
    public fun `ignora ejercicios sin pares y deduplica por palabra`() {
        val lesson =
            Lesson(
                id = LessonId("ls-2"),
                title = "Frases",
                exercises =
                    listOf(
                        Exercise.MatchPairs(
                            id = ExerciseId("e1"),
                            pairs = listOf(Exercise.MatchPair("  hermana ", "sister")),
                        ),
                        Exercise.MatchPairs(
                            id = ExerciseId("e2"),
                            pairs = listOf(Exercise.MatchPair("Hermana", "sister")),
                        ),
                        Exercise.OrderWords(
                            id = ExerciseId("e3"),
                            tokens = listOf("I", "am"),
                            answerTokens = listOf("I", "am"),
                        ),
                    ),
            )

        val pairs = VocabularyExtractor.fromLesson(lesson)

        assertEquals(1, pairs.size)
        assertEquals(VocabularyPair("hermana", "sister"), pairs.single())
    }

    // ---- F1.2: modismos y colocaciones B2 con nota cultural ----

    @Test
    public fun `extrae modismo con traduccion y nota cultural`() {
        val lesson =
            Lesson(
                id = LessonId("ls-b2"),
                title = "Negociación",
                exercises = emptyList(),
                culturalNotes =
                    listOf(
                        CulturalNote(
                            idiom = "play it by ear",
                            note = "Decidir sobre la marcha sin comprometerse.",
                            translation = "improvisar según las circunstancias",
                        ),
                    ),
            )

        val pairs = VocabularyExtractor.fromLesson(lesson)

        assertEquals(1, pairs.size)
        assertEquals(
            VocabularyPair(
                source = "play it by ear",
                target = "improvisar según las circunstancias",
                culturalNote = "Decidir sobre la marcha sin comprometerse.",
            ),
            pairs.single(),
        )
    }

    @Test
    public fun `modismo sin traduccion usa la nota como significado`() {
        val lesson =
            Lesson(
                id = LessonId("ls-b2"),
                title = "Negociación",
                exercises = emptyList(),
                culturalNotes =
                    listOf(
                        CulturalNote(
                            idiom = "play it by ear",
                            note = "Decidir sobre la marcha sin comprometerse.",
                            translation = null,
                        ),
                    ),
            )

        val pairs = VocabularyExtractor.fromLesson(lesson)

        assertEquals(VocabularyPair("play it by ear", "Decidir sobre la marcha sin comprometerse."), pairs.single())
    }

    @Test
    public fun `modismo gana la deduplicacion sobre match pairs y conserva su nota`() {
        val lesson =
            Lesson(
                id = LessonId("ls-b2"),
                title = "Negociación",
                exercises =
                    listOf(
                        Exercise.MatchPairs(
                            id = ExerciseId("mp"),
                            pairs = listOf(Exercise.MatchPair("play it by ear", "improvisar")),
                        ),
                    ),
                culturalNotes =
                    listOf(
                        CulturalNote(
                            idiom = "play it by ear",
                            note = "Nota cultural británica.",
                            translation = "improvisar según las circunstancias",
                        ),
                    ),
            )

        val pairs = VocabularyExtractor.fromLesson(lesson)

        assertEquals(1, pairs.size)
        assertEquals(
            VocabularyPair(
                source = "play it by ear",
                target = "improvisar según las circunstancias",
                culturalNote = "Nota cultural británica.",
            ),
            pairs.single(),
        )
    }

    @Test
    public fun `modismos con idiom o significado vacios se descartan`() {
        val lesson =
            Lesson(
                id = LessonId("ls-vacio"),
                title = "Vacío",
                exercises = emptyList(),
                culturalNotes =
                    listOf(
                        CulturalNote(idiom = "   ", note = "Nota válida."),
                        CulturalNote(idiom = "break the ice", note = "   "),
                    ),
            )

        assertEquals(emptyList<VocabularyPair>(), VocabularyExtractor.fromLesson(lesson))
    }

    @Test
    public fun `modismo sin traduccion no duplica la nota como nota cultural`() {
        val lesson =
            Lesson(
                id = LessonId("ls-nota"),
                title = "Nota única",
                exercises = emptyList(),
                culturalNotes =
                    listOf(
                        CulturalNote(idiom = "break the ice", note = "Romper la tensión.", translation = null),
                    ),
            )

        val pair = VocabularyExtractor.fromLesson(lesson).single()

        assertEquals(null, pair.culturalNote)
    }

    @Test
    public fun `modismo con espacios alrededor se recorta`() {
        val lesson =
            Lesson(
                id = LessonId("ls-trim"),
                title = "Recorte",
                exercises = emptyList(),
                culturalNotes =
                    listOf(
                        CulturalNote(
                            idiom = "  break the ice  ",
                            note = "  Romper la tensión.  ",
                            translation = "  romper el hielo  ",
                        ),
                    ),
            )

        assertEquals(
            VocabularyPair("break the ice", "romper el hielo", "Romper la tensión."),
            VocabularyExtractor.fromLesson(lesson).single(),
        )
    }

    @Test
    public fun `modismo y match pair se extraen juntos sin colision`() {
        val lesson =
            Lesson(
                id = LessonId("ls-mixta"),
                title = "Mixta",
                exercises =
                    listOf(
                        Exercise.MatchPairs(
                            id = ExerciseId("mp"),
                            pairs = listOf(Exercise.MatchPair("deadline", "plazo")),
                        ),
                    ),
                culturalNotes =
                    listOf(
                        CulturalNote(idiom = "play it by ear", note = "Decidir sobre la marcha.", translation = null),
                    ),
            )

        val pairs = VocabularyExtractor.fromLesson(lesson)

        assertEquals(2, pairs.size)
        assertEquals("play it by ear", pairs[0].source)
        assertEquals("deadline", pairs[1].source)
    }
}

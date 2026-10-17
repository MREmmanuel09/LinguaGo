package dev.linguago.core.domain.logic

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
}

package dev.linguago.core.domain.logic

import dev.linguago.core.domain.model.Exercise
import dev.linguago.core.domain.model.ExerciseId
import dev.linguago.core.domain.usecase.ExerciseAnswer
import org.junit.Assert.assertEquals
import org.junit.Test

class ExerciseGraderTest {
    private fun ejercicioId(id: String): ExerciseId = ExerciseId(id)

    @Test
    fun multiple_choice_acierta_por_indice() {
        val exercise =
            Exercise.MultipleChoice(
                id = ejercicioId("mc"),
                question = "Cómo se dice 'manzana'?",
                options = listOf("apple", "orange", "banana"),
                correctIndex = 0,
            )
        assertEquals(true, ExerciseGrader.isCorrect(exercise, ExerciseAnswer.OptionPicked(0)))
        assertEquals(false, ExerciseGrader.isCorrect(exercise, ExerciseAnswer.OptionPicked(1)))
        assertEquals(false, ExerciseGrader.isCorrect(exercise, ExerciseAnswer.OptionPicked(5)))
    }

    @Test
    fun kana_choose_compara_romaji_no_indice_del_correcto() {
        val exercise =
            Exercise.KanaChoose(
                id = ejercicioId("kana"),
                kana = "あ",
                romajiOptions = listOf("i", "a", "u"),
                correctRomaji = "a",
            )
        assertEquals(true, ExerciseGrader.isCorrect(exercise, ExerciseAnswer.OptionPicked(1)))
        assertEquals(false, ExerciseGrader.isCorrect(exercise, ExerciseAnswer.OptionPicked(0)))
    }

    @Test
    fun translate_secuencia_correcta_aceptando_desorden_de_espacios() {
        val exercise =
            Exercise.Translate(
                id = ejercicioId("tr"),
                sourcePhrase = "Apple",
                correctTokens = listOf("the", "red", "apple"),
            )
        assertEquals(true, ExerciseGrader.isCorrect(exercise, ExerciseAnswer.TokenSequence(listOf("the", "red", "apple"))))
        assertEquals(true, ExerciseGrader.isCorrect(exercise, ExerciseAnswer.TokenSequence(listOf("The", "Red ", "  apple"))))
        assertEquals(false, ExerciseGrader.isCorrect(exercise, ExerciseAnswer.TokenSequence(listOf("red", "apple", "the"))))
        assertEquals(false, ExerciseGrader.isCorrect(exercise, ExerciseAnswer.TokenSequence(listOf("the", "red", "pear"))))
    }

    @Test
    fun order_words_ignora_puntuacion_y_capitalizacion() {
        val exercise =
            Exercise.OrderWords(
                id = ejercicioId("ow"),
                tokens = listOf("Good", "morning", "teacher", "!"),
                answerTokens = listOf("Good", "morning", "teacher"),
            )
        assertEquals(true, ExerciseGrader.isCorrect(exercise, ExerciseAnswer.TokenSequence(listOf("Good", "morning", "teacher"))))
        assertEquals(true, ExerciseGrader.isCorrect(exercise, ExerciseAnswer.TokenSequence(listOf("good", "morning", "teacher!"))))
        assertEquals(false, ExerciseGrader.isCorrect(exercise, ExerciseAnswer.TokenSequence(listOf("morning", "good", "teacher"))))
    }

    @Test
    fun match_pairs_exige_mapeo_canonico_identico() {
        val exercise =
            Exercise.MatchPairs(
                id = ejercicioId("mp"),
                pairs =
                    listOf(
                        Exercise.MatchPair(left = "perro", right = "dog"),
                        Exercise.MatchPair(left = "gato", right = "cat"),
                        Exercise.MatchPair(left = "pájaro", right = "bird"),
                    ),
            )
        assertEquals(true, ExerciseGrader.isCorrect(exercise, ExerciseAnswer.MatchedPairs(mapOf(0 to 0, 1 to 1, 2 to 2))))
        assertEquals(false, ExerciseGrader.isCorrect(exercise, ExerciseAnswer.MatchedPairs(mapOf(0 to 1, 1 to 0, 2 to 2))))
        assertEquals(false, ExerciseGrader.isCorrect(exercise, ExerciseAnswer.MatchedPairs(mapOf(0 to 0, 1 to 1))))
    }

    @Test
    fun listen_type_normaliza_puntuacion_japonesa() {
        val exercise =
            Exercise.ListenType(
                id = ejercicioId("lt"),
                transcription = "わたし は 日本人 です。",
                audioText = "わたし は 日本人 です。",
            )
        assertEquals(true, ExerciseGrader.isCorrect(exercise, ExerciseAnswer.TypedText("わたし は 日本人 です")))
        assertEquals(false, ExerciseGrader.isCorrect(exercise, ExerciseAnswer.TypedText("わたしは日本人です。")))
        assertEquals(false, ExerciseGrader.isCorrect(exercise, ExerciseAnswer.TypedText("わたし は 犬 です")))
    }

    @Test
    fun answer_type_inesperado_es_incorrecto_sin_excepciones() {
        val exercise =
            Exercise.MultipleChoice(
                id = ejercicioId("mc"),
                question = "q",
                options = listOf("a"),
                correctIndex = 0,
            )
        assertEquals(false, ExerciseGrader.isCorrect(exercise, ExerciseAnswer.TokenSequence(listOf("a"))))
        assertEquals(false, ExerciseGrader.isCorrect(exercise, ExerciseAnswer.TypedText("a")))
        assertEquals(
            false,
            ExerciseGrader.isCorrect(exercise, ExerciseAnswer.MatchedPairs(mapOf(0 to 0))),
        )
    }
}

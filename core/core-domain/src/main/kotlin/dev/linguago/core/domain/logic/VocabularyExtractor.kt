package dev.linguago.core.domain.logic

import dev.linguago.core.domain.model.Exercise
import dev.linguago.core.domain.model.Lesson

/** Par palabra↔traducción listo para convertirse en tarjeta FSRS. */
public data class VocabularyPair(
    val source: String,
    val target: String,
)

/**
 * Extrae vocabulario revisable del contenido (ADR-016): solo pares
 * explícitos palabra↔significado — MatchPairs, KanaChoose (kana→romaji) y
 * KanjiReading (kanji→lectura). Frases completas no entran al repaso.
 */
public object VocabularyExtractor {
    public fun fromLesson(lesson: Lesson): List<VocabularyPair> {
        val pairs = mutableListOf<VocabularyPair>()
        for (exercise in lesson.exercises) {
            when (exercise) {
                is Exercise.MatchPairs ->
                    exercise.pairs.forEach { pair ->
                        pairs += VocabularyPair(pair.left, pair.right)
                    }
                is Exercise.KanaChoose ->
                    pairs += VocabularyPair(exercise.kana, exercise.correctRomaji)
                is Exercise.KanjiReading ->
                    pairs += VocabularyPair(exercise.kanji, exercise.correctReading)
                else -> Unit
            }
        }
        return pairs
            .map { pair -> VocabularyPair(pair.source.trim(), pair.target.trim()) }
            .filter { pair -> pair.source.isNotBlank() && pair.target.isNotBlank() }
            .distinctBy { pair -> pair.source.lowercase() }
    }
}

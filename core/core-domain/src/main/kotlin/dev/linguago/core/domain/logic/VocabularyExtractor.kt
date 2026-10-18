package dev.linguago.core.domain.logic

import dev.linguago.core.domain.model.Exercise
import dev.linguago.core.domain.model.Lesson

/** Par palabra↔traducción listo para convertirse en tarjeta FSRS. */
public data class VocabularyPair(
    val source: String,
    val target: String,
    /** Nota cultural del modismo/colocación B2 (v2); `null` en vocabulario plano. */
    val culturalNote: String? = null,
)

/**
 * Extrae vocabulario revisable del contenido (ADR-016): pares
 * explícitos palabra↔significado — MatchPairs, KanaChoose (kana→romaji) y
 * KanjiReading (kanji→lectura) — más los modismos y colocaciones B2 de
 * `culturalNotes` (idiom→traducción, con su nota cultural). Frases completas
 * no entran al repaso. Los modismos preceden a los ejercicios, de modo que
 * su versión enriquecida gana la deduplicación por palabra.
 */
public object VocabularyExtractor {
    public fun fromLesson(lesson: Lesson): List<VocabularyPair> {
        val pairs = mutableListOf<VocabularyPair>()
        for (cultural in lesson.culturalNotes) {
            pairs +=
                VocabularyPair(
                    source = cultural.idiom,
                    target = cultural.translation ?: cultural.note,
                    culturalNote = cultural.note.takeIf { cultural.translation != null },
                )
        }
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
            .map { pair ->
                VocabularyPair(
                    source = pair.source.trim(),
                    target = pair.target.trim(),
                    culturalNote = pair.culturalNote?.trim()?.takeIf { it.isNotEmpty() },
                )
            }.filter { pair -> pair.source.isNotBlank() && pair.target.isNotBlank() }
            .distinctBy { pair -> pair.source.lowercase() }
    }
}

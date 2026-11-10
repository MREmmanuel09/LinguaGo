package dev.linguago.core.domain.logic

import dev.linguago.core.domain.model.Exercise
import dev.linguago.core.domain.model.Language
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
 * `culturalNotes` (traducción ES → idiom, con su nota cultural). Frases completas
 * no entran al repaso. Los modismos preceden a los ejercicios, de modo que
 * su versión enriquecida gana la deduplicación por par.
 *
 *
 * [targetLanguage] normaliza las parejas de MatchPairs para que el frente sea
 * siempre el término en español y el reverso el término meta (ADR-045). El
 * contenido japonés declara algunas parejas en dirección contraria.
 */
public object VocabularyExtractor {
    public fun fromLesson(
        lesson: Lesson,
        targetLanguage: Language? = null,
    ): List<VocabularyPair> {
        val pairs = mutableListOf<VocabularyPair>()
        for (cultural in lesson.culturalNotes) {
            // Frente español → reverso meta (ADR-045): la nota cultural venía
            // en dirección meta→español y se normaliza aquí.
            pairs +=
                VocabularyPair(
                    source = cultural.translation ?: cultural.note,
                    target = cultural.idiom,
                    culturalNote = cultural.note.takeIf { cultural.translation != null },
                )
        }
        for (exercise in lesson.exercises) {
            when (exercise) {
                is Exercise.MatchPairs ->
                    exercise.pairs.forEach { pair ->
                        val leftIsTarget = targetLanguage == Language.JAPANESE && JAPANESE_SCRIPT.containsMatchIn(pair.left)
                        val rightIsTarget = JAPANESE_SCRIPT.containsMatchIn(pair.right)
                        if (leftIsTarget && !rightIsTarget) {
                            pairs += VocabularyPair(source = pair.right, target = pair.left)
                        } else {
                            pairs += VocabularyPair(pair.left, pair.right)
                        }
                    }
                is Exercise.KanaChoose ->
                    pairs += VocabularyPair(exercise.kana, exercise.correctRomaji)
                is Exercise.KanjiReading ->
                    pairs += VocabularyPair(exercise.kanji, exercise.correctReading)
                else -> Unit
            }
        }
        val seenPairKeys = mutableSetOf<String>()
        return pairs
            .map { pair ->
                VocabularyPair(
                    source = pair.source.trim(),
                    target = pair.target.trim(),
                    culturalNote = pair.culturalNote?.trim()?.takeIf { it.isNotEmpty() },
                )
            }.filter { pair -> pair.source.isNotBlank() && pair.target.isNotBlank() }
            // Deduplicación por par completo, sin depender de la dirección:
            // un modismo enriquecido y el mismo MatchPair en presencia inversa
            // son una sola tarjeta, pero dos sentidos distintos sobreviven.
            .filter { pair -> seenPairKeys.add(pair.key()) }
    }

    private fun VocabularyPair.key(): String = listOf(source.lowercase(), target.lowercase()).sorted().joinToString("|")

    private val JAPANESE_SCRIPT = Regex("[\\u3040-\\u30FF\\u3400-\\u4DBF\\u4E00-\\u9FFF]")
}

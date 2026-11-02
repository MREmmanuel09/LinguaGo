package dev.linguago.core.domain.logic

import dev.linguago.core.domain.model.Lesson

/**
 * Glosario de la lección para el long-press de palabras (ADR-040): fusiona el
 * glosario curado del contenido (una o varias traducciones por término), los
 * ejemplos de la micro-clase y el vocabulario revisable (pares de MatchPairs,
 * kana/kanji e idiomas). Las claves se normalizan (minúsculas y sin
 * puntuación de borde) para el lookup por palabra. Además del sentido
 * término→traducciones, se añade el reverso de las traducciones de una sola
 * palabra (español→inglés), para que el long-press también responda en los
 * enunciados y frases origen.
 */
public object LessonGlossary {
    /** Construye el glosario término→traducciones de una lección. */
    public fun fromLesson(lesson: Lesson): Map<String, List<String>> {
        val forward = LinkedHashMap<String, MutableList<String>>()
        val reversed = LinkedHashMap<String, MutableList<String>>()

        fun add(
            term: String,
            translation: String,
        ) {
            val key = normalize(term)
            val value = translation.trim()
            if (key.isEmpty() || value.isEmpty()) return
            val values = forward.getOrPut(key) { mutableListOf() }
            if (values.none { existing -> existing.equals(value, ignoreCase = true) }) {
                values += value
            }
            if (value.none { char -> char.isWhitespace() }) {
                val reverseKey = normalize(value)
                val reverseValues = reversed.getOrPut(reverseKey) { mutableListOf() }
                val original = term.trim()
                if (reverseValues.none { existing -> existing.equals(original, ignoreCase = true) }) {
                    reverseValues += original
                }
            }
        }

        lesson.glossary.forEach { entry ->
            entry.translations.forEach { translation -> add(entry.term, translation) }
        }
        lesson.microLesson
            ?.sections
            .orEmpty()
            .flatMap { section -> section.examples }
            .forEach { example -> add(example.target, example.source) }
        VocabularyExtractor.fromLesson(lesson).forEach { pair -> add(pair.target, pair.source) }

        val merged = LinkedHashMap<String, MutableList<String>>()
        forward.forEach { (key, values) -> merged[key] = values }
        reversed.forEach { (key, values) ->
            val existing = merged[key]
            if (existing == null) {
                merged[key] = values
            } else {
                values.forEach { value ->
                    if (existing.none { current -> current.equals(value, ignoreCase = true) }) {
                        existing += value
                    }
                }
            }
        }
        return merged.mapValues { (_, values) -> values.toList() }
    }

    /**
     * Busca las traducciones de [word]: coincidencia exacta normalizada y, si
     * no hay, un fallback de singular y de inflexión verbal simple
     * (houses → house, started → start, working → work).
     */
    public fun lookup(
        glossary: Map<String, List<String>>,
        word: String,
    ): List<String>? {
        val key = normalize(word)
        if (key.isEmpty()) return null
        glossary[key]?.let { return it }
        for (candidate in candidates(key)) {
            glossary[candidate]?.let { return it }
        }
        return null
    }

    private fun candidates(key: String): List<String> =
        buildList {
            if (key.endsWith("ies") && key.length > 3) add(key.dropLast(3) + "y")
            if (key.endsWith("es") && key.length > 2) add(key.dropLast(2))
            if (key.endsWith("s") && key.length > 1) add(key.dropLast(1))
            if (key.endsWith("ied") && key.length > 3) add(key.dropLast(3) + "y")
            if (key.endsWith("ed") && key.length > 2) {
                add(key.dropLast(2))
                add(key.dropLast(1))
            }
            if (key.endsWith("ing") && key.length > 3) {
                val stem = key.dropLast(3)
                add(stem)
                if (stem.length > 1 && stem.last() == stem[stem.length - 2]) {
                    add(stem.dropLast(1))
                }
            }
        }

    /**
     * Busca una frase exacta normalizada en el glosario (modismos y
     * colocaciones del contenido, p. ej. `play it by ear`).
     */
    public fun lookupExact(
        glossary: Map<String, List<String>>,
        phrase: String,
    ): List<String>? {
        val key = normalize(phrase)
        if (key.isEmpty()) return null
        return glossary[key]
    }

    /** Clave de búsqueda: minúsculas, sin puntuación de borde y espacios colapsados. */
    public fun normalize(term: String): String =
        term
            .lowercase()
            .trim()
            .trim { char -> char in TRIM_CHARS }
            .replace(WHITESPACE, " ")
            .trim()

    private const val TRIM_CHARS = ".,;:!?¡¿\"'()«»—…"
    private val WHITESPACE = Regex("\\s+")
}

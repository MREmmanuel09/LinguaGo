package dev.linguago.core.domain.logic

/**
 * Segmento de un pasaje `ClozeDragDrop` (v2, ADR-018): los `{{id}}` del
 * [Exercise.ClozeDragDrop.passage] se separan del texto plano para que la
 * UI renderice los huecos intercalados sin re-parsear en cada recomposición.
 */
public sealed interface ClozeSegment {
    /** Fragmento de texto entre huecos (incluye espacios tal cual). */
    public data class Text(
        public val value: String,
    ) : ClozeSegment

    /** Referencia `{{id}}` a un hueco del pasaje. */
    public data class BlankReference(
        public val id: String,
    ) : ClozeSegment
}

/**
 * Parser puro del pasaje de `ClozeDragDrop`: convierte el texto con
 * referencias `{{id}}` en la lista ordenada de segmentos (texto y huecos).
 * Nunca lanza; una referencia sin par de `}}` se deja como texto plano (el
 * validador de contenido ya garantiza la integridad).
 */
public object ClozePassageParser {
    private val BLANK_REFERENCE = Regex("\\{\\{([^{}]+)\\}\\}")

    /** Segmentos del pasaje en orden de aparición; vacío si el pasaje lo es. */
    public fun parse(passage: String): List<ClozeSegment> {
        val segments = mutableListOf<ClozeSegment>()
        var index = 0
        for (match in BLANK_REFERENCE.findAll(passage)) {
            if (match.range.first > index) {
                segments += ClozeSegment.Text(passage.substring(index, match.range.first))
            }
            segments += ClozeSegment.BlankReference(match.groupValues[1])
            index = match.range.last + 1
        }
        if (index < passage.length) {
            segments += ClozeSegment.Text(passage.substring(index))
        }
        return segments
    }
}

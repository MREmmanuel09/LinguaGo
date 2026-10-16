package dev.linguago.core.domain.model

/**
 * Idiomas soportados por LinguaGo (RFC §1.1: 4 cursos ES→EN/PT/FR/JA).
 */
public enum class Language(
    public val isoCode: String,
) {
    SPANISH("es"),
    ENGLISH("en"),
    PORTUGUESE("pt"),
    FRENCH("fr"),
    JAPANESE("ja"),
    ;

    public companion object {
        /** Resuelve el idioma desde su código ISO-639 si existe. */
        public fun fromIsoCode(isoCode: String): Language? = entries.firstOrNull { it.isoCode == isoCode }
    }
}

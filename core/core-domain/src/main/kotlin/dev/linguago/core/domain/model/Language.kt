package dev.linguago.core.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Idiomas soportados por LinguaGo (RFC §1.1: 4 cursos ES→EN/PT/FR/JA).
 * En JSON se serializa con el código ISO-639 (ADR-009).
 */
@Serializable
public enum class Language(
    public val isoCode: String,
) {
    @SerialName("es")
    SPANISH("es"),

    @SerialName("en")
    ENGLISH("en"),

    @SerialName("pt")
    PORTUGUESE("pt"),

    @SerialName("fr")
    FRENCH("fr"),

    @SerialName("ja")
    JAPANESE("ja"),
    ;

    public companion object {
        /** Resuelve el idioma desde su código ISO-639 si existe. */
        public fun fromIsoCode(isoCode: String): Language? = entries.firstOrNull { it.isoCode == isoCode }
    }
}

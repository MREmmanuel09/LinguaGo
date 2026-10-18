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

/**
 * Variante dialectal de destino declarada por un curso v2 (ADR-018).
 * Se serializa como etiqueta BCP-47; `null` significa la variante por
 * defecto del idioma (comportamiento de los cursos v1).
 */
@Serializable
public enum class CourseLocale(
    public val bcp47: String,
) {
    @SerialName("en-US")
    ENGLISH_US("en-US"),

    @SerialName("en-GB")
    ENGLISH_GB("en-GB"),

    @SerialName("pt-BR")
    PORTUGUESE_BR("pt-BR"),

    @SerialName("pt-PT")
    PORTUGUESE_PT("pt-PT"),

    @SerialName("fr-FR")
    FRENCH_FR("fr-FR"),

    @SerialName("ja-JP")
    JAPANESE_JP("ja-JP"),
    ;

    public companion object {
        /** Resuelve la variante desde su etiqueta BCP-47 si existe. */
        public fun fromBcp47(tag: String): CourseLocale? = entries.firstOrNull { it.bcp47 == tag }
    }
}

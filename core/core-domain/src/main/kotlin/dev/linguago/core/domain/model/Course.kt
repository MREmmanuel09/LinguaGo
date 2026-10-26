package dev.linguago.core.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Jerarquía inmutable del contenido pedagógico (RFC §3.1). Un Curso tiene
 * Niveles, cada Nivel Unidades, cada Unidad Lecciones, cada Lección
 * Ejercicios. La inmutabilidad hace el curso compartible entre pantallas
 * sin riesgos de mutación. Se serializa directamente desde el JSON de los
 * assets (ADR-009): JERARQUÍA JSON = JERARQUÍA DE DOMINIO.
 */
@Serializable
public data class Course(
    val id: CourseId,
    @SerialName("from") public val fromLanguage: Language,
    @SerialName("to") public val toLanguage: Language,
    val levels: List<Level>,
    /** Versión del formato de contenido (ADR-012); v1 por defecto. */
    val schemaVersion: Int = 1,
    /** Variante dialectal objetivo (ADR-018); `null` en cursos v1. */
    public val locale: CourseLocale? = null,
)

/** Nivel CEFR declarado por una lección (ADR-018); A1 si el JSON v1 no lo trae. */
@Serializable
public enum class CefrLevel {
    A1,
    A2,
    B1,
    B2,
}

/** Banda de frecuencia léxica dominante de una lección B1/B2 (ADR-018). */
@Serializable
public enum class VocabFrequencyBand {
    @SerialName("1000-2000")
    BAND_1000_2000,

    @SerialName("2000-3000")
    BAND_2000_3000,

    @SerialName("3000-5000")
    BAND_3000_5000,

    @SerialName("5000+")
    BAND_5000_PLUS,
}

/** Foco gramatical referenciable desde `feedbackOnError` y `FreeSpeakingPrompt`. */
@Serializable
public data class GrammarPoint(
    public val id: GrammarPointId,
    public val label: String,
    public val description: String? = null,
)

/** Modismo o referencia cultural con nota de uso (obligatorio en B1/B2). */
@Serializable
public data class CulturalNote(
    public val idiom: String,
    public val note: String,
    public val translation: String? = null,
)

/**
 * Micro-clase breve de gramática y reglas previa a los ejercicios (ADR-028).
 * No se califica, no consume vidas ni XP y siempre se puede saltar.
 */
@Serializable
public data class MicroLesson(
    public val title: String,
    public val goals: List<String> = emptyList(),
    public val sections: List<MicroLessonSection> = emptyList(),
)

/** Sección de la micro-clase con una regla/explicación y ejemplos opcionales. */
@Serializable
public data class MicroLessonSection(
    public val heading: String,
    public val body: String,
    public val examples: List<MicroLessonExample> = emptyList(),
)

/** Ejemplo bilingüe de la micro-clase; `note` aclara el uso. */
@Serializable
public data class MicroLessonExample(
    public val source: String,
    public val target: String,
    public val note: String? = null,
)

@Serializable
public data class Level(
    val id: LevelId,
    val name: String,
    val units: List<Unit>,
)

@Serializable
public data class Unit(
    val id: UnitId,
    val title: String,
    val lessons: List<Lesson>,
)

@Serializable
public data class Lesson(
    val id: LessonId,
    val title: String,
    val exercises: List<Exercise>,
    /** Nivel CEFR; A1 por defecto para contenido v1 (ADR-018). */
    public val cefrLevel: CefrLevel = CefrLevel.A1,
    /** Situación comunicativa; obligatoria en B1/B2. */
    public val scenario: String? = null,
    /** Descriptor can-do CEFR; obligatorio en B1/B2. */
    public val canDo: String? = null,
    /** Focos gramaticales; obligatorios en B1/B2. */
    public val grammarPoints: List<GrammarPoint> = emptyList(),
    /** Banda de frecuencia léxica; obligatoria en B1/B2. */
    public val vocabFrequencyBand: VocabFrequencyBand? = null,
    /** Modismos con nota cultural; obligatorios en B1/B2. */
    public val culturalNotes: List<CulturalNote> = emptyList(),
    /** Micro-clase opcional de gramática/reglas (ADR-028). */
    public val microLesson: MicroLesson? = null,
)

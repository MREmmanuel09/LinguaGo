package dev.linguago.core.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Vista ligera de un curso para listas de selección (onboarding, home):
 * omite niveles/unidades/lecciones y solo describe la identidad y el par
 * de idiomas objetivo.
 */
@Serializable
public data class CourseSummary(
    val id: CourseId,
    @SerialName("from") public val fromLanguage: Language,
    @SerialName("to") public val toLanguage: Language,
    public val title: String,
)

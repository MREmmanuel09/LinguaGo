package dev.linguago.feature.lesson

import dev.linguago.core.domain.model.Lesson

/** Ejemplo bilingüe de la micro-clase, listo para pintar. */
data class LessonBriefExampleUi(
    val source: String,
    val target: String,
    val note: String? = null,
)

/** Sección de reglas/ejemplos de la micro-clase. */
data class LessonBriefSectionUi(
    val heading: String,
    val body: String,
    val examples: List<LessonBriefExampleUi> = emptyList(),
)

/** Contenido completo de la micro-clase previa a los ejercicios. */
data class LessonBriefUi(
    val title: String,
    val goals: List<String>,
    val sections: List<LessonBriefSectionUi>,
)

/**
 * Construye la micro-clase de una lección (ADR-028, ADR-036): usa el contenido
 * `microLesson` si existe y, si no, lo deriva de `scenario`, `canDo`,
 * `grammarPoints` y `culturalNotes`. El brief explica el tema; las mecánicas
 * de la interfaz ya no forman parte de esta pantalla.
 */
object LessonBriefBuilder {
    fun build(lesson: Lesson): LessonBriefUi =
        LessonBriefUi(
            title = lesson.microLesson?.title?.takeIf { it.isNotBlank() } ?: lesson.title,
            goals = goals(lesson),
            sections = sections(lesson),
        )

    private fun goals(lesson: Lesson): List<String> {
        val authored =
            lesson.microLesson
                ?.goals
                .orEmpty()
                .filter { it.isNotBlank() }
        if (authored.isNotEmpty()) return authored
        lesson.canDo?.takeIf { it.isNotBlank() }?.let { return listOf(it) }
        val labels = lesson.grammarPoints.map { it.label }.filter { it.isNotBlank() }
        if (labels.isNotEmpty()) return listOf("Repasa: ${labels.joinToString(", ")}.")
        return listOf("Practicar «${lesson.title}» con ejercicios cortos.")
    }

    private fun sections(lesson: Lesson): List<LessonBriefSectionUi> {
        val authored =
            lesson.microLesson
                ?.sections
                .orEmpty()
                .map { section ->
                    LessonBriefSectionUi(
                        heading = section.heading,
                        body = section.body,
                        examples =
                            section.examples.map { example ->
                                LessonBriefExampleUi(
                                    source = example.source,
                                    target = example.target,
                                    note = example.note,
                                )
                            },
                    )
                }
        if (authored.isNotEmpty()) return authored
        return fallbackSections(lesson)
    }

    private fun fallbackSections(lesson: Lesson): List<LessonBriefSectionUi> {
        val sections = mutableListOf<LessonBriefSectionUi>()
        lesson.scenario?.takeIf { it.isNotBlank() }?.let { scenario ->
            sections += LessonBriefSectionUi(heading = "Situación", body = scenario)
        }
        for (point in lesson.grammarPoints) {
            if (point.label.isBlank()) continue
            sections +=
                LessonBriefSectionUi(
                    heading = "Regla: ${point.label}",
                    body =
                        point.description?.takeIf { it.isNotBlank() }
                            ?: "Fíjate en esta regla al resolver los ejercicios.",
                )
        }
        val cultural =
            lesson.culturalNotes.filter { note -> note.idiom.isNotBlank() && note.note.isNotBlank() }
        if (cultural.isNotEmpty()) {
            sections +=
                LessonBriefSectionUi(
                    heading = "Cultura",
                    body = cultural.joinToString("\n") { note -> "${note.idiom}: ${note.note}" },
                )
        }
        if (sections.isNotEmpty()) return sections
        return listOf(
            LessonBriefSectionUi(
                heading = "Tema de la lección",
                body =
                    "Esta lección se centra en «${lesson.title}» dentro del nivel " +
                        "${lesson.cefrLevel}. Observa el vocabulario y las estructuras de los ejemplos " +
                        "antes de practicar.",
            ),
        )
    }
}

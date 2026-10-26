package dev.linguago.feature.lesson

import dev.linguago.core.domain.model.Exercise
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

/** Cápsula de mecánica para un tipo de ejercicio presente en la lección. */
data class MechanicHint(
    val title: String,
    val description: String,
)

/** Contenido completo de la micro-clase previa a los ejercicios. */
data class LessonBriefUi(
    val title: String,
    val goals: List<String>,
    val sections: List<LessonBriefSectionUi>,
    val mechanics: List<MechanicHint>,
)

/**
 * Construye la micro-clase de una lección (ADR-028): usa el contenido
 * `microLesson` si existe y, si no, lo deriva de `scenario`, `canDo`,
 * `grammarPoints` y `culturalNotes`. Las mecánicas se generan a partir de
 * los tipos de ejercicio presentes, sin revelar respuestas.
 */
object LessonBriefBuilder {
    fun build(lesson: Lesson): LessonBriefUi =
        LessonBriefUi(
            title = lesson.microLesson?.title?.takeIf { it.isNotBlank() } ?: lesson.title,
            goals = goals(lesson),
            sections = sections(lesson),
            mechanics = mechanics(lesson.exercises),
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
                heading = "En esta lección",
                body =
                    "Vas a practicar «${lesson.title}» con una mezcla de ejercicios. " +
                        "Lee cada enunciado y usa las pistas si fallas.",
            ),
        )
    }

    private fun mechanics(exercises: List<Exercise>): List<MechanicHint> = exercises.mapNotNull(::mechanicFor).distinctBy { it.title }

    private fun mechanicFor(exercise: Exercise): MechanicHint =
        when (exercise) {
            is Exercise.MultipleChoice,
            is Exercise.KanaChoose,
            is Exercise.KanjiReading,
            is Exercise.ParticleFill,
            ->
                MechanicHint(
                    title = "Elegir la opción",
                    description = "Toca la opción que responde al enunciado. Si fallas, verás la explicación y la respuesta.",
                )
            is Exercise.ListenChoose,
            is Exercise.AudioOnlyPrompt,
            ->
                MechanicHint(
                    title = "Escuchar y elegir",
                    description =
                        "Escucha el audio, elige la respuesta y repítelo si lo necesitas; " +
                            "también puedes escucharlo más lento.",
                )
            is Exercise.ListenType ->
                MechanicHint(
                    title = "Dictado",
                    description = "Escucha y escribe lo que oíste. Puedes repetir el audio; si fallas, corrige y reintenta.",
                )
            is Exercise.SpeakRepeat ->
                MechanicHint(
                    title = "Repetir en voz alta",
                    description = "Escucha el modelo, graba tu voz y compárala. Si fallas, vuelve a grabar.",
                )
            is Exercise.Translate ->
                MechanicHint(
                    title = "Traducir con palabras",
                    description = "Toca las palabras en orden para construir la traducción. Puedes quitar una tocándola de nuevo.",
                )
            is Exercise.OrderWords ->
                MechanicHint(
                    title = "Ordenar la frase",
                    description =
                        "Construye la frase en horizontal tocando las palabras en orden. " +
                            "Puedes quitar una tocándola de nuevo.",
                )
            is Exercise.MatchPairs ->
                MechanicHint(
                    title = "Emparejar",
                    description =
                        "Arrastra o toca una pieza izquierda y su pareja: encajan como un rompecabezas. " +
                            "Puedes separar una pareja tocándola.",
                )
            is Exercise.SemanticMatch ->
                MechanicHint(
                    title = "Emparejar con matices",
                    description =
                        "Une cada expresión con su función o matiz. Fíjate en el registro y en la intención; " +
                            "también puedes separar una pareja.",
                )
            is Exercise.ClozeDragDrop ->
                MechanicHint(
                    title = "Completar huecos",
                    description = "Arrastra o toca cada palabra y colócala en su hueco.",
                )
            is Exercise.FreeSpeakingPrompt ->
                MechanicHint(
                    title = "Hablar libremente",
                    description = "Graba tu respuesta, valora los criterios y compárala con la respuesta modelo.",
                )
        }
}

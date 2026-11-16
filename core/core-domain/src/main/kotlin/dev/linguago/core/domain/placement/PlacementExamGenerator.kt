package dev.linguago.core.domain.placement

import dev.linguago.core.domain.model.CefrLevel
import dev.linguago.core.domain.model.Course
import dev.linguago.core.domain.model.Exercise
import kotlin.random.Random

/**
 * Genera exámenes de nivelación dinámicos a partir del contenido empaquetado
 * (ADR-058). Selecciona ejercicios auto-evaluables por nivel CEFR en orden
 * fácil→difícil; el muestreo aleatorio hace que cada intento sea distinto.
 */
public object PlacementExamGenerator {
    /** Tipos aptos para examen objetivo sin STT/audio obligatorio. */
    public val DEFAULT_SUPPORTED_TYPES: Set<String> =
        setOf(
            "MultipleChoice",
            "Translate",
            "MatchPairs",
            "OrderWords",
            "KanaChoose",
            "KanjiReading",
            "ParticleFill",
            "SemanticMatch",
            "ClozeDragDrop",
        )

    public fun isSupported(exercise: Exercise): Boolean = exercise::class.simpleName in DEFAULT_SUPPORTED_TYPES

    /**
     * Construye un examen para [course]. [questionsPerLevel] limita ítems por
     * bloque CEFR (si hay menos candidatos, usa los disponibles). [random]
     * permite inyectar semilla para pruebas o dejarlo no determinista.
     */
    public fun generate(
        course: Course,
        questionsPerLevel: Int = 4,
        random: Random = Random.Default,
    ): PlacementExam {
        val perLevel = questionsPerLevel.coerceAtLeast(1)
        val candidatesByLevel = linkedMapOf<CefrLevel, MutableList<PlacementQuestion>>()
        CefrLevel.entries.forEach { candidatesByLevel[it] = mutableListOf() }

        for (level in course.levels) {
            for (unit in level.units) {
                for (lesson in unit.lessons) {
                    val bucket = candidatesByLevel[lesson.cefrLevel] ?: continue
                    lesson.exercises
                        .asSequence()
                        .filter(::isSupported)
                        .forEach { exercise ->
                            bucket +=
                                PlacementQuestion(
                                    lessonId = lesson.id,
                                    cefrLevel = lesson.cefrLevel,
                                    exercise = exercise,
                                )
                        }
                }
            }
        }

        val blocks =
            CefrLevel.entries.mapNotNull { cefr ->
                val pool = candidatesByLevel[cefr].orEmpty()
                if (pool.isEmpty()) return@mapNotNull null
                val picked = pool.shuffled(random).take(perLevel)
                PlacementLevelBlock(level = cefr, questions = picked)
            }

        return PlacementExam(courseId = course.id.value, blocks = blocks)
    }

    /**
     * IDs de lecciones del [course] cuyo CEFR es <= [achievedLevel]. Se usa
     * para desbloquear de forma monótona (ADR-058, I4).
     */
    public fun lessonsUpTo(
        course: Course,
        achievedLevel: CefrLevel,
    ): List<dev.linguago.core.domain.model.LessonId> {
        val maxOrdinal = achievedLevel.ordinal
        return course.levels
            .asSequence()
            .flatMap { level -> level.units.asSequence() }
            .flatMap { unit -> unit.lessons.asSequence() }
            .filter { lesson -> lesson.cefrLevel.ordinal <= maxOrdinal }
            .map { lesson -> lesson.id }
            .toList()
    }
}

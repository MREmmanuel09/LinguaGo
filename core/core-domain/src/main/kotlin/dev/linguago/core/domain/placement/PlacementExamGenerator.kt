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
    /** Mínimo de opciones en ejercicios de selección (el contenido suele traer 3). */
    private const val MIN_OPTIONS: Int = 5

    public fun isSupported(exercise: Exercise): Boolean =
        when (exercise) {
            is Exercise.MultipleChoice,
            is Exercise.ListenChoose,
            is Exercise.AudioOnlyPrompt,
            is Exercise.ListenType,
            is Exercise.Translate,
            is Exercise.MatchPairs,
            is Exercise.OrderWords,
            is Exercise.KanaChoose,
            is Exercise.KanjiReading,
            is Exercise.ParticleFill,
            is Exercise.SemanticMatch,
            is Exercise.ClozeDragDrop,
            -> true
            else -> false
        }

    /**
     * Construye un examen para [course]. [questionsPerLevel] limita ítems por
     * bloque CEFR (si hay menos candidatos, usa los disponibles). [random]
     * permite inyectar semilla para pruebas o dejarlo no determinista.
     */
    public fun generate(
        course: Course,
        questionsPerLevel: Int = 10,
        random: Random = Random.Default,
        supported: (Exercise) -> Boolean = ::isSupported,
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
                        .filter(supported)
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
                val sameLevel = pool.map { it.exercise }
                val allExercises = candidatesByLevel.values.flatMap { list -> list.map { it.exercise } }
                val picked =
                    pickForLevel(pool, perLevel, random).map { question ->
                        question.copy(
                            exercise =
                                expandOptions(
                                    exercise = question.exercise,
                                    sameLevel = sameLevel,
                                    all = allExercises,
                                    random = random,
                                ),
                        )
                    }
                PlacementLevelBlock(level = cefr, questions = picked)
            }

        return PlacementExam(courseId = course.id.value, blocks = blocks)
    }

    /**
     * Elige hasta [limit] preguntas garantizando variedad de mecánicas: primero
     * una de cada tipo disponible (escucha, dictado, elección, tokens, parejas,
     * cloze) y después el resto al azar. Así un intento nunca queda formado
     * solo por ejercicios de opción múltiple.
     */
    private fun pickForLevel(
        pool: List<PlacementQuestion>,
        limit: Int,
        random: Random,
    ): List<PlacementQuestion> {
        val picked = mutableListOf<PlacementQuestion>()
        pool
            .groupBy { mechanicOf(it.exercise) }
            .entries
            .shuffled(random)
            .forEach { (_, candidates) ->
                if (picked.size >= limit) return@forEach
                candidates.shuffled(random).firstOrNull()?.let { picked += it }
            }
        if (picked.size < limit) {
            val chosen = picked.toSet()
            picked += pool.filterNot { it in chosen }.shuffled(random).take(limit - picked.size)
        }
        return picked.shuffled(random)
    }

    private fun mechanicOf(exercise: Exercise): String =
        when (exercise) {
            is Exercise.ListenChoose,
            is Exercise.AudioOnlyPrompt,
            -> "listen"
            is Exercise.ListenType -> "dictation"
            is Exercise.MultipleChoice,
            is Exercise.KanaChoose,
            is Exercise.KanjiReading,
            is Exercise.ParticleFill,
            -> "choice"
            is Exercise.Translate,
            is Exercise.OrderWords,
            -> "tokens"
            is Exercise.MatchPairs,
            is Exercise.SemanticMatch,
            -> "pairs"
            is Exercise.ClozeDragDrop -> "cloze"
            else -> "other"
        }

    /**
     * Completa ejercicios de opción múltiple hasta [MIN_OPTIONS] con opciones
     * de otros ejercicios del mismo nivel (y del resto del curso como
     * respaldo). La respuesta correcta y su índice no cambian: las opciones
     * extra se añaden al final y la UI las baraja.
     */
    private fun expandOptions(
        exercise: Exercise,
        sameLevel: List<Exercise>,
        all: List<Exercise>,
        random: Random,
    ): Exercise {
        val missing = MIN_OPTIONS - optionCount(exercise)
        if (missing <= 0) return exercise
        val existing = optionsOf(exercise).toSet()
        val correct = correctOption(exercise)
        val extras =
            (optionCandidates(exercise, sameLevel) + optionCandidates(exercise, all))
                .asSequence()
                .filter { it.isNotBlank() && it !in existing && it != correct }
                .distinct()
                .shuffled(random)
                .take(missing)
                .toList()
        if (extras.isEmpty()) return exercise
        return when (exercise) {
            is Exercise.MultipleChoice -> exercise.copy(options = exercise.options + extras)
            is Exercise.ListenChoose -> exercise.copy(options = exercise.options + extras)
            is Exercise.AudioOnlyPrompt -> exercise.copy(options = exercise.options + extras)
            is Exercise.KanaChoose -> exercise.copy(romajiOptions = exercise.romajiOptions + extras)
            is Exercise.KanjiReading -> exercise.copy(readingOptions = exercise.readingOptions + extras)
            is Exercise.ParticleFill -> exercise.copy(particles = exercise.particles + extras)
            else -> exercise
        }
    }

    private fun optionCount(exercise: Exercise): Int = optionsOf(exercise).size

    private fun optionsOf(exercise: Exercise): List<String> =
        when (exercise) {
            is Exercise.MultipleChoice -> exercise.options
            is Exercise.ListenChoose -> exercise.options
            is Exercise.AudioOnlyPrompt -> exercise.options
            is Exercise.KanaChoose -> exercise.romajiOptions
            is Exercise.KanjiReading -> exercise.readingOptions
            is Exercise.ParticleFill -> exercise.particles
            else -> emptyList()
        }

    private fun correctOption(exercise: Exercise): String? =
        when (exercise) {
            is Exercise.MultipleChoice -> exercise.options.getOrNull(exercise.correctIndex)
            is Exercise.ListenChoose -> exercise.options.getOrNull(exercise.correctIndex)
            is Exercise.AudioOnlyPrompt -> exercise.options.getOrNull(exercise.correctIndex)
            is Exercise.KanaChoose -> exercise.correctRomaji
            is Exercise.KanjiReading -> exercise.correctReading
            is Exercise.ParticleFill -> exercise.correctParticle
            else -> null
        }

    private fun optionCandidates(
        exercise: Exercise,
        pool: List<Exercise>,
    ): List<String> =
        when (exercise) {
            is Exercise.MultipleChoice -> pool.filterIsInstance<Exercise.MultipleChoice>().flatMap { it.options }
            is Exercise.ListenChoose -> pool.filterIsInstance<Exercise.ListenChoose>().flatMap { it.options }
            is Exercise.AudioOnlyPrompt -> pool.filterIsInstance<Exercise.AudioOnlyPrompt>().flatMap { it.options }
            is Exercise.KanaChoose -> pool.filterIsInstance<Exercise.KanaChoose>().flatMap { it.romajiOptions }
            is Exercise.KanjiReading -> pool.filterIsInstance<Exercise.KanjiReading>().flatMap { it.readingOptions }
            is Exercise.ParticleFill -> pool.filterIsInstance<Exercise.ParticleFill>().flatMap { it.particles }
            else -> emptyList()
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

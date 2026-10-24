package dev.linguago.qa

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/** Tipos de ejercicio que el gate sabe conducir. */
internal enum class ExerciseKind {
    CLOZE,
    SEMANTIC_MATCH,
    AUDIO_ONLY,
    MULTIPLE_CHOICE,
    LISTEN_TYPE,
    ORDER_WORDS,
    SPEAK_REPEAT,
    FREE_SPEAKING,
    UNKNOWN,
}

/** Ejercicio esperado, con los datos para responder correctamente (o no). */
internal data class ExpectedExercise(
    val type: String,
    val kind: ExerciseKind,
    /**
     * Prompt visible en pantalla (MultipleChoice no tiene ningún string fijo
     * de UI: su cabecera es la `question` del contenido; el gate lo usa como
     * marcador de detección de tipo).
     */
    val prompt: String = "",
    val clozeAnswers: List<String> = emptyList(),
    val clozeDistractors: List<List<String>> = emptyList(),
    val options: List<String> = emptyList(),
    val correctIndex: Int = -1,
    val transcription: String = "",
    val answerTokens: List<String> = emptyList(),
    val pairLefts: List<String> = emptyList(),
    val pairRights: List<String> = emptyList(),
    val criteriaCount: Int = 0,
    val allowSlowPlayback: Boolean = false,
)

/**
 * Respuestas del curso B2 leídas del propio `course.json` empaquetado. Así el
 * gate avanza sin depender de texto visible (robustez ante cambios de UI) y
 * puede responder correcto/incorrecto de forma determinista.
 */
internal class CourseAnswers(
    private val exercises: List<ExpectedExercise>,
) {
    fun exerciseAt(index: Int): ExpectedExercise? = exercises.getOrNull(index)

    /**
     * La app reordena su cola de ejercicios al reanudar una lección tras
     * perder la actividad (perturbaciones R8/expulsivas), así que el índice
     * del curso es solo una línea base. Índices de todos los ejercicios del
     * tipo indicado, en orden de curso.
     */
    fun indicesWithKind(kind: ExerciseKind): List<Int> =
        exercises.withIndex().filter { (_, exercise) -> exercise.kind == kind }.map { it.index }

    fun size(): Int = exercises.size

    companion object {
        private const val ASSET = "courses/es-en/course.json"

        fun load(
            context: Context,
            lessonTitle: String,
        ): CourseAnswers {
            val json =
                context.assets
                    .open(ASSET)
                    .bufferedReader()
                    .use { it.readText() }
            val root = JSONObject(json)
            val levels = root.getJSONArray("levels")
            for (levelIndex in 0 until levels.length()) {
                val units = levels.getJSONObject(levelIndex).getJSONArray("units")
                for (unitIndex in 0 until units.length()) {
                    val lessons = units.getJSONObject(unitIndex).getJSONArray("lessons")
                    for (lessonIndex in 0 until lessons.length()) {
                        val lesson = lessons.getJSONObject(lessonIndex)
                        if (lesson.optString("title") != lessonTitle) continue
                        return CourseAnswers(parseExercises(lesson.getJSONArray("exercises")))
                    }
                }
            }
            error("Lección no encontrada en $ASSET: $lessonTitle")
        }

        private fun parseExercises(array: JSONArray): List<ExpectedExercise> =
            (0 until array.length()).map { index -> parseExercise(array.getJSONObject(index)) }

        private fun parseExercise(json: JSONObject): ExpectedExercise {
            val type = json.optString("type")
            val options = json.optJSONArray("options").toStringList()
            val correctIndex = json.optInt("correctIndex", -1)
            return when (type) {
                "ClozeDragDrop" -> {
                    val blanks = json.getJSONArray("blanks")
                    val answers = mutableListOf<String>()
                    val distractors = mutableListOf<List<String>>()
                    for (blankIndex in 0 until blanks.length()) {
                        val blank = blanks.getJSONObject(blankIndex)
                        val answer = blank.optString("answer")
                        answers += answer
                        distractors += blank.optJSONArray("options").toStringList().filter { it != answer }
                    }
                    ExpectedExercise(
                        type = type,
                        kind = ExerciseKind.CLOZE,
                        clozeAnswers = answers,
                        clozeDistractors = distractors,
                    )
                }
                "SemanticMatch" -> {
                    val pairs = json.getJSONArray("pairs")
                    val lefts = mutableListOf<String>()
                    val rights = mutableListOf<String>()
                    for (pairIndex in 0 until pairs.length()) {
                        lefts += pairs.getJSONObject(pairIndex).optString("left")
                        rights += pairs.getJSONObject(pairIndex).optString("right")
                    }
                    ExpectedExercise(
                        type = type,
                        kind = ExerciseKind.SEMANTIC_MATCH,
                        pairLefts = lefts,
                        pairRights = rights,
                    )
                }
                "AudioOnlyPrompt" ->
                    ExpectedExercise(
                        type = type,
                        kind = ExerciseKind.AUDIO_ONLY,
                        options = options,
                        correctIndex = correctIndex,
                        allowSlowPlayback = json.optBoolean("allowSlowPlayback", false),
                    )
                "MultipleChoice" ->
                    ExpectedExercise(
                        type = type,
                        kind = ExerciseKind.MULTIPLE_CHOICE,
                        prompt = json.optString("question"),
                        options = options,
                        correctIndex = correctIndex,
                    )
                "ListenType" ->
                    ExpectedExercise(
                        type = type,
                        kind = ExerciseKind.LISTEN_TYPE,
                        transcription = json.optString("transcription"),
                    )
                "OrderWords" ->
                    ExpectedExercise(
                        type = type,
                        kind = ExerciseKind.ORDER_WORDS,
                        answerTokens = json.optJSONArray("answerTokens").toStringList(),
                    )
                "SpeakRepeat" -> ExpectedExercise(type = type, kind = ExerciseKind.SPEAK_REPEAT)
                "FreeSpeakingPrompt" ->
                    ExpectedExercise(
                        type = type,
                        kind = ExerciseKind.FREE_SPEAKING,
                        criteriaCount = json.optJSONArray("selfAssessmentCriteria").toStringList().size,
                    )
                else -> ExpectedExercise(type = type, kind = ExerciseKind.UNKNOWN)
            }
        }

        private fun JSONArray?.toStringList(): List<String> {
            if (this == null) return emptyList()
            return (0 until length()).map { optString(it) }
        }
    }
}

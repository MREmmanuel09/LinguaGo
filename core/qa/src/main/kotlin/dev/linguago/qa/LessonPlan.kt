package dev.linguago.qa

/**
 * Plan de escenarios del gate. Las perturbaciones se reparten entre lecciones
 * para cubrir todos los puntos débiles sin parar la sesión.
 */
internal enum class Perturbation {
    NONE,
    ROTATE,
    BACKGROUND_DURING_STT,
    DOUBLE_TAP_CONTINUE,
    NOTIFICATION_DURING_RECORDING,
    TRIM_MEMORY,
    DONT_KEEP_ACTIVITIES,
    CANCEL_STT,
    CANCEL_TTS,
}

/** Escenarios de una lección: perturbación por ejercicio + flags de lección. */
internal data class LessonScenario(
    val lessonIndex: Int,
    val title: String,
    val exercisePerturbations: Map<Int, Perturbation>,
    val fontScale2x: Boolean = false,
    val darkMode: Boolean = false,
    val localeChange: Boolean = false,
)

internal object LessonPlan {
    /**
     * Las 5 primeras lecciones del Camino (A1). El camino lineal de ADR-026
     * bloquea B2 hasta completar el recorrido, así que el gate conduce las
     * primeras lecciones disponibles; siguen siendo contenido real validado.
     */
    val lessonTitles: List<String> =
        listOf(
            "Saludos",
            "Presentarse",
            "La familia",
            "Comida y bebida",
            "Colores y números",
        )

    fun titleFor(lessonIndex: Int): String = lessonTitles[lessonIndex % lessonTitles.size]

    fun scenarioFor(lessonIndex: Int): LessonScenario {
        val title = titleFor(lessonIndex)
        val perturbations =
            when (lessonIndex) {
                // A1 tiene 7 ejercicios: las perturbaciones caen en índices válidos.
                0 -> mapOf(2 to Perturbation.ROTATE)
                1 -> mapOf(6 to Perturbation.BACKGROUND_DURING_STT)
                2 -> mapOf(3 to Perturbation.DOUBLE_TAP_CONTINUE)
                3 -> mapOf(6 to Perturbation.NOTIFICATION_DURING_RECORDING)
                4 -> mapOf(0 to Perturbation.TRIM_MEMORY)
                5 -> mapOf(6 to Perturbation.CANCEL_STT)
                6 -> mapOf(4 to Perturbation.CANCEL_TTS)
                else -> emptyMap()
            }
        return LessonScenario(
            lessonIndex = lessonIndex,
            title = title,
            exercisePerturbations = perturbations,
            fontScale2x = lessonIndex == 7,
            darkMode = lessonIndex == 8,
            localeChange = lessonIndex == 9,
        )
    }
}

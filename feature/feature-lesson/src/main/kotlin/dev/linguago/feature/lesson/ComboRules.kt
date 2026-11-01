package dev.linguago.feature.lesson

/** Reglas de racha de aciertos de la sesión de lección (ADR-033). */
internal object ComboRules {
    /** Siguiente valor del combo: suma en acierto y reinicia en error. */
    fun next(
        current: Int,
        isCorrect: Boolean,
    ): Int = if (isCorrect) current + 1 else 0
}

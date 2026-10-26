package dev.linguago.core.ui.feedback

/**
 * Eventos sensoriales de LinguaGo (ADR-027). Cada cue combina un sonido corto
 * y un patrón háptico; nunca es la única señal de estado (I8).
 */
public enum class FeedbackCue {
    /** Navegación o botón neutro. */
    TAP,

    /** Selección de una opción, token o pieza. */
    SELECT,

    /** Retirada de una selección. */
    DESELECT,

    /** Encaje de un token o de una pareja. */
    SNAP,

    /** Respuesta correcta. */
    CORRECT,

    /** Respuesta incorrecta. */
    INCORRECT,

    /** Lección o unidad completada. */
    COMPLETE,
}

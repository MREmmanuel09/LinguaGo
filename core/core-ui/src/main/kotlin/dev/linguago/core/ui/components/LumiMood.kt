package dev.linguago.core.ui.components

/** Emoción de Lumi (ADR-032); cada una tiene su propio perfil de movimiento. */
public enum class LumiMood {
    /** Flote suave junto al nodo disponible. */
    IDLE,

    /** Rebote con destellos en victorias y aciertos. */
    HAPPY,

    /** Balanceo empático cuando el alumno está en progreso o reintenta. */
    ENCOURAGING,

    /** Pulso atento mientras se escucha al alumno. */
    LISTENING,
}

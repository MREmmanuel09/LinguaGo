package dev.linguago.core.audio

/**
 * Máquina de estados del warmup de un motor audio nativo (ADR-020). La UI
 * observa [TtsEngine.stateFor]/[SttEngine.stateFor] para mostrar el progreso
 * sin que `isReadyFor` toque disco: todas las transiciones ocurren en
 * `Dispatchers.IO` con Mutex por idioma.
 */
public sealed interface AudioEngineState {
    /** Todavía no se solicitó warmup para el idioma. */
    public data object Idle : AudioEngineState

    /** Leyendo el manifest y verificando SHA-256 de lo ya instalado. */
    public data object Verifying : AudioEngineState

    /** Extrayendo assets a un staging (rename atómico al publicar). */
    public data object Extracting : AudioEngineState

    /** Instanciando el motor nativo (JNI/JNA). */
    public data object Loading : AudioEngineState

    /** Motor listo para usarse sin I/O adicional. */
    public data object Ready : AudioEngineState

    /** Sin motor utilizable para el idioma; la app degrada (I3). */
    public data class Degraded(
        public val reason: String,
    ) : AudioEngineState
}

/**
 * `true` mientras el motor prepara sus modelos (verificación, extracción o
 * carga). La UI lo usa para avisar sin bloquear el avance de la lección (I3).
 */
public val AudioEngineState.isWarmingUp: Boolean
    get() =
        when (this) {
            AudioEngineState.Verifying,
            AudioEngineState.Extracting,
            AudioEngineState.Loading,
            -> true
            AudioEngineState.Idle,
            AudioEngineState.Ready,
            is AudioEngineState.Degraded,
            -> false
        }

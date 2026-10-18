package dev.linguago.core.ui.haptics

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import kotlinx.coroutines.delay

/** Separación entre los dos pulsos del error (ms). */
private const val ERROR_PULSE_GAP_MS = 80L

/**
 * Feedback háptico semántico de LinguaGo (F2.1): confirmación al acertar,
 * doble pulso al fallar y tick ligero al acoplar tokens o parejas. No toca
 * el modelo de sonido: delega en `View.performHapticFeedback`, que respeta
 * los ajustes de háptica del sistema.
 */
public class LinguaHaptics internal constructor(
    private val hapticFeedback: HapticFeedback,
) {
    /** Pulso único de confirmación (respuesta correcta). */
    public fun confirm() {
        hapticFeedback.performHapticFeedback(HapticFeedbackType.Confirm)
    }

    /** Tick ligero al acoplar un token o una pareja. */
    public fun tick() {
        hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
    }

    /** Doble pulso de error, separado por un intervalo breve. */
    public suspend fun error() {
        hapticFeedback.performHapticFeedback(HapticFeedbackType.Reject)
        delay(ERROR_PULSE_GAP_MS)
        hapticFeedback.performHapticFeedback(HapticFeedbackType.Reject)
    }
}

/** Recuerda un [LinguaHaptics] ligado al feedback háptico del dispositivo. */
@Composable
public fun rememberLinguaHaptics(): LinguaHaptics {
    val hapticFeedback = LocalHapticFeedback.current
    return remember(hapticFeedback) { LinguaHaptics(hapticFeedback) }
}

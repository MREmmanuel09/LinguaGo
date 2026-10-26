package dev.linguago.core.ui.haptics

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import dev.linguago.core.ui.feedback.FeedbackCue
import dev.linguago.core.ui.feedback.HapticFeedbackPlayer

/**
 * Feedback háptico semántico de LinguaGo (ADR-027): patrones cortos con
 * `VibrationEffect` y fallback a `performHapticFeedback` cuando el dispositivo
 * no tiene vibrador. Respeta el ajuste del sistema; el controlador de
 * `core-ui.feedback` aplica además el ajuste de la app.
 */
public class LinguaHaptics internal constructor(
    private val hapticFeedback: HapticFeedback,
    private val vibrator: Vibrator? = null,
    private val vibrationsAllowed: () -> Boolean = { true },
) : HapticFeedbackPlayer {
    /** Pulso único de confirmación (respuesta correcta). */
    public fun confirm() {
        emit(FeedbackCue.CORRECT)
    }

    /** Tick ligero al acoplar un token o una pareja. */
    public fun tick() {
        emit(FeedbackCue.SELECT)
    }

    /** Doble pulso de error. */
    public fun error() {
        emit(FeedbackCue.INCORRECT)
    }

    override fun play(cue: FeedbackCue) {
        emit(cue)
    }

    private fun emit(cue: FeedbackCue) {
        val device = vibrator
        if (device == null || !device.hasVibrator()) {
            fallback(cue)
            return
        }
        if (!vibrationsAllowed()) return
        val pattern = patternFor(cue)
        val effect =
            if (device.hasAmplitudeControl() && pattern.amplitudes.isNotEmpty()) {
                VibrationEffect.createWaveform(pattern.timings, pattern.amplitudes, NO_REPEAT)
            } else {
                VibrationEffect.createWaveform(pattern.timings, NO_REPEAT)
            }
        device.vibrate(effect)
    }

    private fun fallback(cue: FeedbackCue) {
        when (cue) {
            FeedbackCue.CORRECT -> hapticFeedback.performHapticFeedback(HapticFeedbackType.Confirm)
            FeedbackCue.INCORRECT -> {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.Reject)
                hapticFeedback.performHapticFeedback(HapticFeedbackType.Reject)
            }
            FeedbackCue.COMPLETE -> {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.Confirm)
                hapticFeedback.performHapticFeedback(HapticFeedbackType.Confirm)
            }
            FeedbackCue.TAP,
            FeedbackCue.SELECT,
            FeedbackCue.DESELECT,
            FeedbackCue.SNAP,
            -> hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
    }

    private fun patternFor(cue: FeedbackCue): HapticPattern =
        when (cue) {
            FeedbackCue.TAP -> HapticPattern(longArrayOf(0, 8), intArrayOf(0, 60))
            FeedbackCue.SELECT -> HapticPattern(longArrayOf(0, 10), intArrayOf(0, 80))
            FeedbackCue.DESELECT -> HapticPattern(longArrayOf(0, 8), intArrayOf(0, 60))
            FeedbackCue.SNAP ->
                HapticPattern(
                    timings = longArrayOf(0, 8, 16, 10),
                    amplitudes = intArrayOf(0, 120, 0, 160),
                )
            FeedbackCue.CORRECT ->
                HapticPattern(
                    timings = longArrayOf(0, 12, 28, 12, 28, 18),
                    amplitudes = intArrayOf(0, 100, 0, 140, 0, 200),
                )
            FeedbackCue.INCORRECT ->
                HapticPattern(
                    timings = longArrayOf(0, 25, 40, 25),
                    amplitudes = intArrayOf(0, 180, 0, 220),
                )
            FeedbackCue.COMPLETE ->
                HapticPattern(
                    timings = longArrayOf(0, 12, 24, 12, 24, 12, 40, 22),
                    amplitudes = intArrayOf(0, 110, 0, 150, 0, 190, 0, 230),
                )
        }

    private data class HapticPattern(
        val timings: LongArray,
        val amplitudes: IntArray,
    )

    private companion object {
        const val NO_REPEAT = -1
    }
}

/** Recuerda un [LinguaHaptics] ligado al vibrador y al ajuste del sistema. */
@Suppress("DEPRECATION")
@Composable
public fun rememberLinguaHaptics(): LinguaHaptics {
    val context = LocalContext.current
    val hapticFeedback = LocalHapticFeedback.current
    val vibrator = remember(context) { context.defaultVibrator() }
    val allowed =
        remember(context) {
            {
                runCatching {
                    Settings.System.getInt(
                        context.contentResolver,
                        Settings.System.HAPTIC_FEEDBACK_ENABLED,
                        1,
                    ) != 0
                }.getOrDefault(true)
            }
        }
    return remember(hapticFeedback, vibrator, allowed) {
        LinguaHaptics(
            hapticFeedback = hapticFeedback,
            vibrator = vibrator,
            vibrationsAllowed = allowed,
        )
    }
}

private fun Context.defaultVibrator(): Vibrator? =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        (getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager)?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        (getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator)
    }

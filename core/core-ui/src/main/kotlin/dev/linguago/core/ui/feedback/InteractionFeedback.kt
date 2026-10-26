package dev.linguago.core.ui.feedback

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext
import dev.linguago.core.ui.haptics.rememberLinguaHaptics

/** Receptor de un [FeedbackCue]; lo implementan sonido y háptica. */
public interface InteractionFeedback {
    /** Dispara el refuerzo sensorial del cue, si los ajustes lo permiten. */
    public fun play(cue: FeedbackCue)
}

/** Implementación neutra para previews y tests. */
public object NoOpInteractionFeedback : InteractionFeedback {
    override fun play(cue: FeedbackCue) = Unit
}

/** Controlador activo de feedback; el composition root provee uno real. */
public val LocalInteractionFeedback: ProvidableCompositionLocal<InteractionFeedback> =
    staticCompositionLocalOf { NoOpInteractionFeedback }

/** Reproductor de háptica desacoplado de Android para tests. */
public interface HapticFeedbackPlayer {
    /** Dispara el patrón del cue. */
    public fun play(cue: FeedbackCue)
}

/** Despacha sonido y háptica según [FeedbackSettings] (ADR-027). */
internal class DefaultInteractionFeedback(
    private val sounds: SoundEffectPlayer,
    private val haptics: HapticFeedbackPlayer,
    private val settings: FeedbackSettings,
) : InteractionFeedback {
    override fun play(cue: FeedbackCue) {
        if (settings.soundEffectsEnabled) {
            sounds.play(cue)
        }
        if (settings.hapticsEnabled) {
            haptics.play(cue)
        }
    }
}

/**
 * Recuerda el controlador de feedback. El `SoundPool` y el vibrador viven
 * mientras dure la composición y se liberan al salir.
 */
@Composable
public fun rememberInteractionFeedback(settings: FeedbackSettings = LocalFeedbackSettings.current): InteractionFeedback {
    val context = LocalContext.current
    val haptics = rememberLinguaHaptics()
    val soundPlayer =
        remember(context) {
            SoundPoolSoundEffectPlayer(context)
        }

    DisposableEffect(soundPlayer) {
        onDispose { soundPlayer.release() }
    }

    return remember(soundPlayer, haptics, settings) {
        DefaultInteractionFeedback(
            sounds = soundPlayer,
            haptics = haptics,
            settings = settings,
        )
    }
}

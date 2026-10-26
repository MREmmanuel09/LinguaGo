package dev.linguago.core.ui.feedback

import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * `clickable` con feedback sensorial (ADR-027). Los componentes compartidos
 * lo usan para no repetir el despacho de sonido/háptica en cada pantalla.
 */
@Composable
public fun Modifier.feedbackClickable(
    cue: FeedbackCue,
    enabled: Boolean = true,
    onClick: () -> Unit,
): Modifier {
    val feedback = LocalInteractionFeedback.current
    return clickable(enabled = enabled) {
        feedback.play(cue)
        onClick()
    }
}

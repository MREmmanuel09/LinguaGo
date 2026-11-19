package dev.linguago.core.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.linguago.core.ui.feedback.FeedbackCue
import dev.linguago.core.ui.feedback.feedbackClickable
import dev.linguago.core.ui.theme.LinguaMotion
import dev.linguago.core.ui.theme.LinguaTheme
import dev.linguago.core.ui.theme.linguaExtendedColors
import dev.linguago.core.ui.util.rememberAnimationsEnabled

/**
 * Botón de micrófono del prototipo «Warm Cream & Teal» (ADR-055): círculo
 * teal con anillos concéntricos tan que pulsan mientras escucha (bucle de
 * 1200 ms; sin animaciones del sistema quedan estáticos, I8). Objetivo
 * táctil de 96dp con núcleo de 72dp ≥ 48dp (I8) y cue `TAP` al activar.
 */
@Composable
public fun MicListeningButton(
    listening: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    contentDescription: String = "Grabar voz",
) {
    val extended = linguaExtendedColors()
    val colors = MaterialTheme.colorScheme
    val animations = rememberAnimationsEnabled()
    val transition = rememberInfiniteTransition(label = "micRipple")
    val pulse by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec =
            infiniteRepeatable(
                animation =
                    tween(
                        durationMillis = LinguaMotion.DURATION_MIC_RIPPLE_MS,
                        easing = LinearEasing,
                    ),
            ),
        label = "micRipplePulse",
    )
    Box(
        modifier =
            modifier
                .size(96.dp)
                .feedbackClickable(
                    cue = FeedbackCue.TAP,
                    enabled = enabled,
                    onClick = onClick,
                ).semantics {
                    role = Role.Button
                    this.contentDescription = contentDescription
                },
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val base = 36.dp.toPx()
            val scales = listOf(1f, 1.28f, 1.56f)
            val alphas =
                if (listening) {
                    listOf(0.42f, 0.26f, 0.14f)
                } else {
                    listOf(0.30f, 0.18f, 0.10f)
                }
            val wobble = if (listening && animations) 1f + 0.05f * pulse else 1f
            scales.forEachIndexed { index, scale ->
                drawCircle(
                    color = extended.tanEdge,
                    radius = base * scale * wobble,
                    alpha = alphas[index],
                )
            }
        }
        Box(
            modifier =
                Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(if (listening) colors.primaryContainer else colors.primary),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(LinguaIcons.MicRes),
                contentDescription = null,
                tint = if (listening) colors.onPrimaryContainer else colors.onPrimary,
                modifier = Modifier.size(28.dp),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MicListeningButtonPreview() {
    LinguaTheme {
        MicListeningButton(listening = false, onClick = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun MicListeningButtonListeningPreview() {
    LinguaTheme {
        MicListeningButton(listening = true, onClick = {})
    }
}

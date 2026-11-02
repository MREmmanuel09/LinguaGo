package dev.linguago.core.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.linguago.core.ui.feedback.FeedbackCue
import dev.linguago.core.ui.feedback.LocalInteractionFeedback
import dev.linguago.core.ui.theme.LinguaMotion
import dev.linguago.core.ui.theme.LinguaTheme
import dev.linguago.core.ui.theme.LinguaTokens
import dev.linguago.core.ui.util.rememberAnimationsEnabled

/**
 * Botón principal con borde inferior 3D (RFC §7.2, ADR-031/ADR-034): color
 * primario por defecto, borde 4dp, radio 16dp, objetivo táctil ≥ 48dp (I8). Al
 * presionar, el cuerpo se hunde sobre el borde y vuelve con un rebote corto;
 * un bisel de luz superior da volumen. La pulsación emite un cue `TAP`.
 * [bodyColor], [edgeColor], [contentColor] y [compact] permiten variantes
 * semánticas (por ejemplo, la calificación del repaso) sin duplicar la física.
 */
@Composable
fun PrimaryButton3D(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    bodyColor: Color? = null,
    edgeColor: Color? = null,
    contentColor: Color = Color.White,
    compact: Boolean = false,
) {
    val resolvedBody = bodyColor ?: LinguaTokens.Primary
    val resolvedEdge = edgeColor ?: LinguaTokens.PrimaryEdge
    val body = if (enabled) resolvedBody else resolvedBody.copy(alpha = 0.35f)
    val edge = if (enabled) resolvedEdge else resolvedEdge.copy(alpha = 0.35f)
    val feedback = LocalInteractionFeedback.current
    val animations = rememberAnimationsEnabled()
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val press by animateFloatAsState(
        targetValue = if (pressed && animations) 1f else 0f,
        animationSpec = if (animations) LinguaMotion.SpringBouncy else snap(),
        label = "primaryButtonPress",
    )
    val shape = RoundedCornerShape(LinguaTokens.RADIUS_BUTTON_DP.dp)

    Box(
        modifier =
            modifier
                .padding(bottom = LinguaTokens.PRESS_DEPTH_DP.dp)
                .clip(shape)
                .clickable(
                    interactionSource = interaction,
                    indication = LocalIndication.current,
                    enabled = enabled,
                    onClick = {
                        feedback.play(FeedbackCue.TAP)
                        onClick()
                    },
                ).semantics { role = Role.Button },
    ) {
        Box(
            modifier =
                Modifier
                    .matchParentSize()
                    .offset(y = LinguaTokens.PRESS_DEPTH_DP.dp)
                    .clip(shape)
                    .background(edge),
        )
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = if (compact) 48.dp else 52.dp)
                    .offset(y = (press * LinguaTokens.PRESS_DEPTH_DP).dp)
                    .clip(shape)
                    .background(body)
                    .topHighlight(),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = text,
                color = contentColor,
                textAlign = TextAlign.Center,
                style = if (compact) MaterialTheme.typography.labelLarge else MaterialTheme.typography.titleMedium,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PrimaryButton3DPreview() {
    LinguaTheme {
        PrimaryButton3D(
            text = "Comenzar",
            onClick = {},
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

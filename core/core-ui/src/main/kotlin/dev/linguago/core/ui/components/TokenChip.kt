package dev.linguago.core.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.linguago.core.ui.feedback.FeedbackCue
import dev.linguago.core.ui.feedback.LocalInteractionFeedback
import dev.linguago.core.ui.theme.LinguaMotion
import dev.linguago.core.ui.theme.LinguaTheme
import dev.linguago.core.ui.theme.LinguaTokens
import dev.linguago.core.ui.util.rememberAnimationsEnabled

/**
 * Ficha de palabra del tablero de respuesta (ADR-031): cuerpo con borde
 * inferior 3D, bisel de luz y hundimiento al presionar. Comparte la física de
 * `PrimaryButton3D`/`OptionCard` y emite `SELECT`/`DESELECT` según su estado.
 * Semántica de botón con estado de selección para TalkBack (I8).
 */
@Composable
public fun TokenChip(
    token: String,
    selected: Boolean,
    onToggle: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val colors = MaterialTheme.colorScheme
    val feedback = LocalInteractionFeedback.current
    val animations = rememberAnimationsEnabled()
    val background by animateColorAsState(
        targetValue = if (selected) colors.primary else colors.surface,
        animationSpec = if (animations) tween(durationMillis = 160) else snap(),
        label = "tokenBackground",
    )
    val edgeColor by animateColorAsState(
        targetValue = if (selected) LinguaTokens.PrimaryEdge else colors.outline.copy(alpha = 0.55f),
        animationSpec = if (animations) tween(durationMillis = 160) else snap(),
        label = "tokenEdge",
    )
    val contentColor by animateColorAsState(
        targetValue = if (selected) colors.onPrimary else colors.onSurface,
        animationSpec = if (animations) tween(durationMillis = 160) else snap(),
        label = "tokenContent",
    )
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val press by animateFloatAsState(
        targetValue = if (pressed && animations) 1f else 0f,
        animationSpec = if (animations) LinguaMotion.SpringBouncy else snap(),
        label = "tokenPress",
    )
    val shape = RoundedCornerShape(12.dp)

    Box(modifier = modifier.padding(bottom = LinguaTokens.PRESS_DEPTH_DP.dp)) {
        Box(
            modifier =
                Modifier
                    .matchParentSize()
                    .offset(y = LinguaTokens.PRESS_DEPTH_DP.dp)
                    .clip(shape)
                    .background(edgeColor),
        )
        Text(
            text = token,
            style = MaterialTheme.typography.bodyMedium,
            color = contentColor,
            modifier =
                Modifier
                    .offset(y = (press * LinguaTokens.PRESS_DEPTH_DP).dp)
                    .clip(shape)
                    .background(background)
                    .topHighlight()
                    .clickable(
                        interactionSource = interaction,
                        indication = null,
                        enabled = enabled,
                    ) {
                        feedback.play(if (selected) FeedbackCue.DESELECT else FeedbackCue.SELECT)
                        onToggle(token)
                    }.semantics {
                        role = Role.Button
                        this.selected = selected
                    }.padding(horizontal = 12.dp, vertical = 14.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TokenChipPreview() {
    LinguaTheme {
        TokenChip(token = "hola", selected = false, onToggle = {})
    }
}

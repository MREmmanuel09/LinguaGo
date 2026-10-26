package dev.linguago.core.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.linguago.core.ui.feedback.FeedbackCue
import dev.linguago.core.ui.feedback.LocalInteractionFeedback
import dev.linguago.core.ui.theme.LinguaMotion
import dev.linguago.core.ui.theme.LinguaTokens
import dev.linguago.core.ui.util.rememberAnimationsEnabled

/**
 * Tarjeta de opción (RFC §7.2): borde 2dp `--border`, radio 12dp; al
 * seleccionarse cambia a primario con una transición breve (ADR-027). Tras
 * calificar muestra en verde la correcta y en coral la elegida incorrecta
 * (ADR-028). Semántica de RadioButton para TalkBack (invariante I8).
 */
@Composable
fun OptionCard(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leading: (@Composable () -> Unit)? = null,
    status: OptionStatus = OptionStatus.NEUTRAL,
) {
    val colors = MaterialTheme.colorScheme
    val feedback = LocalInteractionFeedback.current
    val animations = rememberAnimationsEnabled()
    val backgroundTarget =
        when (status) {
            OptionStatus.CORRECT -> LinguaTokens.Accent.copy(alpha = 0.12f)
            OptionStatus.INCORRECT -> LinguaTokens.Destructive.copy(alpha = 0.12f)
            OptionStatus.NEUTRAL -> if (selected) colors.primary.copy(alpha = 0.06f) else Color.Transparent
        }
    val borderTarget =
        when (status) {
            OptionStatus.CORRECT -> LinguaTokens.Accent
            OptionStatus.INCORRECT -> LinguaTokens.Destructive
            OptionStatus.NEUTRAL -> if (selected) colors.primary else colors.outline
        }
    val contentTarget =
        when (status) {
            OptionStatus.CORRECT -> LinguaTokens.Accent
            OptionStatus.INCORRECT -> LinguaTokens.Destructive
            OptionStatus.NEUTRAL -> if (selected) colors.primary else colors.onSurface
        }
    val background by animateColorAsState(
        targetValue = backgroundTarget,
        animationSpec = if (animations) tween(durationMillis = 180) else snap(),
        label = "optionBackground",
    )
    val borderColor by animateColorAsState(
        targetValue = borderTarget,
        animationSpec = if (animations) tween(durationMillis = 180) else snap(),
        label = "optionBorder",
    )
    val contentColor by animateColorAsState(
        targetValue = contentTarget,
        animationSpec = if (animations) tween(durationMillis = 180) else snap(),
        label = "optionContent",
    )
    val scale by animateFloatAsState(
        targetValue = if (selected && animations) 1.02f else 1f,
        animationSpec = LinguaMotion.SpringSnappy,
        label = "optionScale",
    )

    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }.clip(RoundedCornerShape(LinguaTokens.RADIUS_CARD_DP.dp))
                .background(background)
                .border(2.dp, borderColor, RoundedCornerShape(LinguaTokens.RADIUS_CARD_DP.dp))
                .selectable(
                    selected = selected,
                    enabled = enabled,
                    role = Role.RadioButton,
                    onClick = {
                        feedback.play(FeedbackCue.SELECT)
                        onClick()
                    },
                ).semantics {
                    if (status != OptionStatus.NEUTRAL) {
                        stateDescription =
                            if (status == OptionStatus.CORRECT) "Respuesta correcta" else "Respuesta incorrecta"
                    }
                }.padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (leading != null) {
            leading()
        }
        Text(
            text = text,
            color = contentColor,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            style = MaterialTheme.typography.bodyMedium,
        )
        if (status != OptionStatus.NEUTRAL) {
            Spacer(Modifier.weight(1f))
            Image(
                painter =
                    painterResource(
                        if (status == OptionStatus.CORRECT) LinguaIcons.CheckRes else LinguaIcons.CloseRes,
                    ),
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                colorFilter = ColorFilter.tint(contentColor),
            )
        }
    }
}

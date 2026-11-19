package dev.linguago.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.linguago.core.ui.feedback.FeedbackCue
import dev.linguago.core.ui.feedback.feedbackClickable
import dev.linguago.core.ui.theme.LinguaTheme
import dev.linguago.core.ui.theme.LinguaTokens
import dev.linguago.core.ui.theme.linguaExtendedColors

/**
 * Botón píldora con contorno del prototipo «Warm Cream & Teal» (ADR-055):
 * cuerpo crema secundario, borde tan y tinta principal. Para «Guía», «Salir»
 * y «Cambiar». Objetivo táctil ≥ 44dp (I8) y cue `TAP` al pulsar.
 */
@Composable
public fun PillButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val extended = linguaExtendedColors()
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground,
        modifier =
            modifier
                .heightIn(min = 44.dp)
                .clip(RoundedCornerShape(LinguaTokens.RADIUS_PILL_DP.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(1.5.dp, extended.tanEdge, RoundedCornerShape(LinguaTokens.RADIUS_PILL_DP.dp))
                .feedbackClickable(
                    cue = FeedbackCue.TAP,
                    enabled = enabled,
                    onClick = onClick,
                ).semantics { role = Role.Button }
                .padding(horizontal = 14.dp, vertical = 8.dp),
    )
}

@Preview(showBackground = true)
@Composable
private fun PillButtonPreview() {
    LinguaTheme {
        PillButton(text = "Guía", onClick = {})
    }
}

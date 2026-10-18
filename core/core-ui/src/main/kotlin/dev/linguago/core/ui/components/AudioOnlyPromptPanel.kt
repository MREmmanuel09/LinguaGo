package dev.linguago.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.linguago.core.ui.theme.LinguaTheme
import dev.linguago.core.ui.theme.LinguaTokens

/**
 * Enunciado de comprensión auditiva pura (F2.4, ADR-018): la transcripción
 * permanece oculta hasta que el usuario responde; solo entonces se revela
 * como refuerzo. La reproducción lenta (0.75x) es opcional por ejercicio y
 * nunca bloquea la respuesta si no hay voz (I1, I3).
 */
@Composable
public fun AudioOnlyPromptPanel(
    audioAvailable: Boolean,
    allowSlowPlayback: Boolean,
    transcript: String?,
    revealTranscript: Boolean,
    onPlay: () -> Unit,
    onPlaySlow: () -> Unit,
    modifier: Modifier = Modifier,
    question: String? = null,
) {
    val colors = MaterialTheme.colorScheme
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "Escucha y elige",
            style = MaterialTheme.typography.titleLarge,
            color = colors.onBackground,
        )
        question?.let { text ->
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                color = colors.onBackground,
            )
        }
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .clip(RoundedCornerShape(LinguaTokens.RADIUS_CARD_DP.dp))
                    .background(if (audioAvailable) colors.secondaryContainer else colors.surfaceVariant)
                    .clickable(enabled = audioAvailable, onClick = onPlay)
                    .semantics { role = Role.Button }
                    .padding(16.dp),
        ) {
            Text(
                text =
                    if (audioAvailable) {
                        "Reproducir audio (toca aquí)"
                    } else {
                        "Voz no disponible en este dispositivo"
                    },
                style = MaterialTheme.typography.titleMedium,
                color = if (audioAvailable) colors.onSecondaryContainer else colors.onSurfaceVariant,
            )
        }
        if (allowSlowPlayback && audioAvailable) {
            TextButton(
                onClick = onPlaySlow,
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
            ) {
                Text(text = "Reproducir más despacio (0.75x)")
            }
        }
        if (revealTranscript && transcript != null) {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(LinguaTokens.RADIUS_CARD_DP.dp))
                        .background(colors.surfaceVariant)
                        .semantics { liveRegion = LiveRegionMode.Polite }
                        .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = "Lo que se dijo",
                    style = MaterialTheme.typography.labelLarge,
                    color = colors.onSurfaceVariant,
                )
                Text(
                    text = transcript,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurface,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AudioOnlyPromptPanelPreview() {
    LinguaTheme {
        AudioOnlyPromptPanel(
            audioAvailable = true,
            allowSlowPlayback = true,
            transcript = "The weather is nice today.",
            revealTranscript = false,
            onPlay = {},
            onPlaySlow = {},
            question = "¿Qué tiempo hace?",
        )
    }
}

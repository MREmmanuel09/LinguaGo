package dev.linguago.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.linguago.core.ui.feedback.FeedbackCue
import dev.linguago.core.ui.feedback.LocalInteractionFeedback
import dev.linguago.core.ui.theme.LinguaTheme
import dev.linguago.core.ui.theme.LinguaTokens

/**
 * Reproductor de audio de la lección y el repaso (ADR-037/ADR-038): botón
 * circular de reproducción con diana ≥48dp, onda de voz y conmutador de
 * velocidad en píldora `[1x | 0.75x]` (el 0.75x solo aparece si el llamador
 * ofrece versión lenta). No hay barra de progreso ni seek: el contrato
 * `TtsEngine.speak` sintetiza en streaming, sin duración ni posición.
 */
@Composable
public fun AudioPlayerCard(
    title: String,
    playing: Boolean,
    enabled: Boolean,
    onPlay: () -> Unit,
    modifier: Modifier = Modifier,
    onPlaySlow: (() -> Unit)? = null,
) {
    val colors = MaterialTheme.colorScheme
    val feedback = LocalInteractionFeedback.current
    var slow by remember { mutableStateOf(false) }
    val background = if (enabled) colors.secondaryContainer else colors.surfaceVariant

    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(LinguaTokens.RADIUS_CARD_DP.dp))
                .background(background)
                .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier =
                    Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(if (enabled) LinguaTokens.Primary else colors.outlineVariant),
            ) {
                IconButton(
                    onClick = {
                        feedback.play(FeedbackCue.TAP)
                        if (slow && onPlaySlow != null) onPlaySlow() else onPlay()
                    },
                    enabled = enabled,
                    modifier = Modifier.fillMaxSize(),
                ) {
                    Icon(
                        painter = painterResource(LinguaIcons.PlayRes),
                        contentDescription = "Reproducir audio",
                        tint = if (enabled) Color.White else colors.onSurfaceVariant,
                    )
                }
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (enabled) colors.onSecondaryContainer else colors.onSurfaceVariant,
                )
                if (enabled) {
                    VoiceWaveIndicator(active = playing)
                }
            }
        }
        if (onPlaySlow != null && enabled) {
            Row(
                modifier =
                    Modifier
                        .clip(RoundedCornerShape(LinguaTokens.RADIUS_PILL_DP.dp))
                        .background(colors.surface)
                        .padding(2.dp)
                        .selectableGroup(),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                SpeedSegment(label = "1x", selected = !slow, onSelect = { slow = false })
                SpeedSegment(label = "0.75x", selected = slow, onSelect = { slow = true })
            }
        }
    }
}

@Composable
private fun SpeedSegment(
    label: String,
    selected: Boolean,
    onSelect: () -> Unit,
) {
    val feedback = LocalInteractionFeedback.current
    Text(
        text = label,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
        color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
        modifier =
            Modifier
                .heightIn(min = 48.dp)
                .clip(RoundedCornerShape(LinguaTokens.RADIUS_PILL_DP.dp))
                .background(if (selected) LinguaTokens.Primary else Color.Transparent)
                .selectable(
                    selected = selected,
                    role = Role.RadioButton,
                    onClick = {
                        feedback.play(FeedbackCue.TAP)
                        onSelect()
                    },
                ).padding(horizontal = 16.dp, vertical = 12.dp),
    )
}

@Preview(name = "Audio con velocidad", showBackground = true)
@Composable
private fun AudioPlayerCardPreview() {
    LinguaTheme {
        AudioPlayerCard(
            title = "Although the deadline is tight, we can deliver on Friday.",
            playing = false,
            enabled = true,
            onPlay = {},
            onPlaySlow = {},
        )
    }
}

@Preview(name = "Audio sin voz", showBackground = true)
@Composable
private fun AudioPlayerCardDisabledPreview() {
    LinguaTheme {
        AudioPlayerCard(
            title = "Voz no disponible en este dispositivo",
            playing = false,
            enabled = false,
            onPlay = {},
        )
    }
}

package dev.linguago.core.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import dev.linguago.core.ui.feedback.FeedbackCue
import dev.linguago.core.ui.feedback.LocalInteractionFeedback
import dev.linguago.core.ui.theme.LinguaMotion
import dev.linguago.core.ui.theme.LinguaTokens
import dev.linguago.core.ui.util.rememberAnimationsEnabled

/**
 * Banner de feedback (RFC §7.2, F2.2): verde (`--accent`) para aciertos y
 * coral (`--destructive`) para errores. En aciertos muestra la explicación
 * del porqué y la respuesta canónica (ADR-028); en errores mantiene el
 * diagnóstico expandible. Si hay una nota cultural aplicable, puede
 * expandirse. No bloquea el avance (I3) y se anuncia como live region (I8).
 */
@Composable
public fun FeedbackBanner(
    isCorrect: Boolean,
    message: String,
    modifier: Modifier = Modifier,
    diagnostic: String? = null,
    idiom: String? = null,
    culturalNote: String? = null,
    successExplanation: String? = null,
    correctAnswer: String? = null,
) {
    val statusColor = if (isCorrect) LinguaTokens.Accent else LinguaTokens.Destructive
    val iconRes = if (isCorrect) LinguaIcons.CheckRes else LinguaIcons.CloseRes
    val hasDetails = diagnostic != null || culturalNote != null
    val feedback = LocalInteractionFeedback.current
    val animations = rememberAnimationsEnabled()
    val entered = remember(isCorrect, message) { mutableStateOf(false) }
    LaunchedEffect(entered) { entered.value = true }
    val iconScale by animateFloatAsState(
        targetValue = if (!animations || entered.value) 1f else 0.7f,
        animationSpec = LinguaMotion.SpringBouncy,
        label = "feedbackIconScale",
    )
    var expanded by remember(isCorrect, message, diagnostic, idiom, culturalNote) {
        mutableStateOf(!isCorrect && diagnostic != null)
    }

    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(LinguaTokens.RADIUS_CARD_DP.dp))
                .background(statusColor.copy(alpha = 0.18f))
                .animateContentSize()
                .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .semantics(mergeDescendants = true) {
                        liveRegion = LiveRegionMode.Polite
                    },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Image(
                painter = painterResource(iconRes),
                contentDescription = null,
                modifier =
                    Modifier
                        .padding(2.dp)
                        .graphicsLayer {
                            scaleX = iconScale
                            scaleY = iconScale
                        },
                colorFilter = ColorFilter.tint(statusColor),
            )
            Text(
                text = message,
                color = statusColor,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        if (successExplanation != null || correctAnswer != null) {
            Column(
                modifier =
                    Modifier.semantics(mergeDescendants = true) {
                        liveRegion = LiveRegionMode.Polite
                    },
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                successExplanation?.let { explanation ->
                    Text(
                        text = explanation,
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                correctAnswer?.let { answer ->
                    Text(
                        text = "Respuesta correcta: $answer",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }
        if (hasDetails) {
            TextButton(
                onClick = {
                    feedback.play(FeedbackCue.TAP)
                    expanded = !expanded
                },
                modifier =
                    Modifier.semantics {
                        stateDescription = if (expanded) "Detalle visible" else "Detalle oculto"
                    },
            ) {
                Text(
                    text =
                        when {
                            expanded -> "Ocultar detalle"
                            diagnostic != null -> "Ver por qué"
                            else -> "Ver nota cultural"
                        },
                )
            }
            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier =
                        Modifier.semantics(mergeDescendants = true) {
                            liveRegion = LiveRegionMode.Polite
                        },
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    diagnostic?.let { explanation ->
                        Text(
                            text = explanation,
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                    if (culturalNote != null) {
                        Text(
                            text = if (idiom != null) "Modismo: $idiom" else "Nota cultural",
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.labelLarge,
                        )
                        Text(
                            text = culturalNote,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            }
        }
    }
}

package dev.linguago.core.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.linguago.core.ui.feedback.FeedbackCue
import dev.linguago.core.ui.feedback.LocalInteractionFeedback
import dev.linguago.core.ui.theme.LinguaTheme
import dev.linguago.core.ui.theme.linguaExtendedColors

/**
 * Sheet de feedback del prototipo «Warm Cream & Teal» (ADR-055): fondo
 * cálido, título en tinta terracota, respuesta canónica y bloque «Ver por
 * qué» desplegable sobre superficie tan, con botón Continuar teal. La X
 * cierra el resultado y avanza (equivale a Continuar); nunca bloquea el
 * avance (I3) y se anuncia como live region (I8).
 */
@Composable
public fun FeedbackSheet(
    isCorrect: Boolean,
    message: String,
    modifier: Modifier = Modifier,
    skipped: Boolean = false,
    diagnostic: String? = null,
    idiom: String? = null,
    culturalNote: String? = null,
    successExplanation: String? = null,
    correctAnswer: String? = null,
    glossary: Map<String, List<String>> = emptyMap(),
    retryAvailable: Boolean = false,
    onRetry: () -> Unit = {},
    onReveal: () -> Unit = {},
    onContinue: () -> Unit,
    onClose: (() -> Unit)? = null,
) {
    val extended = linguaExtendedColors()
    val feedback = LocalInteractionFeedback.current
    var expanded by remember(isCorrect, message, diagnostic, idiom, culturalNote) {
        mutableStateOf(false)
    }
    val hasDetails = !skipped && (diagnostic != null || culturalNote != null)
    val title =
        when {
            skipped -> "Ejercicio omitido"
            isCorrect -> "¡Correcto!"
            else -> "Revisa la respuesta"
        }

    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .background(extended.sheetWarm)
                .semantics(mergeDescendants = true) {
                    liveRegion = LiveRegionMode.Polite
                }.padding(horizontal = 20.dp, vertical = 16.dp)
                .animateContentSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = extended.sheetTitleInk,
                modifier = Modifier.weight(1f),
            )
            IconButton(
                onClick = {
                    feedback.play(FeedbackCue.TAP)
                    (onClose ?: onContinue)()
                },
                modifier = Modifier.size(32.dp),
            ) {
                Image(
                    painter = painterResource(LinguaIcons.CloseRes),
                    contentDescription = "Cerrar y continuar",
                    modifier = Modifier.size(18.dp),
                    colorFilter = ColorFilter.tint(extended.sheetTitleInk),
                )
            }
        }
        if (skipped) {
            Text(
                text = message.ifBlank { "Puedes continuar sin penalización." },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
        } else {
            successExplanation?.let { explanation ->
                Text(
                    text = explanation,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                )
            }
            val revealedAnswer = correctAnswer?.takeIf { !isCorrect }
            revealedAnswer?.let { answer ->
                WordGlossaryText(
                    text = "Respuesta correcta: $answer",
                    glossary = glossary,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                )
            }
        }
        if (hasDetails) {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(extended.sheetWarmSurface)
                        .padding(12.dp)
                        .animateContentSize(),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text =
                            when {
                                expanded -> "Ocultar detalle"
                                diagnostic != null -> "Ver por qué"
                                else -> "Ver nota cultural"
                            },
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.weight(1f),
                    )
                    Image(
                        painter = painterResource(LinguaIcons.ChevronDownRes),
                        contentDescription = null,
                        modifier =
                            Modifier
                                .size(18.dp)
                                .graphicsLayer { rotationZ = if (expanded) 180f else 0f },
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground),
                    )
                }
                AnimatedVisibility(visible = expanded) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        diagnostic?.let { explanation ->
                            Text(
                                text = explanation,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onBackground,
                            )
                        }
                        if (culturalNote != null) {
                            Text(
                                text = if (idiom != null) "Modismo: $idiom" else "Nota cultural",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onBackground,
                            )
                            Text(
                                text = culturalNote,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onBackground,
                            )
                        }
                    }
                }
            }
        }
        if (!skipped && retryAvailable) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                TextButton(
                    onClick = {
                        feedback.play(FeedbackCue.TAP)
                        onRetry()
                    },
                    modifier = Modifier.weight(1f),
                ) {
                    Text(text = "Reintentar", color = MaterialTheme.colorScheme.onBackground)
                }
                if (correctAnswer == null) {
                    TextButton(
                        onClick = {
                            feedback.play(FeedbackCue.TAP)
                            onReveal()
                        },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(text = "Ver respuesta", color = MaterialTheme.colorScheme.onBackground)
                    }
                }
            }
        }
        PrimaryButton3D(
            text = "Continuar",
            onClick = onContinue,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun FeedbackSheetIncorrectPreview() {
    LinguaTheme {
        FeedbackSheet(
            isCorrect = false,
            message = "Revisa la respuesta",
            correctAnswer = "It's seven o'clock",
            diagnostic = "Usa It's para la hora y o'clock para la hora en punto.",
            retryAvailable = true,
            onRetry = {},
            onReveal = {},
            onContinue = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun FeedbackSheetCorrectPreview() {
    LinguaTheme {
        FeedbackSheet(
            isCorrect = true,
            message = "¡Correcto!",
            successExplanation = "La frase conserva el orden y el significado.",
            onContinue = {},
        )
    }
}

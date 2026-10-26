package dev.linguago.core.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.linguago.core.domain.logic.FreeSpeakingFeedback
import dev.linguago.core.domain.logic.FreeSpeakingVerdict
import dev.linguago.core.ui.feedback.FeedbackCue
import dev.linguago.core.ui.feedback.LocalInteractionFeedback
import dev.linguago.core.ui.feedback.feedbackClickable
import dev.linguago.core.ui.theme.LinguaTheme
import dev.linguago.core.ui.theme.LinguaTokens
import kotlin.math.roundToInt

/**
 * Producción oral libre B2 (F2.5, ADR-019): grabación con visualizador de
 * energía RMS en vivo y palabras clave detectadas, paso de revisión con
 * respuesta modelo (TTS), desglose heurístico determinista y autoevaluación
 * de tres niveles por criterio. Nunca bloquea: sin STT se puede saltar (I3).
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
public fun FreeSpeakingPanel(
    prompt: String,
    speechAvailable: Boolean,
    recording: Boolean,
    waveform: List<Float>,
    liveKeywords: List<String>,
    partialTranscript: String,
    transcript: String?,
    durationMillis: Long,
    feedback: FreeSpeakingFeedback?,
    detectedGrammarLabels: List<String>,
    modelAnswer: String,
    criteria: List<String>,
    selfAssessment: Map<Int, Int>,
    audioAvailable: Boolean,
    enabled: Boolean = true,
    onStart: () -> Unit,
    onStop: () -> Unit,
    onRate: (criterion: Int, level: Int) -> Unit,
    onPlayModel: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Producción oral",
            style = MaterialTheme.typography.titleLarge,
            color = colors.onBackground,
        )
        Text(
            text = prompt,
            style = MaterialTheme.typography.bodyLarge,
            color = colors.onBackground,
        )
        when {
            recording ->
                RecordingSection(
                    waveform = waveform,
                    liveKeywords = liveKeywords,
                    partialTranscript = partialTranscript,
                    onStop = onStop,
                )
            transcript != null ->
                ReviewSection(
                    transcript = transcript,
                    durationMillis = durationMillis,
                    feedback = feedback,
                    detectedGrammarLabels = detectedGrammarLabels,
                    modelAnswer = modelAnswer,
                    criteria = criteria,
                    selfAssessment = selfAssessment,
                    audioAvailable = audioAvailable,
                    enabled = enabled,
                    onRate = onRate,
                    onPlayModel = onPlayModel,
                )
            else ->
                ReadySection(
                    speechAvailable = speechAvailable,
                    enabled = enabled,
                    onStart = onStart,
                    onSkip = onSkip,
                )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RecordingSection(
    waveform: List<Float>,
    liveKeywords: List<String>,
    partialTranscript: String,
    onStop: () -> Unit,
) {
    RmsWaveform(levels = waveform, modifier = Modifier.fillMaxWidth().height(96.dp))
    if (liveKeywords.isNotEmpty()) {
        Text(
            text = "Ideas clave detectadas",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            liveKeywords.forEach { term -> KeywordChip(term) }
        }
    }
    if (partialTranscript.isNotBlank()) {
        Text(
            text = "Vamos escuchando: \"$partialTranscript\"",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
    PrimaryButton3D(
        text = "Detener grabación",
        onClick = onStop,
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun ReviewSection(
    transcript: String,
    durationMillis: Long,
    feedback: FreeSpeakingFeedback?,
    detectedGrammarLabels: List<String>,
    modelAnswer: String,
    criteria: List<String>,
    selfAssessment: Map<Int, Int>,
    audioAvailable: Boolean,
    enabled: Boolean,
    onRate: (criterion: Int, level: Int) -> Unit,
    onPlayModel: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val feedbackPlayer = LocalInteractionFeedback.current
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(LinguaTokens.RADIUS_CARD_DP.dp))
                .background(colors.surfaceVariant)
                .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = "Lo que entendimos",
            style = MaterialTheme.typography.labelLarge,
            color = colors.onSurfaceVariant,
        )
        Text(
            text = transcript.ifBlank { "No detectamos voz. Puedes intentarlo de nuevo." },
            style = MaterialTheme.typography.bodyMedium,
            color = colors.onSurface,
        )
        Text(
            text = "Duración: ${(durationMillis / 1000.0).roundToInt()} s",
            style = MaterialTheme.typography.bodySmall,
            color = colors.onSurfaceVariant,
        )
    }
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(LinguaTokens.RADIUS_CARD_DP.dp))
                .border(2.dp, colors.outline, RoundedCornerShape(LinguaTokens.RADIUS_CARD_DP.dp))
                .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = "Respuesta modelo",
            style = MaterialTheme.typography.labelLarge,
            color = colors.onSurfaceVariant,
        )
        Text(text = modelAnswer, style = MaterialTheme.typography.bodyMedium, color = colors.onSurface)
        TextButton(
            onClick = {
                feedbackPlayer.play(FeedbackCue.TAP)
                onPlayModel()
            },
            enabled = audioAvailable,
        ) {
            Text(text = "Escuchar respuesta modelo")
        }
    }
    if (feedback != null) {
        HeuristicBreakdown(feedback = feedback, detectedGrammarLabels = detectedGrammarLabels)
    }
    if (criteria.isNotEmpty()) {
        SelfAssessment(
            criteria = criteria,
            selfAssessment = selfAssessment,
            enabled = enabled,
            onRate = onRate,
        )
    }
}

@Composable
private fun ReadySection(
    speechAvailable: Boolean,
    enabled: Boolean,
    onStart: () -> Unit,
    onSkip: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val feedback = LocalInteractionFeedback.current
    Text(
        text =
            if (speechAvailable) {
                "Toca para grabar tu respuesta. Al terminar verás el desglose y la respuesta modelo."
            } else {
                "Reconocimiento de voz no disponible en este dispositivo."
            },
        style = MaterialTheme.typography.bodyMedium,
        color = colors.onSurfaceVariant,
    )
    PrimaryButton3D(
        text = "Grabar respuesta",
        onClick = onStart,
        enabled = speechAvailable && enabled,
        modifier = Modifier.fillMaxWidth(),
    )
    TextButton(
        onClick = {
            feedback.play(FeedbackCue.TAP)
            onSkip()
        },
        enabled = enabled,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(text = "No puedo hablar ahora")
    }
}

/** Barras de energía vocal: nivel 0..1 por lectura del `Flow` de RMS. */
@Composable
private fun RmsWaveform(
    levels: List<Float>,
    modifier: Modifier = Modifier,
) {
    val activeColor = MaterialTheme.colorScheme.primary
    val idleColor = MaterialTheme.colorScheme.outlineVariant
    Canvas(
        modifier =
            modifier
                .clip(RoundedCornerShape(LinguaTokens.RADIUS_CARD_DP.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(horizontal = 12.dp, vertical = 14.dp)
                .semantics { contentDescription = "Visualizador de energía de voz" },
    ) {
        val visible = levels.takeLast(WAVE_BARS)
        val start = WAVE_BARS - visible.size
        val slot = size.width / WAVE_BARS
        val barWidth = (slot * 0.6f).coerceAtLeast(1f)
        val minHeight = 4.dp.toPx()
        val centerY = size.height / 2f
        for (index in 0 until WAVE_BARS) {
            val level = visible.getOrNull(index - start) ?: 0f
            val barHeight = (minHeight + level.coerceIn(0f, 1f) * (size.height - minHeight))
            val x = index * slot + (slot - barWidth) / 2f
            drawRoundRect(
                color = if (level > 0f) activeColor else idleColor,
                topLeft = Offset(x, centerY - barHeight / 2f),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f),
            )
        }
    }
}

@Composable
private fun KeywordChip(term: String) {
    Text(
        text = term,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onTertiaryContainer,
        modifier =
            Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.tertiaryContainer)
                .padding(horizontal = 10.dp, vertical = 4.dp),
    )
}

@Composable
private fun HeuristicBreakdown(
    feedback: FreeSpeakingFeedback,
    detectedGrammarLabels: List<String>,
) {
    val colors = MaterialTheme.colorScheme
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(LinguaTokens.RADIUS_CARD_DP.dp))
                .background(colors.secondaryContainer)
                .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = "Tu resultado",
            style = MaterialTheme.typography.titleMedium,
            color = colors.onSecondaryContainer,
        )
        BreakdownRow(label = "Palabras", value = feedback.tokenCount.toString())
        BreakdownRow(label = "Ideas clave", value = "${feedback.coveredClusters}/${feedback.totalClusters}")
        BreakdownRow(
            label = "Gramática detectada",
            value = if (detectedGrammarLabels.isEmpty()) "Ninguna" else detectedGrammarLabels.joinToString(", "),
        )
        BreakdownRow(label = "Ritmo", value = "${feedback.wpm.roundToInt()} palabras/min")
    }
}

@Composable
private fun BreakdownRow(
    label: String,
    value: String,
) {
    Row(
        modifier =
            Modifier.fillMaxWidth().semantics(mergeDescendants = true) {
                contentDescription = "$label: $value"
            },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
        )
    }
}

@Composable
private fun SelfAssessment(
    criteria: List<String>,
    selfAssessment: Map<Int, Int>,
    enabled: Boolean,
    onRate: (criterion: Int, level: Int) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "Autoevaluación",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            text = "Marca cómo te salió cada criterio.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        criteria.forEachIndexed { index, criterion ->
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "${index + 1}. $criterion",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    LEVEL_LABELS.forEachIndexed { level, label ->
                        LevelChip(
                            label = label,
                            selected = selfAssessment[index] == level,
                            enabled = enabled,
                            description = "Criterio ${index + 1}: $label",
                            onClick = { onRate(index, level) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LevelChip(
    label: String,
    selected: Boolean,
    enabled: Boolean,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    Text(
        text = label,
        style = MaterialTheme.typography.bodySmall,
        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
        color = if (selected) colors.onPrimary else colors.onSurface,
        textAlign = TextAlign.Center,
        modifier =
            modifier
                .heightIn(min = 48.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(if (selected) colors.primary else Color.Transparent)
                .border(2.dp, if (selected) colors.primary else colors.outline, RoundedCornerShape(8.dp))
                .feedbackClickable(FeedbackCue.SELECT, enabled = enabled, onClick = onClick)
                .semantics {
                    role = Role.RadioButton
                    this.selected = selected
                    contentDescription = description
                }.padding(horizontal = 8.dp, vertical = 14.dp),
    )
}

private const val WAVE_BARS = 48

private val LEVEL_LABELS = listOf("Aún no", "Casi", "Sí")

@Preview(showBackground = true)
@Composable
private fun FreeSpeakingPanelPreview() {
    LinguaTheme {
        FreeSpeakingPanel(
            prompt = "Describe una decisión difícil que hayas tomado.",
            speechAvailable = true,
            recording = false,
            waveform = emptyList(),
            liveKeywords = emptyList(),
            partialTranscript = "",
            transcript = "Last year I decided to change jobs because I wanted to grow.",
            durationMillis = 12_400,
            feedback =
                FreeSpeakingFeedback(
                    passes = true,
                    tokenCount = 14,
                    coveredClusters = 2,
                    totalClusters = 2,
                    detectedGrammarRefs = emptyList(),
                    wpm = 108.0,
                    verdict = FreeSpeakingVerdict.Pass,
                ),
            detectedGrammarLabels = listOf("Conditionals"),
            modelAnswer = "Last year I made a difficult decision: I changed jobs to keep growing.",
            criteria =
                listOf(
                    "Hablé sin leer y con fluidez",
                    "Usé el vocabulario de la lección",
                    "Conecté mis ideas con ejemplos",
                ),
            selfAssessment = mapOf(0 to 2, 1 to 1),
            audioAvailable = true,
            onStart = {},
            onStop = {},
            onRate = { _, _ -> },
            onPlayModel = {},
            onSkip = {},
        )
    }
}

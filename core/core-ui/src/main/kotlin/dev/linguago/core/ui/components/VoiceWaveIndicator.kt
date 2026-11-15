package dev.linguago.core.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import dev.linguago.core.ui.theme.LinguaTokens
import dev.linguago.core.ui.util.rememberAnimationsEnabled
import kotlin.math.PI
import kotlin.math.sin

/**
 * Onda de voz del prototipo «Warm Cream & Teal» (ADR-055): barras bicolor
 * espejadas — teal arriba y terracota abajo — que pulsan mientras hay
 * reproducción TTS o escucha activa. Con reduced motion muestra una forma
 * estática; nunca es la única señal de estado (I8).
 */
@Composable
public fun VoiceWaveIndicator(
    active: Boolean,
    modifier: Modifier = Modifier,
    bars: Int = 12,
) {
    val animations = rememberAnimationsEnabled()
    val transition = rememberInfiniteTransition(label = "voiceWave")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = (2f * PI).toFloat(),
        animationSpec =
            infiniteRepeatable(
                animation = tween(durationMillis = 900, easing = LinearEasing),
            ),
        label = "voiceWavePhase",
    )
    val topColor = if (active) LinguaTokens.Primary else MaterialTheme.colorScheme.outlineVariant
    val bottomColor = if (active) LinguaTokens.Terracotta else MaterialTheme.colorScheme.outlineVariant

    Canvas(
        modifier =
            modifier
                .fillMaxWidth()
                .height(28.dp)
                .semantics {
                    contentDescription = if (active) "Reproduciendo audio" else "Audio inactivo"
                },
    ) {
        val count = bars.coerceAtLeast(1)
        val slot = size.width / count
        val barWidth = (slot * 0.5f).coerceAtLeast(1f)
        val minHeight = 3.dp.toPx()
        val maxHalf = size.height / 2f
        val centerY = size.height / 2f
        for (index in 0 until count) {
            val wave =
                when {
                    !active -> 0.12f
                    !animations -> 0.35f + 0.35f * (index % 3) / 2f
                    else -> 0.35f + 0.45f * ((sin(phase + index * 0.7f) + 1f) / 2f)
                }
            val halfLength = minHeight / 2f + wave * (maxHalf - minHeight / 2f)
            val x = index * slot + (slot - barWidth) / 2f
            val radius = CornerRadius(barWidth / 2f, barWidth / 2f)
            drawRoundRect(
                color = topColor,
                topLeft = Offset(x, centerY - halfLength),
                size = Size(barWidth, halfLength),
                cornerRadius = radius,
            )
            drawRoundRect(
                color = bottomColor,
                topLeft = Offset(x, centerY),
                size = Size(barWidth, halfLength),
                cornerRadius = radius,
            )
        }
    }
}

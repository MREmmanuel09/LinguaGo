package dev.linguago.core.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.progressSemantics
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.linguago.core.ui.theme.LinguaTheme
import dev.linguago.core.ui.theme.LinguaTokens
import dev.linguago.core.ui.util.rememberAnimationsEnabled
import kotlin.math.PI
import kotlin.math.sin

/**
 * Barra de XP con relleno "líquido" (ADR-026): el nivel de XP sube con una
 * animación suave y la superficie ondula de forma sutil. Respeta la escala de
 * animaciones del sistema y expone el rango para TalkBack (I8).
 */
@Composable
fun LiquidXpBar(
    progress: Float,
    modifier: Modifier = Modifier,
) {
    val target = progress.coerceIn(0f, 1f)
    val animated by animateFloatAsState(
        targetValue = target,
        animationSpec = tween(durationMillis = 600),
        label = "liquidXpFill",
    )
    val animations = rememberAnimationsEnabled()
    val transition = rememberInfiniteTransition(label = "liquidXpWave")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = (2f * PI).toFloat(),
        animationSpec =
            infiniteRepeatable(
                animation = tween(durationMillis = 1_400, easing = LinearEasing),
            ),
        label = "liquidXpPhase",
    )

    Canvas(
        modifier =
            modifier
                .fillMaxWidth()
                .height(12.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .progressSemantics(target),
    ) {
        if (animated <= 0f) return@Canvas
        val fillWidth = size.width * animated
        val amplitude = size.height * 0.16f
        val baseY = size.height * 0.20f
        val wavePhase = if (animations) phase else 0f
        val wavelength = (size.width / 6f).coerceAtLeast(1f)
        val path =
            Path().apply {
                moveTo(0f, size.height)
                lineTo(fillWidth, size.height)
                var x = fillWidth
                while (x >= 0f) {
                    lineTo(x, baseY + amplitude * sin(wavePhase + x / wavelength))
                    x -= 6f
                }
                close()
            }
        drawPath(path = path, color = LinguaTokens.Accent)
    }
}

@Preview(name = "XP 65%", showBackground = true)
@Composable
private fun LiquidXpBarPreview() {
    LinguaTheme {
        LiquidXpBar(progress = 0.65f)
    }
}

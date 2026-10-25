package dev.linguago.core.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import dev.linguago.core.ui.theme.LinguaTokens
import dev.linguago.core.ui.util.rememberAnimationsEnabled
import kotlin.random.Random

/**
 * Explosión de confeti ligera y determinista (ADR-026): 48 partículas en
 * Canvas, sin dependencias externas. Se dispara cuando cambia [playKey] y
 * respeta la escala de animaciones del sistema (sin movimiento decorativo si
 * están desactivadas, I8).
 */
@Composable
fun ConfettiBurst(
    playKey: Int,
    modifier: Modifier = Modifier,
) {
    val animations = rememberAnimationsEnabled()
    val progress = remember { Animatable(0f) }
    val particles = remember(playKey) { confettiParticles(playKey) }

    LaunchedEffect(playKey, animations) {
        if (playKey <= 0 || !animations) return@LaunchedEffect
        progress.snapTo(0f)
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1_200, easing = LinearEasing),
        )
    }

    Canvas(modifier = modifier) {
        if (!animations) return@Canvas
        val t = progress.value
        if (t <= 0f || t >= 1f) return@Canvas
        particles.forEach { particle ->
            val x = size.width * particle.startX + particle.vx * size.width * t
            val y =
                size.height * particle.startY +
                    particle.vy * size.height * t +
                    0.55f * size.height * t * t
            val alpha = (1f - t).coerceIn(0f, 1f)
            val side = particle.sizeDp.dp.toPx()
            rotate(degrees = particle.rotation * t, pivot = Offset(x, y)) {
                drawRect(
                    color = particle.color.copy(alpha = alpha),
                    topLeft = Offset(x, y),
                    size = Size(side, side * 0.65f),
                )
            }
        }
    }
}

private data class ConfettiParticle(
    val startX: Float,
    val startY: Float,
    val vx: Float,
    val vy: Float,
    val rotation: Float,
    val sizeDp: Float,
    val color: Color,
)

private val CONFETTI_COLORS =
    listOf(
        LinguaTokens.Primary,
        LinguaTokens.Accent,
        LinguaTokens.Warning,
        LinguaTokens.Info,
        LinguaTokens.Destructive,
        Color(LinguaTokens.CHEST_GOLD_HEX),
    )

private fun confettiParticles(playKey: Int): List<ConfettiParticle> {
    val random = Random(playKey * 31 + 7)
    return List(48) {
        ConfettiParticle(
            startX = 0.1f + random.nextFloat() * 0.8f,
            startY = 0.35f + random.nextFloat() * 0.25f,
            vx = (random.nextFloat() - 0.5f) * 0.9f,
            vy = -0.2f - random.nextFloat() * 0.6f,
            rotation = 180f + random.nextFloat() * 540f,
            sizeDp = 3f + random.nextFloat() * 4f,
            color = CONFETTI_COLORS[random.nextInt(CONFETTI_COLORS.size)],
        )
    }
}

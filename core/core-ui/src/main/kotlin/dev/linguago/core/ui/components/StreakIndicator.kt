package dev.linguago.core.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.linguago.core.ui.theme.LinguaTokens

/**
 * Indicador de racha (RFC §7.2): llama `--warning` con animación de pulso
 * suave, junto al número de días.
 */
@Composable
fun StreakIndicator(
    days: Int,
    modifier: Modifier = Modifier,
) {
    val transition = rememberInfiniteTransition(label = "streakPulse")
    val pulse by transition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec =
            infiniteRepeatable(
                animation = tween<Float>(durationMillis = 700),
                repeatMode = RepeatMode.Reverse,
            ),
        label = "streakPulseScale",
    )

    Row(
        modifier =
            modifier.semantics(mergeDescendants = true) {
                contentDescription = "Racha de $days días"
            },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Image(
            painter = painterResource(LinguaIcons.FlameRes),
            contentDescription = null,
            modifier =
                Modifier
                    .size(26.dp)
                    .graphicsLayer {
                        scaleX = pulse
                        scaleY = pulse
                    },
            contentScale = androidx.compose.ui.layout.ContentScale.Fit,
            colorFilter = ColorFilter.tint(LinguaTokens.Warning),
        )
        Text(
            text = days.toString(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onBackground,
        )
    }
}

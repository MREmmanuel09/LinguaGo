package dev.linguago.core.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.linguago.core.domain.hearts.HEARTS_MAX
import dev.linguago.core.domain.model.ThemeMode
import dev.linguago.core.ui.theme.LinguaMotion
import dev.linguago.core.ui.theme.LinguaTheme
import dev.linguago.core.ui.theme.linguaExtendedColors
import dev.linguago.core.ui.util.rememberAnimationsEnabled

/**
 * HUD de vidas (ADR-025/ADR-026): 10 corazones con un único nodo de
 * accesibilidad ("7 de 10 vidas"). El corazón recién perdido hace un latido
 * seco; sin animaciones del sistema no hay movimiento (I8).
 */
@Composable
fun HeartsIndicator(
    hearts: Int,
    modifier: Modifier = Modifier,
    max: Int = HEARTS_MAX,
) {
    val extended = linguaExtendedColors()
    val animations = rememberAnimationsEnabled()
    val clamped = hearts.coerceIn(0, max)
    val beat = remember { Animatable(1f) }

    LaunchedEffect(clamped, animations) {
        if (!animations) return@LaunchedEffect
        beat.snapTo(1f)
        beat.animateTo(targetValue = 0.82f, animationSpec = tween(durationMillis = 90))
        beat.animateTo(targetValue = 1f, animationSpec = LinguaMotion.SpringSnappy)
    }

    Row(
        modifier =
            modifier.semantics(mergeDescendants = true) {
                contentDescription = "$clamped de $max vidas"
            },
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        repeat(max) { index ->
            val filled = index < clamped
            val scale = if (animations && index == clamped) beat.value else 1f
            Icon(
                painter = painterResource(LinguaIcons.HeartRes),
                contentDescription = null,
                tint = if (filled) extended.heartRed else extended.heartEmpty,
                modifier =
                    Modifier
                        .size(20.dp)
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                        },
            )
        }
    }
}

@Preview(name = "Vidas claro", showBackground = true)
@Composable
private fun HeartsIndicatorPreview() {
    LinguaTheme {
        HeartsIndicator(hearts = 7)
    }
}

@Preview(name = "Vidas oscuro", showBackground = true)
@Composable
private fun HeartsIndicatorDarkPreview() {
    LinguaTheme(themeMode = ThemeMode.DARK) {
        HeartsIndicator(hearts = 3)
    }
}

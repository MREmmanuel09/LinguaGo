package dev.linguago.core.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.linguago.core.domain.hearts.HEARTS_MAX
import dev.linguago.core.domain.model.ThemeMode
import dev.linguago.core.ui.theme.LinguaMotion
import dev.linguago.core.ui.theme.LinguaTheme
import dev.linguago.core.ui.theme.LinguaTokens
import dev.linguago.core.ui.theme.linguaExtendedColors
import dev.linguago.core.ui.util.rememberAnimationsEnabled

/**
 * HUD de vidas numérico (ADR-037): píldora `❤ 8/10` que sustituye a la fila
 * de diez corazones para saturar menos la cabecera de lección, home y
 * repaso. Conserva la semántica única “N de max vidas” para TalkBack y el
 * latido seco al cambiar el contador (ADR-026/ADR-027); sin animaciones del
 * sistema no hay movimiento (I8).
 */
@Composable
public fun HeartsCounter(
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
            modifier
                .clip(RoundedCornerShape(LinguaTokens.RADIUS_PILL_DP.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .semantics(mergeDescendants = true) {
                    contentDescription = "$clamped de $max vidas"
                }.padding(horizontal = 10.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(LinguaIcons.HeartRes),
            contentDescription = null,
            tint = if (clamped > 0) extended.heartRed else extended.heartEmpty,
            modifier =
                Modifier
                    .size(16.dp)
                    .graphicsLayer {
                        scaleX = beat.value
                        scaleY = beat.value
                    },
        )
        Text(
            text = "$clamped/$max",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Preview(name = "Vidas claro", showBackground = true)
@Composable
private fun HeartsCounterPreview() {
    LinguaTheme {
        HeartsCounter(hearts = 7)
    }
}

@Preview(name = "Vidas oscuro", showBackground = true)
@Composable
private fun HeartsCounterDarkPreview() {
    LinguaTheme(themeMode = ThemeMode.DARK) {
        HeartsCounter(hearts = 3)
    }
}

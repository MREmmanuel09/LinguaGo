package dev.linguago.core.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.linguago.core.ui.theme.LinguaTheme
import dev.linguago.core.ui.theme.LinguaTokens
import dev.linguago.core.ui.util.rememberAnimationsEnabled

/**
 * Lumi, la mascota original de LinguaGo (ADR-026): un colibrí guía que flota
 * junto al nodo activo del camino. Es decorativa para TalkBack
 * (`contentDescription = null`) y no anima si el sistema lo desactiva (I8).
 */
@Composable
fun LumiMascot(
    modifier: Modifier = Modifier,
    size: Dp = 30.dp,
    tint: Color = LinguaTokens.Primary,
) {
    val animations = rememberAnimationsEnabled()
    val transition = rememberInfiniteTransition(label = "lumiFloat")
    val float by transition.animateFloat(
        initialValue = 0f,
        targetValue = if (animations) 1f else 0f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(durationMillis = 1_200, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse,
            ),
        label = "lumiFloatValue",
    )

    Icon(
        painter = painterResource(LinguaIcons.LumiRes),
        contentDescription = null,
        tint = tint,
        modifier =
            modifier
                .size(size)
                .graphicsLayer { translationY = -float * 3.dp.toPx() },
    )
}

@Preview(showBackground = true)
@Composable
private fun LumiMascotPreview() {
    LinguaTheme {
        LumiMascot(size = 64.dp)
    }
}

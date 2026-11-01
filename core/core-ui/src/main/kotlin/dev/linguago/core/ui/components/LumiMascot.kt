package dev.linguago.core.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.linguago.core.ui.theme.LinguaTheme
import dev.linguago.core.ui.theme.LinguaTokens
import dev.linguago.core.ui.util.rememberAnimationsEnabled

/**
 * Lumi, la mascota original de LinguaGo (ADR-026, ADR-032): un colibrí
 * multicapa que guía el camino. Es decorativa para TalkBack
 * (`contentDescription = null`) y no anima si el sistema desactiva las
 * animaciones (I8); en ese caso muestra su estado final.
 */
@Composable
fun LumiMascot(
    modifier: Modifier = Modifier,
    size: Dp = 30.dp,
    mood: LumiMood = LumiMood.IDLE,
) {
    val animations = rememberAnimationsEnabled()
    val duration =
        when (mood) {
            LumiMood.IDLE -> 1_400
            LumiMood.HAPPY -> 700
            LumiMood.ENCOURAGING -> 1_800
            LumiMood.LISTENING -> 1_000
        }
    val easing = if (mood == LumiMood.HAPPY) FastOutSlowInEasing else LinearEasing
    val transition = rememberInfiniteTransition(label = "lumi")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(durationMillis = duration, easing = easing),
                repeatMode = RepeatMode.Reverse,
            ),
        label = "lumiPhase",
    )

    val translationY =
        when {
            !animations -> 0.dp
            mood == LumiMood.HAPPY -> -(phase * 6).dp
            mood == LumiMood.ENCOURAGING -> -(phase * 1.5f).dp
            mood == LumiMood.LISTENING -> -(phase * 1f).dp
            else -> -(phase * 3).dp
        }
    val rotation =
        if (!animations) {
            0f
        } else {
            when (mood) {
                LumiMood.IDLE -> (phase - 0.5f) * 3f
                LumiMood.HAPPY -> (phase - 0.5f) * 8f
                LumiMood.ENCOURAGING -> (phase - 0.5f) * 12f
                LumiMood.LISTENING -> 0f
            }
        }
    val scale =
        when {
            !animations -> 1f
            mood == LumiMood.HAPPY -> 1f + 0.07f * phase
            mood == LumiMood.LISTENING -> 1f + 0.08f * phase
            else -> 1f
        }

    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        Image(
            painter = painterResource(LinguaIcons.LumiRes),
            contentDescription = null,
            modifier =
                Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        this.translationY = translationY.toPx()
                        rotationZ = rotation
                        scaleX = scale
                        scaleY = scale
                    },
        )
        when (mood) {
            LumiMood.HAPPY -> Sparkles(phase = if (animations) phase else 1f)
            LumiMood.LISTENING ->
                ListeningRing(
                    phase = if (animations) phase else 0f,
                )
            LumiMood.IDLE,
            LumiMood.ENCOURAGING,
            -> Unit
        }
    }
}

/** Destellos dorados de Lumi en victorias y aciertos. */
@Composable
private fun Sparkles(phase: Float) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val alpha = 0.35f + 0.65f * phase
        val radius = size.minDimension * 0.05f
        val gold = Color(LinguaTokens.CHEST_GOLD_HEX)
        drawCircle(gold.copy(alpha = alpha), radius, Offset(size.width * 0.14f, size.height * 0.20f))
        drawCircle(gold.copy(alpha = alpha * 0.8f), radius * 0.8f, Offset(size.width * 0.88f, size.height * 0.30f))
        drawCircle(gold.copy(alpha = alpha * 0.7f), radius * 0.7f, Offset(size.width * 0.80f, size.height * 0.82f))
    }
}

/** Anillo atento mientras el micrófono escucha al alumno. */
@Composable
private fun ListeningRing(phase: Float) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        drawCircle(
            color = LinguaTokens.Primary.copy(alpha = 0.20f * (1f - phase)),
            radius = size.minDimension * (0.38f + 0.16f * phase),
            style = Stroke(width = 1.5.dp.toPx()),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LumiMascotPreview() {
    LinguaTheme {
        LumiMascot(size = 64.dp, mood = LumiMood.HAPPY)
    }
}

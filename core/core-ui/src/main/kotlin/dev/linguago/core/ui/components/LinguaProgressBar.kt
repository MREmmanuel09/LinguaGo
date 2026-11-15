package dev.linguago.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.progressSemantics
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import dev.linguago.core.ui.theme.LinguaTokens

/**
 * Barra de progreso del prototipo «Warm Cream & Teal» (ADR-055): píldora de
 * 10dp sobre pista tan con relleno teal.
 */
@Composable
fun LinguaProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
) {
    val clamped = progress.coerceIn(0f, 1f)

    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(LinguaTokens.RADIUS_PILL_DP.dp))
                .background(LinguaTokens.Tan)
                .progressSemantics(clamped),
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth(fraction = clamped.coerceAtLeast(0.02f))
                    .height(10.dp)
                    .clip(RoundedCornerShape(LinguaTokens.RADIUS_PILL_DP.dp))
                    .background(LinguaTokens.Primary),
        )
    }
}

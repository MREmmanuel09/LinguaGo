package dev.linguago.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.progressSemantics
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import dev.linguago.core.ui.theme.LinguaTokens

/**
 * Barra de progreso (RFC §7.2): 8dp de alto, radio 4dp, relleno `--accent`.
 */
@Composable
fun LinguaProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
) {
    val clamped = progress.coerceIn(0f, 1f)
    val colors = MaterialTheme.colorScheme

    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(colors.surfaceVariant)
                .progressSemantics(clamped),
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth(fraction = clamped.coerceAtLeast(0.02f))
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(LinguaTokens.Accent),
        )
    }
}

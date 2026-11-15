package dev.linguago.core.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.linguago.core.ui.theme.LinguaTheme
import dev.linguago.core.ui.theme.LinguaTokens

/**
 * Píldora de racha del prototipo «Warm Cream & Teal» (ADR-055): fondo coral
 * fuerte y llama crema, «N seguidas». El relleno `CORAL_STRONG` con texto
 * crema cumple AA para texto grande/negrita (el rótulo es labelLarge bold);
 * la racha también se anuncia por el feedback de la lección (I8).
 */
@Composable
public fun StreakPill(
    count: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .clip(RoundedCornerShape(LinguaTokens.RADIUS_PILL_DP.dp))
                .background(LinguaTokens.CoralStrong)
                .semantics(mergeDescendants = true) {
                    contentDescription = "$count aciertos seguidos"
                }.padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Image(
            painter = painterResource(LinguaIcons.FlameRes),
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            colorFilter = ColorFilter.tint(LinguaTokens.OnCoralStrong),
        )
        Text(
            text = "$count seguidas",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = LinguaTokens.OnCoralStrong,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun StreakPillPreview() {
    LinguaTheme {
        StreakPill(count = 3)
    }
}

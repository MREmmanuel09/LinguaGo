package dev.linguago.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.linguago.core.ui.theme.LinguaTheme
import dev.linguago.core.ui.theme.LinguaTokens
import dev.linguago.core.ui.theme.linguaExtendedColors

/**
 * Insignia de tarea del prototipo «Warm Cream & Teal» (ADR-055): píldora
 * slate que rotula el tipo de ejercicio («Traduce esta oración», «Escucha y
 * repite», …). El texto usa `onInfoFill` para cumplir AA sobre el slate
 * (I8); el rótulo de tarea es texto grande y claro, no la única señal.
 */
@Composable
public fun TaskBadgePill(
    label: String,
    modifier: Modifier = Modifier,
) {
    val extended = linguaExtendedColors()
    Text(
        text = label,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        color = extended.onInfoFill,
        modifier =
            modifier
                .clip(RoundedCornerShape(LinguaTokens.RADIUS_PILL_DP.dp))
                .background(LinguaTokens.Info)
                .padding(horizontal = 12.dp, vertical = 6.dp),
    )
}

@Preview(showBackground = true)
@Composable
private fun TaskBadgePillPreview() {
    LinguaTheme {
        TaskBadgePill(label = "Traduce esta oración")
    }
}

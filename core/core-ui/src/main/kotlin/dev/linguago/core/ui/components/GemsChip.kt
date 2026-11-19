package dev.linguago.core.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.linguago.core.ui.theme.LinguaTheme

/**
 * Contador de gemas del prototipo «Warm Cream & Teal» (ADR-055): rombo teal
 * y número en negrita, sin fondo (hud de la ruta). Mientras la métrica no
 * existe (R6), muestra «—» con el mismo silueteado.
 */
@Composable
public fun GemsChip(
    count: Int?,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier.semantics(mergeDescendants = true) {
                contentDescription = count?.let { "$it gemas" } ?: "Gemas"
            },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Image(
            painter = painterResource(LinguaIcons.GemRes),
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
        )
        Text(
            text = count?.toString() ?: "—",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onBackground,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun GemsChipPreview() {
    LinguaTheme {
        GemsChip(count = null)
    }
}

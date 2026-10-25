package dev.linguago.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.linguago.core.ui.theme.LinguaTheme
import dev.linguago.core.ui.theme.LinguaTokens

/**
 * Tarjeta con lenguaje flat-3D (ADR-026): radio 20 dp y borde inferior sólido
 * de 4 dp, sin sombras difusas. Base para hojas de recompensa y modales.
 */
@Composable
fun LinguaCard3D(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(LinguaTokens.RADIUS_CARD_LARGE_DP.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(contentPadding),
            content = content,
        )
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(LinguaTokens.EDGE_BUTTON_DP.dp)
                    .background(MaterialTheme.colorScheme.outline),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LinguaCard3DPreview() {
    LinguaTheme {
        LinguaCard3D {
            Text(text = "Lección completada", style = MaterialTheme.typography.titleLarge)
            Text(text = "+80 XP", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

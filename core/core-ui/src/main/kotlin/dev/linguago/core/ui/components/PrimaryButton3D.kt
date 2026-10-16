package dev.linguago.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.linguago.core.ui.theme.LinguaTheme
import dev.linguago.core.ui.theme.LinguaTokens

/**
 * Botón principal con borde inferior 3D (RFC §7.2): color primario, borde
 * 4dp con `--primary-edge`, radio 16dp, objetivo táctil ≥ 48dp (I8).
 */
@Composable
fun PrimaryButton3D(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val bodyColor = if (enabled) LinguaTokens.Primary else LinguaTokens.Primary.copy(alpha = 0.45f)
    val edgeColor = if (enabled) LinguaTokens.PrimaryEdge else LinguaTokens.PrimaryEdge.copy(alpha = 0.45f)

    Column(
        modifier =
            modifier
                .clip(RoundedCornerShape(LinguaTokens.RADIUS_BUTTON_DP.dp))
                .clickable(enabled = enabled, onClick = onClick)
                .semantics { role = Role.Button },
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = 52.dp)
                    .background(bodyColor),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = text,
                color = Color.White,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleMedium,
            )
        }

        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(LinguaTokens.EDGE_BUTTON_DP.dp)
                    .background(edgeColor),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PrimaryButton3DPreview() {
    LinguaTheme {
        PrimaryButton3D(
            text = "Comenzar",
            onClick = {},
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

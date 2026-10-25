package dev.linguago.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.linguago.core.ui.theme.LinguaTheme
import dev.linguago.core.ui.theme.linguaExtendedColors

/**
 * Modal de lección perdida (ADR-025): al llegar a 0 vidas no se guarda el XP
 * ni el progreso de la sesión. La salida es inmediata y las vidas siguen
 * regenerándose; el acceso nunca se cierra de forma permanente (I3).
 */
@Composable
fun CancelledLessonSheet(
    xpLost: Int,
    onExit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val extended = linguaExtendedColors()

    Box(
        modifier =
            modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(
                painter = painterResource(LinguaIcons.HeartRes),
                contentDescription = null,
                tint = extended.heartEmpty,
                modifier = Modifier.size(64.dp),
            )
            Text(
                text = "Te quedaste sin vidas",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
            )
            Text(
                text =
                    if (xpLost > 0) {
                        "La lección se canceló y no se guardaron los $xpLost XP."
                    } else {
                        "La lección se canceló y no se guardó el progreso."
                    },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Text(
                text = "Recuperas 1 vida cada minuto; puedes intentarlo de nuevo cuando quieras.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(8.dp))
            PrimaryButton3D(
                text = "Volver al camino",
                onClick = onExit,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CancelledLessonSheetPreview() {
    LinguaTheme {
        CancelledLessonSheet(xpLost = 40, onExit = {})
    }
}

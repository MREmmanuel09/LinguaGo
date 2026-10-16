package dev.linguago.feature.sync

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.linguago.core.ui.theme.LinguaTheme

/**
 * Estado de sincronización en la nube (RFC §6, Fase 4). La app entera es
 * 100% offline (I1): esta pantalla solo gestiona el respaldo opcional.
 */
@Composable
fun SyncScreen(modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(colors.background)
                .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "Sincronización",
            style = MaterialTheme.typography.headlineMedium,
            color = colors.onBackground,
        )
        Text(
            text = "Estado actual: respaldo desactivado (Fase 4 conecta Google Drive y OneDrive con merge sin pérdidas).",
            style = MaterialTheme.typography.bodyMedium,
            color = colors.onSurfaceVariant,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SyncScreenPreview() {
    LinguaTheme {
        SyncScreen()
    }
}

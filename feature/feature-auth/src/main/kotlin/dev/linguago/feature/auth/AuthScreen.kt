package dev.linguago.feature.auth

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
 * Vinculación opcional de cuenta para sincronización (RFC §6, Fase 4).
 * Invariante I3: nunca es obligatorio; la app completa funciona sin login.
 */
@Composable
fun AuthScreen(modifier: Modifier = Modifier) {
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
            text = "Cuenta (opcional)",
            style = MaterialTheme.typography.headlineMedium,
            color = colors.onBackground,
        )
        Text(
            text = "La sincronización con Google Drive/OneDrive llega en la Fase 4.\nTu progreso vive en el dispositivo y nunca se pierde.",
            style = MaterialTheme.typography.bodyMedium,
            color = colors.onSurfaceVariant,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AuthScreenPreview() {
    LinguaTheme {
        AuthScreen()
    }
}

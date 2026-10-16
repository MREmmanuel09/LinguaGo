package dev.linguago.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.linguago.core.ui.components.OptionCard
import dev.linguago.core.ui.theme.LinguaTheme

/**
 * Ajustes generales (RFC §2.2): curso activo, modo oscuro (señal al
 * sistema FO), tamaño de texto, licencias y sincronización opcional (Fase 4).
 */
@Composable
fun SettingsScreen(
    onOpenSync: () -> Unit,
    modifier: Modifier = Modifier,
) {
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
            text = "Ajustes",
            style = MaterialTheme.typography.headlineMedium,
            color = colors.onBackground,
        )

        Text(
            text = "Preferencias de la app. La identidad y el modo claro/oscuro\nsiguen al sistema.",
            style = MaterialTheme.typography.bodySmall,
            color = colors.onSurfaceVariant,
        )

        OptionCard(
            text = "Sincronización y cuenta",
            selected = false,
            onClick = onOpenSync,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsScreenPreview() {
    LinguaTheme {
        SettingsScreen(onOpenSync = {})
    }
}

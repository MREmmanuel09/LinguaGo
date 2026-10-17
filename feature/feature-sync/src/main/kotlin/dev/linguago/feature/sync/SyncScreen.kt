package dev.linguago.feature.sync

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.linguago.core.ui.components.PrimaryButton3D
import dev.linguago.core.ui.theme.LinguaTheme

/**
 * Estado de sincronización en la nube (RFC §6, ADR-013). La app entera es
 * 100% offline (I1): esta pantalla solo gestiona el respaldo opcional y
 * queda deshabilitada mientras no haya proveedor vinculado (I3).
 */
@Composable
fun SyncScreen(
    modifier: Modifier = Modifier,
    viewModel: SyncViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    SyncContent(
        state = state,
        onBackup = viewModel::backup,
        onRestore = viewModel::restore,
        modifier = modifier,
    )
}

@Composable
private fun SyncContent(
    state: SyncUiState,
    onBackup: () -> Unit,
    onRestore: () -> Unit,
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
            text = "Sincronización",
            style = MaterialTheme.typography.headlineMedium,
            color = colors.onBackground,
        )
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(colors.surfaceVariant)
                    .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "Proveedor",
                style = MaterialTheme.typography.labelLarge,
                color = colors.onSurfaceVariant,
            )
            Text(
                text = state.providerId,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = colors.onBackground,
            )
            Text(
                text = if (state.linked) "Sesión vinculada." else "Sin sesión vinculada. El aprendizaje vive 100% en este dispositivo.",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onSurfaceVariant,
            )
        }
        PrimaryButton3D(
            text = "Respaldar ahora",
            onClick = onBackup,
            enabled = state.linked && !state.busy,
            modifier = Modifier.fillMaxWidth(),
        )
        PrimaryButton3D(
            text = "Restaurar copia",
            onClick = onRestore,
            enabled = state.linked && !state.busy,
            modifier = Modifier.fillMaxWidth(),
        )
        state.message?.let { message ->
            Spacer(Modifier.height(4.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onSurfaceVariant,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SyncScreenPreview() {
    LinguaTheme {
        SyncContent(
            state =
                SyncUiState(
                    providerId = "none",
                    linked = false,
                ),
            onBackup = {},
            onRestore = {},
        )
    }
}

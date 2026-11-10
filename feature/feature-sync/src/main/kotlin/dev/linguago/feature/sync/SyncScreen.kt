package dev.linguago.feature.sync

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.linguago.core.ui.components.LinguaCard3D
import dev.linguago.core.ui.components.LinguaTopBar
import dev.linguago.core.ui.components.PrimaryButton3D
import dev.linguago.core.ui.theme.LinguaTheme
import dev.linguago.core.ui.theme.LinguaTokens
import dev.linguago.core.ui.theme.linguaExtendedColors
import java.time.LocalDate

/**
 * Pantalla unificada de respaldo y cuenta (ADR-050): Google Drive opcional,
 * exportación/importación JSON por SAF y estado claro de cada proveedor. La
 * app funciona 100% offline (I1) y ninguna operación bloquea el aprendizaje
 * (I3).
 */
@Composable
fun SyncScreen(
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    viewModel: SyncViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val exportLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
            uri?.let(viewModel::exportTo)
        }
    val importLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            uri?.let(viewModel::importFrom)
        }
    val exportFileName = remember { "linguago-backup-${LocalDate.now()}.json" }

    LaunchedEffect(state.launchUrl) {
        val url = state.launchUrl ?: return@LaunchedEffect
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        viewModel.onBrowserOpened()
    }

    SyncContent(
        state = state,
        onBackup = viewModel::backup,
        onRestore = viewModel::restore,
        onExport = { exportLauncher.launch(exportFileName) },
        onImport = { importLauncher.launch(arrayOf("application/json", "text/plain", "application/octet-stream")) },
        onStartGoogleLink = viewModel::startGoogleLink,
        onUnlinkGoogle = viewModel::unlinkGoogle,
        onLinkDevLocal = viewModel::linkDevLocal,
        onUnlinkDevLocal = viewModel::unlinkDevLocal,
        onBack = onBack,
        modifier = modifier,
    )
}

@Composable
private fun SyncContent(
    state: SyncUiState,
    onBackup: () -> Unit,
    onRestore: () -> Unit,
    onExport: () -> Unit,
    onImport: () -> Unit,
    onStartGoogleLink: () -> Unit,
    onUnlinkGoogle: () -> Unit,
    onLinkDevLocal: () -> Unit,
    onUnlinkDevLocal: () -> Unit,
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val extended = linguaExtendedColors()

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(colors.background)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        if (onBack != null) {
            LinguaTopBar(
                title = "Respaldo y datos",
                onBack = onBack,
                subtitle = "Opcional · sin bloquear el aprendizaje",
            )
        } else {
            Text(
                text = "Respaldo y datos",
                style = MaterialTheme.typography.headlineMedium,
                color = colors.onBackground,
            )
        }
        Text(
            text =
                "Tu progreso vive en el dispositivo. Puedes guardarlo cifrado en la nube " +
                    "o exportarlo a un archivo local sin crear ninguna cuenta.",
            style = MaterialTheme.typography.bodyMedium,
            color = colors.onSurfaceVariant,
        )

        SectionTitle(text = "Respaldo en la nube")
        LinguaCard3D(contentPadding = PaddingValues(18.dp)) {
            Text(
                text = state.providerName,
                style = MaterialTheme.typography.titleLarge,
                color = colors.onSurface,
            )
            Text(
                text =
                    when {
                        state.linked -> "Cuenta vinculada. La copia se cifra antes de salir del dispositivo."
                        state.googleConfigured || state.devEnabled -> "Conecta una cuenta para activar el respaldo."
                        else -> "Esta compilación no tiene Google Drive configurado; usa la copia local."
                    },
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant,
            )
            Spacer(Modifier.height(12.dp))
            PrimaryButton3D(
                text = "Respaldar en la nube",
                onClick = onBackup,
                enabled = state.linked && !state.busy,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(8.dp))
            PrimaryButton3D(
                text = "Restaurar desde la nube",
                onClick = onRestore,
                enabled = state.linked && !state.busy,
                bodyColor = colors.surfaceVariant,
                edgeColor = colors.outline,
                contentColor = colors.onSurface,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        SectionTitle(text = "Cuenta (opcional)")
        AccountCard(
            title = "Google Drive",
            status =
                when {
                    state.googleLinked -> "Vinculada"
                    state.googleConfigured -> "No vinculada"
                    else -> "Sin configurar"
                },
            linked = state.googleLinked,
            actionText =
                when {
                    state.googleLinked -> "Desconectar Google Drive"
                    state.googleConfigured -> "Conectar Google Drive"
                    else -> "Google Drive no configurado"
                },
            enabled = (state.googleConfigured || state.googleLinked) && !state.busy,
            onClick = if (state.googleLinked) onUnlinkGoogle else onStartGoogleLink,
        )
        if (state.devEnabled) {
            AccountCard(
                title = "Laboratorio local",
                status = if (state.devLinked) "Vinculada" else "No vinculada",
                linked = state.devLinked,
                actionText =
                    if (state.devLinked) {
                        "Desconectar laboratorio"
                    } else {
                        "Conectar laboratorio"
                    },
                enabled = !state.busy,
                onClick = if (state.devLinked) onUnlinkDevLocal else onLinkDevLocal,
            )
        }

        SectionTitle(text = "Copia de seguridad en archivo local")
        LinguaCard3D(contentPadding = PaddingValues(18.dp)) {
            Text(
                text = "Sin nube, sin cuenta",
                style = MaterialTheme.typography.titleMedium,
                color = colors.onSurface,
            )
            Text(
                text =
                    "Exporta un .json con racha, XP, vidas, lecciones y tarjetas FSRS. " +
                        "Al importarlo se fusiona y nunca borra avances.",
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant,
            )
            Spacer(Modifier.height(12.dp))
            PrimaryButton3D(
                text = "Exportar a archivo…",
                onClick = onExport,
                enabled = !state.busy,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(8.dp))
            PrimaryButton3D(
                text = "Importar desde archivo…",
                onClick = onImport,
                enabled = !state.busy,
                bodyColor = colors.surfaceVariant,
                edgeColor = colors.outline,
                contentColor = colors.onSurface,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        state.message?.let { message ->
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = extended.primaryInk,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(LinguaTokens.RADIUS_CARD_DP.dp))
                        .background(colors.surfaceVariant)
                        .padding(12.dp),
            )
        }
        Text(
            text = "El estado de la app nunca depende de la nube (I1).",
            style = MaterialTheme.typography.labelSmall,
            color = colors.onSurfaceVariant,
        )
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onBackground,
    )
}

@Composable
private fun AccountCard(
    title: String,
    status: String,
    linked: Boolean,
    actionText: String,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    LinguaCard3D(contentPadding = PaddingValues(18.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = colors.onSurface,
        )
        Text(
            text = status,
            style = MaterialTheme.typography.bodySmall,
            color = if (linked) colors.primary else colors.onSurfaceVariant,
        )
        Spacer(Modifier.height(10.dp))
        PrimaryButton3D(
            text = actionText,
            onClick = onClick,
            enabled = enabled,
            bodyColor = if (linked) colors.surfaceVariant else LinguaTokens.Primary,
            edgeColor = if (linked) colors.outline else LinguaTokens.PrimaryEdge,
            contentColor = if (linked) colors.onSurface else colors.onPrimary,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SyncScreenPreview() {
    LinguaTheme {
        SyncContent(
            state =
                SyncUiState(
                    providerName = "Google Drive",
                    linked = true,
                    googleConfigured = true,
                    googleLinked = true,
                ),
            onBackup = {},
            onRestore = {},
            onExport = {},
            onImport = {},
            onStartGoogleLink = {},
            onUnlinkGoogle = {},
            onLinkDevLocal = {},
            onUnlinkDevLocal = {},
        )
    }
}

package dev.linguago.feature.auth

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.linguago.core.ui.components.PrimaryButton3D
import dev.linguago.core.ui.theme.LinguaTheme
import dev.linguago.core.ui.theme.LinguaTokens

/**
 * Vinculación opcional de cuenta para sincronización (RFC §6, ADR-011).
 * Invariante I3: nunca es obligatoria; la app completa funciona sin login.
 */
@Composable
public fun AuthScreen(modifier: Modifier = Modifier) {
    val viewModel = hiltViewModel<AuthViewModel>()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(state.launchUrl) {
        val url = state.launchUrl
        when (url == null) {
            true -> Unit
            false -> {
                context.startActivity(
                    Intent(Intent.ACTION_VIEW, Uri.parse(url)),
                )
                viewModel.onBrowserOpened()
            }
        }
    }

    AuthContent(
        state = state,
        onLinkGoogle = viewModel::startGoogleLink,
        onUnlinkGoogle = viewModel::unlinkGoogle,
        onLinkDevLocal = viewModel::linkDevLocal,
        onUnlinkDevLocal = viewModel::unlinkDevLocal,
        modifier = modifier,
    )
}

@Composable
private fun AuthContent(
    state: AuthUiState,
    onLinkGoogle: () -> Unit,
    onUnlinkGoogle: () -> Unit,
    onLinkDevLocal: () -> Unit,
    onUnlinkDevLocal: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(colors.background)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "Cuenta (opcional)",
            style = MaterialTheme.typography.headlineMedium,
            color = colors.onBackground,
        )
        Text(
            text =
                "Tu progreso vive en el dispositivo. La cuenta solo agrega" +
                    " respaldo en la nube; nunca es obligatorio.",
            style = MaterialTheme.typography.bodyMedium,
            color = colors.onSurfaceVariant,
        )

        state.entries.forEach { entry ->
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(LinguaTokens.RADIUS_CARD_DP.dp))
                        .background(colors.surfaceVariant)
                        .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = entry.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.onSurface,
                    )
                    Text(
                        text =
                            when (entry.linked) {
                                true -> "Vinculada"
                                false -> if (entry.configured) "No vinculada" else "Sin configurar"
                            },
                        style = MaterialTheme.typography.labelMedium,
                        color =
                            when (entry.linked) {
                                true -> colors.primary
                                false -> colors.onSurfaceVariant
                            },
                    )
                }
                Text(
                    text = entry.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(4.dp))
                PrimaryButton3D(
                    text =
                        when (entry.linked) {
                            true -> "Desconectar"
                            false -> "Conectar ${entry.title}"
                        },
                    onClick =
                        when (entry.providerId) {
                            "google_drive" ->
                                when (entry.linked) {
                                    true -> onUnlinkGoogle
                                    false -> onLinkGoogle
                                }
                            else ->
                                when (entry.linked) {
                                    true -> onUnlinkDevLocal
                                    false -> onLinkDevLocal
                                }
                        },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        state.message?.let { message ->
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.primary,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AuthScreenPreview() {
    LinguaTheme {
        AuthContent(
            state =
                AuthUiState(
                    entries =
                        listOf(
                            AccountEntry(
                                providerId = "google_drive",
                                title = "Google Drive",
                                subtitle = "Copia automática en tu Drive.",
                                linked = false,
                                configured = false,
                            ),
                        ),
                ),
            onLinkGoogle = {},
            onUnlinkGoogle = {},
            onLinkDevLocal = {},
            onUnlinkDevLocal = {},
        )
    }
}

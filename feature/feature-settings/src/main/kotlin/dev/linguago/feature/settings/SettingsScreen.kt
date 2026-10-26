package dev.linguago.feature.settings

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
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.linguago.core.domain.model.ThemeMode
import dev.linguago.core.ui.components.OptionCard
import dev.linguago.core.ui.feedback.FeedbackCue
import dev.linguago.core.ui.feedback.LocalInteractionFeedback
import dev.linguago.core.ui.theme.LinguaTheme
import dev.linguago.core.ui.theme.LinguaTokens

/**
 * Ajustes generales (RFC §2.2): tema claro/oscuro, feedback sensorial
 * (ADR-027), cambio de curso y sincronización opcional (Fase 4, ADR-011).
 */
@Composable
fun SettingsScreen(
    onOpenOnboarding: () -> Unit,
    onOpenSync: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    SettingsContent(
        uiState = uiState,
        onSetTheme = viewModel::setTheme,
        onSoundEffectsChanged = viewModel::setSoundEffectsEnabled,
        onHapticsChanged = viewModel::setHapticsEnabled,
        onResetCourse = { viewModel.resetCourse(onOpenOnboarding) },
        onOpenSync = onOpenSync,
        modifier = modifier,
    )
}

@Composable
private fun SettingsContent(
    uiState: SettingsUiState,
    onSetTheme: (ThemeMode) -> Unit,
    onSoundEffectsChanged: (Boolean) -> Unit,
    onHapticsChanged: (Boolean) -> Unit,
    onResetCourse: () -> Unit,
    onOpenSync: () -> Unit,
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
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "Ajustes",
            style = MaterialTheme.typography.headlineMedium,
            color = colors.onBackground,
        )

        Text(
            text = "Apariencia",
            style = MaterialTheme.typography.titleMedium,
            color = colors.onBackground,
        )
        OptionCard(
            text = "Seguir al sistema",
            selected = uiState.themeMode == ThemeMode.SYSTEM,
            onClick = { onSetTheme(ThemeMode.SYSTEM) },
            modifier = Modifier.fillMaxWidth(),
        )
        OptionCard(
            text = "Modo claro",
            selected = uiState.themeMode == ThemeMode.LIGHT,
            onClick = { onSetTheme(ThemeMode.LIGHT) },
            modifier = Modifier.fillMaxWidth(),
        )
        OptionCard(
            text = "Modo oscuro",
            selected = uiState.themeMode == ThemeMode.DARK,
            onClick = { onSetTheme(ThemeMode.DARK) },
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(8.dp))
        Text(
            text = "Sonido y vibración",
            style = MaterialTheme.typography.titleMedium,
            color = colors.onBackground,
        )
        SettingsToggleRow(
            title = "Efectos de sonido",
            description = "Selección, acierto, error y completado",
            checked = uiState.soundEffectsEnabled,
            onCheckedChange = onSoundEffectsChanged,
        )
        SettingsToggleRow(
            title = "Vibración",
            description = "Patrones suaves al interactuar con los ejercicios",
            checked = uiState.hapticsEnabled,
            onCheckedChange = onHapticsChanged,
        )

        Spacer(Modifier.height(8.dp))
        Text(
            text = "Curso",
            style = MaterialTheme.typography.titleMedium,
            color = colors.onBackground,
        )
        OptionCard(
            text =
                if (uiState.selectedCourseId != null) {
                    "Cambiar de curso"
                } else {
                    "Elegir un curso"
                },
            selected = false,
            onClick = onResetCourse,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(8.dp))
        OptionCard(
            text = "Sincronización y cuenta",
            selected = false,
            onClick = onOpenSync,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun SettingsToggleRow(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val feedback = LocalInteractionFeedback.current
    val colors = MaterialTheme.colorScheme

    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(LinguaTokens.RADIUS_CARD_DP.dp))
                .background(colors.surfaceVariant)
                .toggleable(
                    value = checked,
                    role = Role.Switch,
                    onValueChange = { enabled ->
                        feedback.play(FeedbackCue.SELECT)
                        onCheckedChange(enabled)
                    },
                ).padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = colors.onSurface,
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant,
            )
        }
        Switch(checked = checked, onCheckedChange = null)
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsScreenPreview() {
    LinguaTheme {
        SettingsContent(
            uiState =
                SettingsUiState(
                    themeMode = ThemeMode.SYSTEM,
                    selectedCourseId = "es-en",
                    soundEffectsEnabled = true,
                    hapticsEnabled = true,
                ),
            onSetTheme = {},
            onSoundEffectsChanged = {},
            onHapticsChanged = {},
            onResetCourse = {},
            onOpenSync = {},
        )
    }
}

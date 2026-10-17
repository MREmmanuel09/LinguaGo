package dev.linguago.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.linguago.core.domain.model.ThemeMode
import dev.linguago.core.ui.components.OptionCard
import dev.linguago.core.ui.theme.LinguaTheme

/**
 * Ajustes generales (RFC §2.2): tema claro/oscuro, cambio de curso
 * (onboarding) y sincronización opcional (Fase 4).
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
        onResetCourse = { viewModel.resetCourse(onOpenOnboarding) },
        onOpenSync = onOpenSync,
        modifier = modifier,
    )
}

@Composable
private fun SettingsContent(
    uiState: SettingsUiState,
    onSetTheme: (ThemeMode) -> Unit,
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
            text = "Sincronización y cuenta (Fase 4)",
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
        SettingsContent(
            uiState = SettingsUiState(themeMode = ThemeMode.SYSTEM, selectedCourseId = "es-en"),
            onSetTheme = {},
            onResetCourse = {},
            onOpenSync = {},
        )
    }
}

package dev.linguago.feature.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.linguago.core.ui.components.OptionCard
import dev.linguago.core.ui.components.PrimaryButton3D
import dev.linguago.core.ui.theme.LinguaTheme
import dev.linguago.core.ui.theme.LinguaTokens

/** Identidad original: iris de color como marca, sin arte de terceros (I5). */
@Composable
private fun BrandMark(modifier: Modifier = Modifier) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Row(modifier = Modifier.padding(2.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Box(
                modifier =
                    Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(LinguaTokens.Primary),
            )
            Box(
                modifier =
                    Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(LinguaTokens.Accent),
            )
            Box(
                modifier =
                    Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(LinguaTokens.Warning),
            )
        }
        Spacer(Modifier.width(10.dp))
        Text(
            text = "LinguaGo",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
    }
}

/**
 * Bienvenida + selección de curso (Fase 1: la lista ES el contenido
 * empaquetado y la elección persiste en DataStore antes de entrar a home).
 */
@Composable
fun OnboardingScreen(
    onCourseSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    OnboardingContent(
        uiState = uiState,
        onSelectCourse = viewModel::selectCourse,
        onConfirm = { viewModel.confirmSelection(onCourseSelected) },
        modifier = modifier,
    )
}

@Composable
private fun OnboardingContent(
    uiState: OnboardingUiState,
    onSelectCourse: (String) -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 32.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Column {
            BrandMark()
            Spacer(Modifier.height(16.dp))
            Text(
                text = "Aprende un idioma, sin esperas y sin conexión.\nTodo el contenido ya viene instalado.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Start,
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (uiState.loading) {
                Text(
                    text = "Preparando los cursos instalados…",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            uiState.tiles.forEach { tile ->
                OptionCard(
                    text = tile.subtitle,
                    selected = uiState.selectedId == tile.id,
                    onClick = { onSelectCourse(tile.id) },
                    leading = {
                        Text(
                            text = tile.title,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Spacer(Modifier.width(12.dp))
                    },
                )
            }
        }

        Column {
            PrimaryButton3D(
                text =
                    if (uiState.selectedId == null) {
                        "Elige un curso para continuar"
                    } else {
                        "Comenzar"
                    },
                onClick = onConfirm,
                enabled = uiState.selectedId != null && !uiState.saving,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun OnboardingScreenPreview() {
    LinguaTheme {
        OnboardingContent(
            uiState =
                OnboardingUiState(
                    tiles =
                        listOf(
                            OnboardingCourseTile("es-en", "Inglés", "Español → Inglés"),
                            OnboardingCourseTile("es-ja", "Japonés", "Español → Japonés"),
                        ),
                    selectedId = "es-en",
                    loading = false,
                ),
            onSelectCourse = {},
            onConfirm = {},
        )
    }
}

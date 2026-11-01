package dev.linguago.feature.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.linguago.core.ui.components.LinguaCard3D
import dev.linguago.core.ui.components.LumiMascot
import dev.linguago.core.ui.components.LumiMood
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
 * Bienvenida + selección de curso (Fase 1, ADR-035): Lumi animada, tarjetas
 * con insignia de idioma y confirmación visual al seleccionar. La elección
 * persiste en DataStore antes de entrar a home.
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
    val selected = uiState.tiles.firstOrNull { it.id == uiState.selectedId }

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 32.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                LumiMascot(size = 56.dp, mood = LumiMood.HAPPY)
                BrandMark()
            }
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
                        LanguageBadge(courseId = tile.id)
                        Spacer(Modifier.width(12.dp))
                    },
                )
            }
            AnimatedVisibility(
                visible = selected != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically(),
            ) {
                selected?.let { tile ->
                    LinguaCard3D(contentPadding = PaddingValues(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            LumiMascot(size = 26.dp, mood = LumiMood.HAPPY)
                            Text(
                                text = "Listo para empezar: ${tile.subtitle}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                }
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

/** Insignia original por idioma destino (ADR-035). */
@Composable
private fun LanguageBadge(courseId: String) {
    val style = languageStyle(courseId)
    Box(
        modifier =
            Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(style.accent),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = style.label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = style.content,
        )
    }
}

private data class LanguageStyle(
    val label: String,
    val accent: Color,
    val content: Color,
)

private fun languageStyle(courseId: String): LanguageStyle {
    val suffix = courseId.substringAfterLast('-', courseId).lowercase()
    return when (suffix) {
        "en" -> LanguageStyle("EN", LinguaTokens.Info, LinguaTokens.Fg)
        "pt" -> LanguageStyle("PT", LinguaTokens.Accent, Color.White)
        "fr" -> LanguageStyle("FR", LinguaTokens.Primary, Color.White)
        "ja" -> LanguageStyle("JA", LinguaTokens.Warning, LinguaTokens.Fg)
        else -> LanguageStyle(suffix.take(2).uppercase(), LinguaTokens.Primary, Color.White)
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

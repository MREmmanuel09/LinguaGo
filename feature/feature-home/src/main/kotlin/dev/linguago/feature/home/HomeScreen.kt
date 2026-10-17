package dev.linguago.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.linguago.core.ui.components.LinguaProgressBar
import dev.linguago.core.ui.components.StreakIndicator
import dev.linguago.core.ui.theme.LinguaTheme
import dev.linguago.core.ui.theme.LinguaTokens

/**
 * Ruta de aprendizaje del curso activo (RFC §2.3). Las tarjetas de unidad
 * muestran el avance local y abren la siguiente lección sin completar.
 */
@Composable
fun HomeScreen(
    onLessonClick: (courseId: String, lessonId: String) -> Unit,
    onStartReview: (courseId: String) -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    HomeContent(
        uiState = uiState,
        onLessonSelected = onLessonClick,
        onStartReview = onStartReview,
        modifier = modifier,
    )
}

@Composable
private fun HomeContent(
    uiState: HomeUiState,
    onLessonSelected: (String, String) -> Unit,
    onStartReview: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(colors.background)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        StreakIndicator(days = uiState.streakDays)

        Text(
            text =
                when {
                    uiState.courseTitle != null -> uiState.courseTitle
                    uiState.loading -> "Preparando tu ruta…"
                    else -> "Selecciona un curso en ajustes"
                },
            style = MaterialTheme.typography.headlineMedium,
            color = colors.onBackground,
        )

        if (uiState.loading && uiState.courseTitle == null) {
            Text(
                text = "Cargando el curso instalado…",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onSurfaceVariant,
            )
        }

        if (uiState.dueReviews > 0) {
            val courseId = uiState.courseId
            ReviewCard(
                count = uiState.dueReviews,
                onClick = { courseId?.let(onStartReview) },
            )
        }

        uiState.units.forEach { unit ->
            val lessonId = unit.nextLessonId
            val courseId = uiState.courseId
            UnitCard(
                tile = unit,
                onClick = { if (lessonId != null && courseId != null) onLessonSelected(courseId, lessonId) },
            )
        }
    }
}

@Composable
private fun ReviewCard(
    count: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme

    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(LinguaTokens.RADIUS_CARD_DP.dp))
                .background(colors.primary.copy(alpha = 0.12f))
                .clickable(onClick = onClick)
                .padding(16.dp),
    ) {
        Text(
            text = "Repasar ahora",
            style = MaterialTheme.typography.titleMedium,
            color = colors.primary,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "$count tarjetas pendientes de repaso",
            style = MaterialTheme.typography.bodySmall,
            color = colors.onSurfaceVariant,
        )
    }
}

@Composable
private fun UnitCard(
    tile: UnitTile,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val progress = if (tile.total == 0) 0f else tile.completed / tile.total.toFloat()

    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(LinguaTokens.RADIUS_CARD_DP.dp))
                .background(colors.surfaceVariant)
                .clickable(enabled = tile.nextLessonId != null, onClick = onClick)
                .padding(16.dp),
    ) {
        Text(
            text = tile.title,
            style = MaterialTheme.typography.titleMedium,
            color = colors.onSurface,
        )
        Spacer(Modifier.height(8.dp))
        LinguaProgressBar(progress = progress)
        Spacer(Modifier.height(10.dp))
        Text(
            text = "${tile.completed} de ${tile.total} lecciones",
            style = MaterialTheme.typography.bodySmall,
            color = colors.onSurfaceVariant,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    LinguaTheme {
        HomeContent(
            uiState =
                HomeUiState(
                    loading = false,
                    hasCourse = true,
                    courseId = "es-en",
                    streakDays = 3,
                    totalXp = 220,
                    courseTitle = "Español → Inglés",
                    units =
                        listOf(
                            UnitTile("un-1", "Primeros pasos", completed = 1, total = 3, nextLessonId = "ls-2"),
                            UnitTile("un-2", "Vida diaria", completed = 0, total = 3, nextLessonId = "ls-4"),
                        ),
                ),
            onLessonSelected = { _, _ -> },
            onStartReview = {},
        )
    }
}

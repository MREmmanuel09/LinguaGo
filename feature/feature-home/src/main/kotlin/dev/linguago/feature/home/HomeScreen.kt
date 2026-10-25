package dev.linguago.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.linguago.core.ui.components.ChestRewardSheet
import dev.linguago.core.ui.components.HeartsIndicator
import dev.linguago.core.ui.components.LinguaProgressBar
import dev.linguago.core.ui.components.LiquidXpBar
import dev.linguago.core.ui.components.StreakIndicator
import dev.linguago.core.ui.components.path.PathNodeState
import dev.linguago.core.ui.components.path.PathNodeUi
import dev.linguago.core.ui.theme.LinguaTheme
import dev.linguago.core.ui.theme.LinguaTokens
import dev.linguago.feature.home.path.LearningPath
import dev.linguago.feature.home.path.PathItem

/**
 * Ruta de aprendizaje del curso activo (RFC §2.3). Con [pathEnabled] se
 * muestra el camino lineal en zigzag (ADR-026); sin él, las tarjetas de
 * unidad originales se mantienen intactas como fallback.
 */
@Composable
fun HomeScreen(
    onLessonClick: (courseId: String, lessonId: String) -> Unit,
    onStartReview: (courseId: String) -> Unit = {},
    pathEnabled: Boolean = false,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    if (pathEnabled && uiState.pathItems.isNotEmpty()) {
        PathHomeContent(
            uiState = uiState,
            onLessonSelected = onLessonClick,
            onStartReview = onStartReview,
            modifier = modifier,
        )
    } else {
        HomeContent(
            uiState = uiState,
            onLessonSelected = onLessonClick,
            onStartReview = onStartReview,
            modifier = modifier,
        )
    }
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
        HomeHeader(uiState = uiState, onStartReview = onStartReview)

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

/** Camino lineal: cabecera fija + zigzag desplazable con auto-scroll al nodo activo. */
@Composable
private fun PathHomeContent(
    uiState: HomeUiState,
    onLessonSelected: (String, String) -> Unit,
    onStartReview: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    var chestUnit by remember { mutableStateOf<String?>(null) }

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(colors.background)
                .padding(horizontal = 24.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        HomeHeader(uiState = uiState, onStartReview = onStartReview, showHud = true)
        val courseId = uiState.courseId
        LearningPath(
            items = uiState.pathItems,
            onLessonClick = { lessonId -> courseId?.let { onLessonSelected(it, lessonId) } },
            onChestClick = { unitTitle -> chestUnit = unitTitle },
            modifier = Modifier.weight(1f),
        )
    }

    chestUnit?.let { unitTitle ->
        Dialog(
            onDismissRequest = { chestUnit = null },
            properties = DialogProperties(usePlatformDefaultWidth = false),
        ) {
            ChestRewardSheet(unitTitle = unitTitle, onDismiss = { chestUnit = null })
        }
    }
}

/** Cabecera común a ambos modos: racha, curso y repaso pendiente. */
@Composable
private fun HomeHeader(
    uiState: HomeUiState,
    onStartReview: (String) -> Unit,
    showHud: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            StreakIndicator(days = uiState.streakDays)
            if (showHud) {
                Spacer(Modifier.weight(1f))
                HeartsIndicator(hearts = uiState.hearts)
            }
        }

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

        if (showHud) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Nivel ${uiState.level}",
                    style = MaterialTheme.typography.labelLarge,
                    color = colors.onSurfaceVariant,
                )
                LiquidXpBar(progress = uiState.xpIntoLevel)
            }
        }

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

@Preview(showBackground = true, name = "Camino lineal")
@Composable
private fun HomePathPreview() {
    LinguaTheme {
        PathHomeContent(
            uiState =
                HomeUiState(
                    loading = false,
                    hasCourse = true,
                    courseId = "es-en",
                    streakDays = 5,
                    totalXp = 240,
                    hearts = 7,
                    level = 3,
                    xpIntoLevel = 0.4f,
                    courseTitle = "Español → Inglés",
                    pathItems =
                        listOf(
                            PathItem.Header("un-1", "Primeros pasos", completed = 2, total = 3),
                            PathItem.Node(PathNodeUi("ls-1", "Saludos", PathNodeState.COMPLETED), lessonId = "ls-1"),
                            PathItem.Node(PathNodeUi("ls-2", "Presentarse", PathNodeState.COMPLETED), lessonId = "ls-2"),
                            PathItem.Node(PathNodeUi("ls-3", "La familia", PathNodeState.AVAILABLE), lessonId = "ls-3"),
                            PathItem.Node(
                                PathNodeUi("chest:un-1", "Primeros pasos", PathNodeState.CHEST_LOCKED),
                                lessonId = null,
                            ),
                            PathItem.Header("un-2", "Vida diaria", completed = 0, total = 3),
                            PathItem.Node(PathNodeUi("ls-4", "Comida y bebida", PathNodeState.LOCKED), lessonId = "ls-4"),
                        ),
                ),
            onLessonSelected = { _, _ -> },
            onStartReview = {},
        )
    }
}

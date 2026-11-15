package dev.linguago.feature.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExtendedFloatingActionButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.linguago.core.ui.components.ChestRewardSheet
import dev.linguago.core.ui.components.CourseSwitcherSheet
import dev.linguago.core.ui.components.GemsChip
import dev.linguago.core.ui.components.HeartsCounter
import dev.linguago.core.ui.components.LinguaIcons
import dev.linguago.core.ui.components.PillButton
import dev.linguago.core.ui.components.StreakIndicator
import dev.linguago.core.ui.components.path.PathNodeState
import dev.linguago.core.ui.components.path.PathNodeUi
import dev.linguago.core.ui.feedback.FeedbackCue
import dev.linguago.core.ui.feedback.feedbackClickable
import dev.linguago.core.ui.theme.LinguaTheme
import dev.linguago.core.ui.theme.LinguaTokens
import dev.linguago.feature.home.path.LearningPath
import dev.linguago.feature.home.path.PathItem

/**
 * Ruta de aprendizaje del curso activo (RFC §2.3): camino lineal en zigzag
 * (ADR-026), con cabecera fija y auto-scroll al nodo activo.
 */
@Composable
fun HomeScreen(
    onLessonClick: (courseId: String, lessonId: String) -> Unit,
    onStartReview: (courseId: String) -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    PathHomeContent(
        uiState = uiState,
        onLessonSelected = onLessonClick,
        onStartReview = onStartReview,
        onSwitchCourse = viewModel::switchCourse,
        onClaimChest = viewModel::claimChest,
        onDismissChest = viewModel::dismissChest,
        modifier = modifier,
    )
}

/** Camino lineal: cabecera fija + zigzag desplazable con auto-scroll al nodo activo. */
@Composable
private fun PathHomeContent(
    uiState: HomeUiState,
    onLessonSelected: (String, String) -> Unit,
    onStartReview: (String) -> Unit,
    onSwitchCourse: (String) -> Unit,
    onClaimChest: (String, String) -> Unit,
    onDismissChest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    var showCourseSwitcher by remember { mutableStateOf(false) }
    val courseId = uiState.courseId
    val nextLesson =
        uiState.pathItems
            .filterIsInstance<PathItem.Node>()
            .firstOrNull { item ->
                item.lessonId != null &&
                    (item.node.state == PathNodeState.AVAILABLE || item.node.state == PathNodeState.IN_PROGRESS)
            }

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(colors.background)
                .padding(horizontal = 24.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        HomeHeader(
            uiState = uiState,
            onStartReview = onStartReview,
            onSwitchCourse = { showCourseSwitcher = true },
            showHud = true,
        )
        Box(modifier = Modifier.weight(1f)) {
            LearningPath(
                items = uiState.pathItems,
                onLessonClick = { lessonId -> courseId?.let { onLessonSelected(it, lessonId) } },
                onChestClick = { unitId, unitTitle -> onClaimChest(unitId, unitTitle) },
                modifier = Modifier.fillMaxSize(),
            )
            if (courseId != null && nextLesson?.lessonId != null) {
                ExtendedFloatingActionButton(
                    text = { Text(text = "Ir a la lección") },
                    icon = {
                        Image(
                            painter = painterResource(LinguaIcons.PlayRes),
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            colorFilter = ColorFilter.tint(colors.onPrimary),
                        )
                    },
                    onClick = { onLessonSelected(courseId, nextLesson.lessonId) },
                    containerColor = colors.primary,
                    contentColor = colors.onPrimary,
                    shape = RoundedCornerShape(LinguaTokens.RADIUS_PILL_DP.dp),
                    modifier = Modifier.align(Alignment.BottomEnd),
                )
            }
        }
    }

    if (showCourseSwitcher) {
        CourseSwitcherSheet(
            courses = uiState.courses,
            selectedId = uiState.courseId,
            onSelect = { selected ->
                showCourseSwitcher = false
                onSwitchCourse(selected)
            },
            onDismiss = { showCourseSwitcher = false },
        )
    }

    uiState.chestReward?.let { reward ->
        ChestRewardSheet(
            unitTitle = reward.unitTitle,
            xpEarned = reward.xpEarned,
            alreadyOpened = reward.alreadyOpened,
            onDismiss = onDismissChest,
        )
    }
}

/** Cabecera común: racha, curso, cambio rápido y repaso pendiente. */
@Composable
private fun HomeHeader(
    uiState: HomeUiState,
    onStartReview: (String) -> Unit,
    onSwitchCourse: () -> Unit = {},
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
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            StreakIndicator(days = uiState.streakDays)
            GemsChip(count = uiState.gems)
            HeartsCounter(hearts = uiState.hearts)
        }

        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text =
                        when {
                            uiState.courseTitle != null -> uiState.courseTitle
                            uiState.loading -> "Preparando tu ruta…"
                            else -> "Selecciona un curso en ajustes"
                        },
                    style = MaterialTheme.typography.headlineLarge,
                    color = colors.onBackground,
                    modifier = Modifier.weight(1f),
                )
                if (uiState.courseTitle != null && uiState.courses.isNotEmpty()) {
                    PillButton(text = "Cambiar", onClick = onSwitchCourse)
                }
            }
            if (uiState.courseTitle != null) {
                Text(
                    text = "Nivel ${uiState.level}",
                    style = MaterialTheme.typography.labelLarge,
                    color = colors.onSurfaceVariant,
                )
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

/** Tarjeta «Repasar ahora» del prototipo: crema, barra terracota y marcador dorado (ADR-055). */
@Composable
private fun ReviewCard(
    count: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .heightIn(min = 72.dp)
                .clip(RoundedCornerShape(LinguaTokens.RADIUS_BUTTON_DP.dp))
                .background(LinguaTokens.BgSecondary)
                .border(1.dp, LinguaTokens.TanEdge, RoundedCornerShape(LinguaTokens.RADIUS_BUTTON_DP.dp))
                .feedbackClickable(FeedbackCue.TAP, onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier =
                Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(LinguaTokens.Terracotta),
        )
        Column(
            modifier = Modifier.weight(1f).padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = "Repasar ahora",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = LinguaTokens.Fg,
            )
            Text(
                text = "$count tarjetas pendientes",
                style = MaterialTheme.typography.bodySmall,
                color = LinguaTokens.FgMuted,
            )
        }
        Image(
            painter = painterResource(LinguaIcons.StarRes),
            contentDescription = null,
            modifier = Modifier.padding(end = 16.dp).size(24.dp),
            colorFilter = ColorFilter.tint(Color(LinguaTokens.CHEST_GOLD_HEX)),
        )
    }
}

@Preview(showBackground = true)
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
            onSwitchCourse = {},
            onClaimChest = { _, _ -> },
            onDismissChest = {},
        )
    }
}

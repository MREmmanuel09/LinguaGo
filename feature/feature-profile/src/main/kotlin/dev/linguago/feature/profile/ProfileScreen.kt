package dev.linguago.feature.profile

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.progressSemantics
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.linguago.core.ui.components.LinguaCard3D
import dev.linguago.core.ui.components.LinguaIcons
import dev.linguago.core.ui.components.LiquidXpBar
import dev.linguago.core.ui.components.LumiMascot
import dev.linguago.core.ui.components.LumiMood
import dev.linguago.core.ui.theme.LinguaTheme
import dev.linguago.core.ui.theme.LinguaTokens
import dev.linguago.core.ui.util.rememberAnimationsEnabled
import kotlin.math.roundToInt

/**
 * Perfil y progreso del usuario (RFC §2.2, ADR-035): héroe con anillo de
 * dominio, cuadrícula Bento de estadísticas 3D y racha con mejor marca.
 */
@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    ProfileContent(
        uiState = uiState,
        modifier = modifier,
    )
}

@Composable
private fun ProfileContent(
    uiState: ProfileUiState,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val mastery =
        if (uiState.lessonsTotal > 0) {
            uiState.lessonsCompleted.toFloat() / uiState.lessonsTotal
        } else {
            0f
        }

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
            text = "Tu progreso",
            style = MaterialTheme.typography.headlineMedium,
            color = colors.onBackground,
        )

        LinguaCard3D(contentPadding = PaddingValues(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                MasteryRing(mastery = mastery)
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = "Dominio del curso",
                        style = MaterialTheme.typography.titleLarge,
                        color = colors.onSurface,
                    )
                    Text(
                        text =
                            if (uiState.lessonsTotal > 0) {
                                "${uiState.lessonsCompleted} de ${uiState.lessonsTotal} lecciones"
                            } else {
                                "Sin lecciones todavía"
                            },
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant,
                    )
                    Text(
                        text = "Nivel ${uiState.level}",
                        style = MaterialTheme.typography.labelLarge,
                        color = LinguaTokens.Warning,
                        fontWeight = FontWeight.Bold,
                    )
                    LiquidXpBar(progress = uiState.xpIntoLevel)
                }
                LumiMascot(
                    size = 34.dp,
                    mood = if (mastery >= 0.5f) LumiMood.HAPPY else LumiMood.IDLE,
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            StatCard(
                iconRes = LinguaIcons.StarRes,
                label = "XP acumulados",
                value = uiState.totalXp.toString(),
                accent = LinguaTokens.Warning,
                modifier = Modifier.weight(1f),
            )
            StatCard(
                iconRes = LinguaIcons.CheckRes,
                label = "Lecciones",
                value = "${uiState.lessonsCompleted}/${uiState.lessonsTotal}",
                accent = LinguaTokens.Accent,
                modifier = Modifier.weight(1f),
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            StatCard(
                iconRes = LinguaIcons.CardsRes,
                label = "Tarjetas de repaso",
                value = uiState.reviewCards.toString(),
                accent = LinguaTokens.Info,
                modifier = Modifier.weight(1f),
            )
            StatCard(
                iconRes = LinguaIcons.CardsRes,
                label = "Pendientes hoy",
                value = uiState.reviewDue.toString(),
                accent = LinguaTokens.Destructive,
                modifier = Modifier.weight(1f),
            )
        }

        StreakCard(
            current = uiState.streakDays,
            longest = uiState.longestStreak,
        )
    }
}

/** Anillo de dominio animado; expone el progreso a TalkBack (I8). */
@Composable
private fun MasteryRing(
    mastery: Float,
    modifier: Modifier = Modifier,
    ringSize: Dp = 104.dp,
) {
    val animations = rememberAnimationsEnabled()
    val animated by animateFloatAsState(
        targetValue = mastery.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = if (animations) 700 else 0),
        label = "profileMastery",
    )
    val trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.28f)
    val centerColor = MaterialTheme.colorScheme.onSurface
    val mutedColor = MaterialTheme.colorScheme.onSurfaceVariant

    Box(
        modifier =
            modifier
                .size(ringSize)
                .progressSemantics(animated),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 10.dp.toPx()
            val inset = strokeWidth / 2f
            val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)
            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
            )
            if (animated > 0f) {
                drawArc(
                    color = LinguaTokens.Accent,
                    startAngle = -90f,
                    sweepAngle = 360f * animated,
                    useCenter = false,
                    topLeft = Offset(inset, inset),
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                )
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "${(animated * 100).roundToInt()}%",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = centerColor,
            )
            Text(
                text = "dominio",
                style = MaterialTheme.typography.labelSmall,
                color = mutedColor,
            )
        }
    }
}

/** Tarjeta Bento de estadística con icono semántico (ADR-035). */
@Composable
private fun StatCard(
    iconRes: Int,
    label: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier,
) {
    LinguaCard3D(
        modifier =
            modifier.semantics(mergeDescendants = true) {
                contentDescription = "$label: $value"
            },
        contentPadding = PaddingValues(14.dp),
    ) {
        Box(
            modifier =
                Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(accent.copy(alpha = 0.16f)),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(iconRes),
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                colorFilter = ColorFilter.tint(accent),
            )
        }
        Spacer(Modifier.height(10.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/** Racha actual frente a la mejor marca, con barra de avance (ADR-035). */
@Composable
private fun StreakCard(
    current: Int,
    longest: Int,
) {
    val colors = MaterialTheme.colorScheme
    val progress = if (longest > 0) (current.toFloat() / longest).coerceIn(0f, 1f) else 0f

    LinguaCard3D(
        modifier =
            Modifier.semantics(mergeDescendants = true) {
                contentDescription = "Racha actual: $current días. Mejor racha: $longest días"
            },
        contentPadding = PaddingValues(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier =
                    Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(LinguaTokens.Warning.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(LinguaIcons.FlameRes),
                    contentDescription = null,
                    modifier = Modifier.size(22.dp),
                    colorFilter = ColorFilter.tint(LinguaTokens.Warning),
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Racha actual",
                    style = MaterialTheme.typography.labelLarge,
                    color = colors.onSurfaceVariant,
                )
                Text(
                    text = "$current días",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface,
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "Mejor racha",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.onSurfaceVariant,
                )
                Text(
                    text = "$longest días",
                    style = MaterialTheme.typography.titleMedium,
                    color = LinguaTokens.Warning,
                )
            }
        }
        Spacer(Modifier.height(10.dp))
        LiquidXpBar(progress = progress)
    }
}

@Preview(showBackground = true)
@Composable
private fun ProfileScreenPreview() {
    LinguaTheme {
        ProfileContent(
            uiState =
                ProfileUiState(
                    streakDays = 7,
                    totalXp = 320,
                    lessonsCompleted = 12,
                    lessonsTotal = 18,
                    longestStreak = 14,
                    reviewCards = 24,
                    reviewDue = 5,
                    level = 4,
                    xpIntoLevel = 0.2f,
                ),
        )
    }
}

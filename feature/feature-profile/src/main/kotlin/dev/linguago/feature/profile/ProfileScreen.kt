package dev.linguago.feature.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
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
import dev.linguago.core.ui.components.StreakIndicator
import dev.linguago.core.ui.theme.LinguaTheme
import dev.linguago.core.ui.theme.LinguaTokens

/**
 * Perfil y progreso del usuario (RFC §2.2): racha, XP y resumen de lecciones,
 * leído en vivo desde ProgressRepository.
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

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(colors.background)
                .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "Tu progreso",
            style = MaterialTheme.typography.headlineMedium,
            color = colors.onBackground,
        )

        StreakIndicator(days = uiState.streakDays)

        StatCard(
            label = "XP acumulados",
            value = uiState.totalXp.toString(),
        )
        StatCard(
            label = "Lecciones completadas",
            value =
                when {
                    uiState.lessonsTotal > 0 ->
                        "${uiState.lessonsCompleted} de ${uiState.lessonsTotal}"
                    else -> uiState.lessonsCompleted.toString()
                },
        )
        StatCard(
            label = "Tarjetas de repaso",
            value = uiState.reviewCards.toString(),
        )
        StatCard(
            label = "Repaso pendiente hoy",
            value = uiState.reviewDue.toString(),
        )
        StatCard(
            label = "Mejor racha",
            value = "${uiState.longestStreak} días",
        )
    }
}

@Composable
private fun StatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(LinguaTokens.RADIUS_CARD_DP.dp))
                .background(colors.surfaceVariant)
                .padding(16.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.onSurfaceVariant,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            color = colors.onSurface,
        )
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
                ),
        )
    }
}

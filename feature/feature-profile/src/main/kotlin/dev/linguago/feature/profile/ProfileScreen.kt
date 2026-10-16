package dev.linguago.feature.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.linguago.core.ui.components.StreakIndicator
import dev.linguago.core.ui.theme.LinguaTheme

/**
 * Perfil y progreso del usuario (RFC §2.2): racha, XP y resumen de lecciones.
 * Fase 0: esqueleto reactivo a props; la conexión con ProgressRepository
 * (Room/DataStore) llega en la Fase 1.
 */
@Composable
fun ProfileScreen(
    streakDays: Int,
    totalXp: Int,
    lessonsCompleted: Int,
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

        StreakIndicator(days = streakDays)

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "$totalXp XP acumulados",
                style = MaterialTheme.typography.titleLarge,
                color = colors.onBackground,
            )
            Text(
                text = "$lessonsCompleted lecciones completadas",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onSurfaceVariant,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ProfileScreenPreview() {
    LinguaTheme {
        ProfileScreen(streakDays = 7, totalXp = 320, lessonsCompleted = 12)
    }
}

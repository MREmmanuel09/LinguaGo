package dev.linguago.feature.home

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.linguago.core.ui.components.LinguaProgressBar
import dev.linguago.core.ui.components.StreakIndicator
import dev.linguago.core.ui.theme.LinguaTheme
import dev.linguago.core.ui.theme.LinguaTokens

/**
 * Ruta de aprendizaje del curso activo (RFC §8.5). En Fase 0 la pantalla
 * muestra el esqueleto con el estado real de racha y una tarjeta de
 * progreso vacía; la carga de unidades llega con el contenido ES→EN A1 (Fase 3).
 */
@Composable
fun HomeScreen(
    streakDays: Int,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(colors.background)
                .padding(horizontal = 24.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        StreakIndicator(days = streakDays)

        Text(
            text = "Tu ruta de aprendizaje",
            style = MaterialTheme.typography.headlineMedium,
            color = colors.onBackground,
        )

        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(LinguaTokens.RADIUS_CARD_DP.dp))
                    .background(colors.surfaceVariant)
                    .padding(16.dp),
        ) {
            Text(
                text = "Unidad 1 · Nivel A1",
                style = MaterialTheme.typography.titleMedium,
                color = colors.onSurface,
            )
            Spacer(Modifier.height(8.dp))
            LinguaProgressBar(progress = 0f)
            Spacer(Modifier.height(10.dp))
            Text(
                text = "El contenido ES→EN A1 se carga en la Fase 1 del proyecto.",
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    LinguaTheme {
        HomeScreen(streakDays = 3)
    }
}

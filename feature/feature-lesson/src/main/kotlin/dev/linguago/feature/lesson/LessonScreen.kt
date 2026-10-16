package dev.linguago.feature.lesson

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.linguago.core.ui.components.LinguaProgressBar
import dev.linguago.core.ui.theme.LinguaTheme

/**
 * Motor de lección (RFC §2.3 — FSM, ADR-002/007). Fase 0: esqueleto con
 * barra de progreso y enunciado de ejemplo; la máquina de estados y los
 * ejercicios se implementan en Fase 1.
 */
@Composable
fun LessonScreen(modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(colors.background)
                .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        LinguaProgressBar(progress = 0f)

        Text(
            text = "Lección en construcción",
            style = MaterialTheme.typography.headlineMedium,
            color = colors.onBackground,
        )

        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Aquí vivirán los ejercicios del motor de lección:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurfaceVariant,
                )
                Text(
                    text = "translate · multiple_choice · order_words · match_pairs · listen_choose …",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        }

        Box(
            Modifier
                .fillMaxWidth()
                .height(4.dp)
                .background(colors.outline),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LessonScreenPreview() {
    LinguaTheme {
        LessonScreen()
    }
}

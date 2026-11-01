package dev.linguago.feature.lesson

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.linguago.core.ui.components.LinguaCard3D
import dev.linguago.core.ui.components.LumiMascot
import dev.linguago.core.ui.components.LumiMood
import dev.linguago.core.ui.components.PrimaryButton3D
import dev.linguago.core.ui.feedback.FeedbackCue
import dev.linguago.core.ui.feedback.LocalInteractionFeedback
import dev.linguago.core.ui.theme.LinguaTheme

/**
 * Ventana de corrección antes de cerrar la lección (ADR-036): repite solo los
 * ejercicios fallados o permite ver la tabla final sin repetir. Nunca bloquea
 * la salida (I3).
 */
@Composable
fun MistakeReviewSheet(
    mistakeCount: Int,
    total: Int,
    onRetry: () -> Unit,
    onSeeResults: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val feedback = LocalInteractionFeedback.current

    Box(
        modifier =
            modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center,
    ) {
        LinguaCard3D(modifier = Modifier.padding(24.dp)) {
            LumiMascot(
                size = 44.dp,
                mood = LumiMood.ENCOURAGING,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Corrige tus errores",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text =
                    "Fallaste $mistakeCount de $total ejercicios. " +
                        "Repítelos para afianzar antes de cerrar la lección.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(20.dp))
            PrimaryButton3D(
                text = "Repetir errores",
                onClick = onRetry,
                modifier = Modifier.fillMaxWidth(),
            )
            TextButton(
                onClick = {
                    feedback.play(FeedbackCue.TAP)
                    onSeeResults()
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(text = "Ver resultados")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MistakeReviewSheetPreview() {
    LinguaTheme {
        MistakeReviewSheet(
            mistakeCount = 2,
            total = 10,
            onRetry = {},
            onSeeResults = {},
        )
    }
}

package dev.linguago.core.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.linguago.core.ui.theme.LinguaTheme
import dev.linguago.core.ui.util.rememberAnimationsEnabled
import kotlin.math.roundToInt

/**
 * Modal de victoria (ADR-026): XP con contador animado, confeti y salida
 * inmediata al camino. Sin esperas ni pasos obligatorios (I3).
 */
@Composable
fun RewardSheet(
    xpEarned: Int,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
    correct: Int? = null,
    incorrect: Int? = null,
    percentage: Int? = null,
) {
    val animations = rememberAnimationsEnabled()
    val counter = remember { Animatable(0f) }

    LaunchedEffect(xpEarned, animations) {
        val target = xpEarned.coerceAtLeast(0).toFloat()
        if (!animations) {
            counter.snapTo(target)
            return@LaunchedEffect
        }
        counter.snapTo(0f)
        counter.animateTo(target, animationSpec = tween(durationMillis = 900))
    }

    Box(
        modifier =
            modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center,
    ) {
        ConfettiBurst(playKey = 1, modifier = Modifier.fillMaxSize())
        LinguaCard3D(modifier = Modifier.padding(24.dp)) {
            LumiMascot(
                size = 44.dp,
                mood = LumiMood.HAPPY,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "¡Lección completada!",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = "+${counter.value.roundToInt()} XP",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(8.dp))
            if (percentage != null) {
                ScoreTable(
                    correct = correct ?: 0,
                    incorrect = incorrect ?: 0,
                    percentage = percentage,
                )
                Spacer(Modifier.height(12.dp))
            }
            Text(
                text = "Cada acierto te acerca al siguiente nivel.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(20.dp))
            PrimaryButton3D(
                text = "Volver al camino",
                onClick = onContinue,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

/** Tabla de resultados sobre el primer intento de cada ejercicio (ADR-036). */
@Composable
private fun ScoreTable(
    correct: Int,
    incorrect: Int,
    percentage: Int,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        ScoreRow(label = "Aciertos", value = correct.toString())
        ScoreRow(label = "Errores", value = incorrect.toString())
        ScoreRow(label = "Nota final", value = "$percentage %", emphasized = true)
    }
}

@Composable
private fun ScoreRow(
    label: String,
    value: String,
    emphasized: Boolean = false,
) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.onSurfaceVariant,
        )
        Text(
            text = value,
            style =
                if (emphasized) {
                    MaterialTheme.typography.titleMedium
                } else {
                    MaterialTheme.typography.bodyMedium
                },
            fontWeight = if (emphasized) FontWeight.Bold else FontWeight.Medium,
            color = if (emphasized) colors.primary else colors.onSurface,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun RewardSheetPreview() {
    LinguaTheme {
        RewardSheet(xpEarned = 80, correct = 8, incorrect = 2, percentage = 80, onContinue = {})
    }
}

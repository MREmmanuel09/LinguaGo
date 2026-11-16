package dev.linguago.feature.placement

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.linguago.core.domain.model.CefrLevel
import dev.linguago.core.ui.components.InfoBanner
import dev.linguago.core.ui.components.LinguaProgressBar
import dev.linguago.core.ui.components.OptionCard
import dev.linguago.core.ui.components.OptionStatus
import dev.linguago.core.ui.components.PillButton
import dev.linguago.core.ui.components.PrimaryButton3D

@Composable
public fun PlacementScreen(
    onExit: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PlacementViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = MaterialTheme.colorScheme

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(colors.background)
                .padding(horizontal = 20.dp, vertical = 16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "Examen de nivelación",
                style = MaterialTheme.typography.titleLarge,
                color = colors.onBackground,
            )
            PillButton(text = "Salir", onClick = onExit)
        }
        Spacer(Modifier.height(12.dp))

        when {
            uiState.loading -> {
                InfoBanner(message = "Preparando examen…")
            }
            uiState.error != null -> {
                InfoBanner(message = uiState.error!!)
                Spacer(Modifier.height(12.dp))
                PrimaryButton3D(text = "Reintentar", onClick = viewModel::retry, modifier = Modifier.fillMaxWidth())
            }
            uiState.finished && uiState.result != null -> {
                PlacementResultView(
                    result = uiState.result!!,
                    applying = uiState.applying,
                    applied = uiState.applied,
                    unlockedCount = uiState.unlockedCount,
                    applyError = uiState.applyError,
                    onApply = viewModel::applyPlacement,
                    onRetry = viewModel::retry,
                    onExit = onExit,
                )
            }
            else -> {
                val question = uiState.currentQuestion()
                val exam = uiState.exam
                if (question == null || exam == null || exam.totalQuestions == 0) {
                    InfoBanner(message = "No hay preguntas disponibles para este idioma.")
                    Spacer(Modifier.height(12.dp))
                    PrimaryButton3D(text = "Volver", onClick = onExit, modifier = Modifier.fillMaxWidth())
                } else {
                    PlacementQuestionView(
                        uiState = uiState,
                        onSelect = viewModel::selectOption,
                        onSubmit = viewModel::submitAnswer,
                        onNext = viewModel::next,
                    )
                }
            }
        }
    }
}

@Composable
private fun PlacementQuestionView(
    uiState: PlacementUiState,
    onSelect: (Int) -> Unit,
    onSubmit: () -> Unit,
    onNext: () -> Unit,
) {
    val question = uiState.currentQuestion() ?: return
    val level = uiState.currentLevel() ?: CefrLevel.A1
    val (qNum, qTotal) = uiState.progressInBlock()
    val (prompt, options) = question.exercise.optionPromptAndChoices() ?: ("" to emptyList())
    val correctIndex = question.exercise.optionCorrectIndex()

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "Nivel ${level.name} · Pregunta $qNum de $qTotal",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        LinguaProgressBar(
            progress = if (qTotal == 0) 0f else qNum / qTotal.toFloat(),
            modifier = Modifier.fillMaxWidth(),
        )
        Text(
            text = "Responde según tu nivel. El examen avanza de fácil a difícil y puede desbloquear niveles.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = prompt,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
        )
        options.forEachIndexed { index, option ->
            val status =
                when {
                    !uiState.answered -> OptionStatus.NEUTRAL
                    index == correctIndex -> OptionStatus.CORRECT
                    uiState.selectedIndex == index -> OptionStatus.INCORRECT
                    else -> OptionStatus.NEUTRAL
                }
            OptionCard(
                text = option,
                selected = uiState.selectedIndex == index,
                enabled = !uiState.answered,
                status = status,
                onClick = { onSelect(index) },
            )
        }

        Spacer(Modifier.height(8.dp))
        if (!uiState.answered) {
            PrimaryButton3D(
                text = "Comprobar",
                onClick = onSubmit,
                enabled = uiState.selectedIndex != null,
                modifier = Modifier.fillMaxWidth(),
            )
        } else {
            val feedback = if (uiState.lastCorrect == true) "¡Correcto!" else "Respuesta incorrecta"
            InfoBanner(message = feedback)
            PrimaryButton3D(
                text = "Continuar",
                onClick = onNext,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun PlacementResultView(
    result: dev.linguago.core.domain.placement.PlacementResult,
    applying: Boolean,
    applied: Boolean,
    unlockedCount: Int,
    applyError: String?,
    onApply: () -> Unit,
    onRetry: () -> Unit,
    onExit: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "Resultado del examen",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold,
        )
        val achieved = result.achievedLevel
        Text(
            text =
                if (achieved != null) {
                    "Nivel alcanzado: ${achieved.name}"
                } else {
                    "Aún no se alcanzó un nivel para desbloquear."
                },
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = "Puntaje total: ${result.totalCorrect}/${result.totalQuestions} (${result.percentage}%)",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )

        result.levelResults.forEach { level ->
            val label =
                "${level.level.name}: ${level.correct}/${level.total} (${level.percentage}%)" +
                    if (level.passed) " · aprobado" else ""
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color =
                    if (level.passed) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
            )
        }

        if (applyError != null) {
            InfoBanner(message = applyError)
        }

        Spacer(Modifier.height(8.dp))
        if (!applied) {
            PrimaryButton3D(
                text =
                    if (result.unlockedLessonIds.isEmpty()) {
                        "Continuar"
                    } else {
                        "Desbloquear ${result.unlockedLessonIds.size} lecciones"
                    },
                onClick = {
                    if (result.unlockedLessonIds.isEmpty()) onExit() else onApply()
                },
                enabled = !applying,
                modifier = Modifier.fillMaxWidth(),
            )
        } else {
            InfoBanner(
                message =
                    if (unlockedCount > 0) {
                        "Se desbloquearon $unlockedCount lecciones nuevas (sin quitar tu progreso)."
                    } else if (result.unlockedLessonIds.isEmpty()) {
                        "No hay cambios de desbloqueo."
                    } else {
                        "Los niveles ya estaban desbloqueados. Tu progreso se mantiene."
                    },
            )
            PrimaryButton3D(text = "Ir al camino", onClick = onExit, modifier = Modifier.fillMaxWidth())
        }

        TextButton(onClick = onRetry, modifier = Modifier.align(Alignment.CenterHorizontally)) {
            Text("Repetir examen (preguntas nuevas)")
        }
    }
}

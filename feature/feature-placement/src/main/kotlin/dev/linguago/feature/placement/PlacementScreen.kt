package dev.linguago.feature.placement

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.linguago.core.domain.logic.ClozePassageParser
import dev.linguago.core.domain.model.CefrLevel
import dev.linguago.core.domain.model.Exercise
import dev.linguago.core.ui.components.AudioPlayerCard
import dev.linguago.core.ui.components.ClozeDragDropBoard
import dev.linguago.core.ui.components.InfoBanner
import dev.linguago.core.ui.components.LinguaProgressBar
import dev.linguago.core.ui.components.MicListeningButton
import dev.linguago.core.ui.components.OptionCard
import dev.linguago.core.ui.components.OptionStatus
import dev.linguago.core.ui.components.PillButton
import dev.linguago.core.ui.components.PrimaryButton3D
import dev.linguago.core.ui.components.PuzzleMatchBoard
import dev.linguago.core.ui.components.PuzzlePairUi
import dev.linguago.core.ui.components.TokenChip

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
            uiState.loading -> InfoBanner(message = "Preparando examen…")
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
                        canSubmit = viewModel.canSubmit(uiState),
                        onSelectOption = viewModel::selectOption,
                        onTypedText = viewModel::updateTypedText,
                        onToggleToken = viewModel::toggleToken,
                        onSelectLeft = viewModel::selectLeft,
                        onMatchRight = viewModel::matchRight,
                        onUnpair = viewModel::unpair,
                        onFillCloze = viewModel::fillClozeBlank,
                        onClearCloze = viewModel::clearClozeBlank,
                        onPlay = viewModel::playAudio,
                        onPlaySlow = viewModel::playAudioSlow,
                        onTranscribe = viewModel::transcribeSpoken,
                        onStopSpeech = viewModel::stopSpoken,
                        onSubmit = viewModel::submitAnswer,
                        onNext = viewModel::next,
                        onFinishEarly = viewModel::finishEarly,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PlacementQuestionView(
    uiState: PlacementUiState,
    canSubmit: Boolean,
    onSelectOption: (Int) -> Unit,
    onTypedText: (String) -> Unit,
    onToggleToken: (Int) -> Unit,
    onSelectLeft: (Int) -> Unit,
    onMatchRight: (Int) -> Unit,
    onUnpair: (Int) -> Unit,
    onFillCloze: (String, String) -> Unit,
    onClearCloze: (String) -> Unit,
    onPlay: () -> Unit,
    onPlaySlow: () -> Unit,
    onTranscribe: () -> Unit,
    onStopSpeech: () -> Unit,
    onSubmit: () -> Unit,
    onNext: () -> Unit,
    onFinishEarly: () -> Unit,
) {
    val question = uiState.currentQuestion() ?: return
    val level = uiState.currentLevel() ?: CefrLevel.A1
    val (qNum, qTotal) = uiState.progressInBlock()
    val exercise = question.exercise
    val context = LocalContext.current
    var micDenied by remember { mutableStateOf(false) }
    val micPermissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            micDenied = !granted
            if (granted) onTranscribe()
        }
    val micPermission = Manifest.permission.RECORD_AUDIO
    val requestDictation: () -> Unit = {
        if (ContextCompat.checkSelfPermission(context, micPermission) == PackageManager.PERMISSION_GRANTED) {
            micDenied = false
            onTranscribe()
        } else {
            micPermissionLauncher.launch(micPermission)
        }
    }
    var confirmFinish by remember { mutableStateOf(false) }

    if (confirmFinish) {
        AlertDialog(
            onDismissRequest = { confirmFinish = false },
            title = { Text("Finalizar examen") },
            text = {
                Text(
                    "Se evaluará lo respondido hasta ahora y se desbloquearán las lecciones " +
                        "del nivel que hayas demostrado. No perderás progreso.",
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmFinish = false
                        onFinishEarly()
                    },
                ) { Text("Finalizar") }
            },
            dismissButton = {
                TextButton(onClick = { confirmFinish = false }) { Text("Seguir examinando") }
            },
        )
    }

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
            text = "Responde según tu nivel. El examen avanza de fácil a difícil y combina distintos tipos de ejercicios.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        ExercisePrompt(exercise = exercise)

        if (exercise is Exercise.ListenChoose || exercise is Exercise.AudioOnlyPrompt || exercise is Exercise.ListenType) {
            AudioPlayerCard(
                title =
                    when {
                        uiState.voiceWarmingUp -> "Preparando la voz…"
                        uiState.audioAvailable -> "Escucha el audio"
                        else -> "Voz no disponible en este dispositivo"
                    },
                playing = uiState.speaking,
                enabled = uiState.audioAvailable && !uiState.speaking,
                onPlay = onPlay,
                onPlaySlow = onPlaySlow,
            )
        }

        when (exercise) {
            is Exercise.MultipleChoice,
            is Exercise.ListenChoose,
            is Exercise.AudioOnlyPrompt,
            is Exercise.KanaChoose,
            is Exercise.KanjiReading,
            is Exercise.ParticleFill,
            -> {
                val options = exercise.displayOptions(uiState.displayToOriginal)
                val correctOriginal = exercise.correctOriginalIndex()
                options.forEachIndexed { displayIndex, option ->
                    val original = uiState.displayToOriginal.getOrNull(displayIndex) ?: displayIndex
                    val status =
                        when {
                            !uiState.answered -> OptionStatus.NEUTRAL
                            original == correctOriginal -> OptionStatus.CORRECT
                            uiState.selectedOriginalIndex == original -> OptionStatus.INCORRECT
                            else -> OptionStatus.NEUTRAL
                        }
                    OptionCard(
                        text = option,
                        selected = uiState.selectedDisplayIndex == displayIndex,
                        enabled = !uiState.answered,
                        status = status,
                        onClick = { onSelectOption(displayIndex) },
                    )
                }
            }
            is Exercise.ListenType -> {
                if (uiState.speechAvailable) {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center,
                    ) {
                        MicListeningButton(
                            listening = uiState.listening,
                            onClick = requestDictation,
                            enabled = !uiState.listening && !uiState.answered,
                            contentDescription = "Dictar respuesta",
                        )
                    }
                    Text(
                        text =
                            when {
                                uiState.listening -> "Escuchando… toca «Detener» al terminar"
                                uiState.answered -> "Respuesta enviada"
                                else -> "También puedes dictar tu respuesta con el micrófono"
                            },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                    )
                    if (uiState.listening) {
                        PrimaryButton3D(
                            text = "Detener grabación",
                            onClick = onStopSpeech,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
                if (micDenied) {
                    InfoBanner(
                        message = "Permiso de micrófono denegado. Puedes escribir el dictado a mano.",
                    )
                }
                OutlinedTextField(
                    value = uiState.typedText,
                    onValueChange = onTypedText,
                    enabled = !uiState.answered,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Escribe lo que escuchas") },
                    minLines = 2,
                )
                if (uiState.answered) {
                    val correct = ExerciseGraderLocal.isCorrect(exercise, uiState.typedText)
                    InfoBanner(message = if (correct) "Respuesta correcta" else "Respuesta: ${exercise.transcription}")
                }
            }
            is Exercise.Translate,
            is Exercise.OrderWords,
            -> {
                TokenAnswerArea(
                    available = uiState.availableTokens,
                    chosen = uiState.chosenTokens,
                    enabled = !uiState.answered,
                    answered = uiState.answered,
                    correct = uiState.lastCorrect == true,
                    onToggle = onToggleToken,
                )
            }
            is Exercise.MatchPairs -> {
                PuzzleMatchBoard(
                    pairs = exercise.pairs.map { PuzzlePairUi(left = it.left, right = it.right) },
                    shuffledRights = uiState.shuffledRights,
                    selectedLeft = uiState.selectedLeft,
                    matched = uiState.matched,
                    onSelectLeft = onSelectLeft,
                    onMatchRight = onMatchRight,
                    onUnpair = onUnpair,
                    enabled = !uiState.answered,
                    result = if (uiState.answered) uiState.lastCorrect else null,
                )
            }
            is Exercise.SemanticMatch -> {
                PuzzleMatchBoard(
                    pairs =
                        exercise.pairs.map {
                            PuzzlePairUi(
                                left = it.left,
                                right = it.right,
                                rightBadge = it.register?.name,
                                rightDetail = it.nuance,
                            )
                        },
                    shuffledRights = uiState.shuffledRights,
                    selectedLeft = uiState.selectedLeft,
                    matched = uiState.matched,
                    onSelectLeft = onSelectLeft,
                    onMatchRight = onMatchRight,
                    onUnpair = onUnpair,
                    enabled = !uiState.answered,
                    result = if (uiState.answered) uiState.lastCorrect else null,
                )
            }
            is Exercise.ClozeDragDrop -> {
                val segments = remember(exercise.id) { ClozePassageParser.parse(exercise.passage) }
                ClozeDragDropBoard(
                    segments = segments,
                    options = exercise.blanks.flatMap { it.options }.distinct(),
                    filled = uiState.clozeFilled,
                    onAssign = onFillCloze,
                    onClearBlank = onClearCloze,
                    enabled = !uiState.answered,
                )
            }
            else -> InfoBanner(message = "Tipo de ejercicio no disponible en este examen.")
        }

        Spacer(Modifier.height(8.dp))
        if (!uiState.answered) {
            PrimaryButton3D(
                text = "Comprobar",
                onClick = onSubmit,
                enabled = canSubmit,
                modifier = Modifier.fillMaxWidth(),
            )
        } else {
            val feedback =
                when {
                    uiState.lastCorrect == true -> "¡Correcto!"
                    exercise is Exercise.ListenType -> "Respuesta incorrecta"
                    else -> "Respuesta incorrecta"
                }
            InfoBanner(message = feedback)
            PrimaryButton3D(
                text = "Continuar",
                onClick = onNext,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        TextButton(
            onClick = { confirmFinish = true },
            modifier = Modifier.align(Alignment.CenterHorizontally),
        ) {
            Text("Finalizar examen con lo respondido")
        }
    }
}

@Composable
private fun ExercisePrompt(exercise: Exercise) {
    val prompt =
        when (exercise) {
            is Exercise.MultipleChoice -> exercise.question
            is Exercise.ListenChoose -> "Escucha y elige la opción correcta"
            is Exercise.AudioOnlyPrompt -> exercise.question ?: "Escucha y elige la opción correcta"
            is Exercise.KanaChoose -> "Lee el kana: ${exercise.kana}"
            is Exercise.KanjiReading -> "Lectura de: ${exercise.kanji}"
            is Exercise.ParticleFill -> "Completa la oración: ${exercise.sentenceTemplate}"
            is Exercise.ListenType -> "Escucha y escribe lo que oyes"
            is Exercise.Translate -> "Traduce: ${exercise.sourcePhrase}"
            is Exercise.OrderWords -> exercise.sourcePhrase?.let { "Ordena: $it" } ?: "Ordena las palabras para formar la frase"
            is Exercise.MatchPairs -> "Empareja las columnas"
            is Exercise.SemanticMatch -> exercise.instructions ?: "Empareja cada expresión con su equivalente"
            is Exercise.ClozeDragDrop -> "Completa los espacios en blanco"
            else -> ""
        }
    if (prompt.isNotBlank()) {
        Text(
            text = prompt,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TokenAnswerArea(
    available: List<PlacementToken>,
    chosen: List<PlacementToken>,
    enabled: Boolean,
    answered: Boolean,
    correct: Boolean,
    onToggle: (Int) -> Unit,
) {
    val status =
        when {
            !answered -> OptionStatus.NEUTRAL
            correct -> OptionStatus.CORRECT
            else -> OptionStatus.INCORRECT
        }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Tu respuesta", style = MaterialTheme.typography.labelLarge)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (chosen.isEmpty()) {
                Text(
                    "Toca las palabras para construir la respuesta",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                chosen.forEach { token ->
                    TokenChip(
                        token = token.text,
                        selected = true,
                        enabled = enabled,
                        onToggle = { onToggle(token.id) },
                        status = status,
                    )
                }
            }
        }
        Spacer(Modifier.height(4.dp))
        Text("Palabras disponibles", style = MaterialTheme.typography.labelLarge)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            available.forEach { token ->
                TokenChip(
                    token = token.text,
                    selected = false,
                    enabled = enabled,
                    onToggle = { onToggle(token.id) },
                )
            }
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
            text = "Puntaje: ${result.totalCorrect}/${result.totalAttempted} respondidas (${result.percentage}%)",
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

        if (applyError != null) InfoBanner(message = applyError)

        Spacer(Modifier.height(8.dp))
        if (!applied) {
            PrimaryButton3D(
                text =
                    if (result.unlockedLessonIds.isEmpty()) {
                        "Continuar"
                    } else {
                        "Desbloquear ${result.unlockedLessonIds.size} lecciones"
                    },
                onClick = { if (result.unlockedLessonIds.isEmpty()) onExit() else onApply() },
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

/** Pequeño helper para feedback en ListenType sin exponer grader en UI compleja. */
private object ExerciseGraderLocal {
    fun isCorrect(
        exercise: Exercise.ListenType,
        text: String,
    ): Boolean =
        dev.linguago.core.domain.logic.ExerciseGrader.isCorrect(
            exercise,
            dev.linguago.core.domain.usecase.ExerciseAnswer
                .TypedText(text),
        )
}

package dev.linguago.feature.lesson

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.linguago.core.domain.logic.ClozePassageParser
import dev.linguago.core.domain.model.Exercise
import dev.linguago.core.domain.model.ExerciseId
import dev.linguago.core.ui.components.ClozeDragDropBoard
import dev.linguago.core.ui.components.FeedbackBanner
import dev.linguago.core.ui.components.InfoBanner
import dev.linguago.core.ui.components.LinguaProgressBar
import dev.linguago.core.ui.components.OptionCard
import dev.linguago.core.ui.components.PrimaryButton3D
import dev.linguago.core.ui.haptics.rememberLinguaHaptics
import dev.linguago.core.ui.theme.LinguaTheme
import dev.linguago.core.ui.theme.LinguaTokens

/**
 * Motor de lección (RFC §2.3, ADR-002). Renderiza cada ejercicio según su
 * tipo: opciones listadas, tablero de tokens, dictado libre o parejas.
 * Tras responder muestra FeedbackBanner y avanza con "Continuar".
 */
@Composable
fun LessonScreen(
    onExit: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LessonViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    /** SpeakRepeat: pide el permiso de micrófono antes de escuchar (I3). */
    val context = LocalContext.current
    val micPermission = Manifest.permission.RECORD_AUDIO
    val micLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) viewModel.transcribeSpoken()
        }
    val onSpeakRequest: () -> Unit = {
        if (ContextCompat.checkSelfPermission(context, micPermission) == PackageManager.PERMISSION_GRANTED) {
            viewModel.transcribeSpoken()
        } else {
            micLauncher.launch(micPermission)
        }
    }

    LessonContent(
        uiState = uiState,
        onPickOption = viewModel::pickOption,
        onToggleToken = viewModel::toggleToken,
        onSubmitTokens = viewModel::submitTokens,
        onMatchLeft = viewModel::selectLeft,
        onMatchRight = viewModel::matchLeftTo,
        onSubmitPairs = viewModel::submitMatchedPairs,
        onFillCloze = viewModel::fillClozeBlank,
        onClearCloze = viewModel::clearClozeBlank,
        onSubmitCloze = viewModel::submitCloze,
        onTypedText = viewModel::submitTypedText,
        onSpeakRequest = onSpeakRequest,
        onSkipSpeech = viewModel::skipSpeech,
        onAdvance = viewModel::advance,
        onPlayAudio = viewModel::playAudio,
        onExit = onExit,
        modifier = modifier,
    )
}

@Composable
private fun LessonContent(
    uiState: LessonUiState,
    onPickOption: (Int) -> Unit,
    onToggleToken: (String) -> Unit,
    onSubmitTokens: () -> Unit,
    onMatchLeft: (Int) -> Unit,
    onMatchRight: (Int) -> Unit,
    onSubmitPairs: () -> Unit,
    onFillCloze: (String, String) -> Unit,
    onClearCloze: (String) -> Unit,
    onSubmitCloze: () -> Unit,
    onTypedText: (String) -> Unit,
    onSpeakRequest: () -> Unit,
    onSkipSpeech: () -> Unit,
    onAdvance: () -> Unit,
    onPlayAudio: () -> Unit,
    onExit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when {
        uiState.loading -> Loading(modifier)
        uiState.invalid -> Invalid(modifier, onExit)
        uiState.finished -> Finish(uiState, onExit, modifier)
        else ->
            ExerciseStage(
                uiState = uiState,
                onPickOption = onPickOption,
                onToggleToken = onToggleToken,
                onSubmitTokens = onSubmitTokens,
                onMatchLeft = onMatchLeft,
                onMatchRight = onMatchRight,
                onSubmitPairs = onSubmitPairs,
                onFillCloze = onFillCloze,
                onClearCloze = onClearCloze,
                onSubmitCloze = onSubmitCloze,
                onTypedText = onTypedText,
                onSpeakRequest = onSpeakRequest,
                onSkipSpeech = onSkipSpeech,
                onAdvance = onAdvance,
                onPlayAudio = onPlayAudio,
                onExit = onExit,
                modifier = modifier,
            )
    }
}

@Composable
private fun ExerciseStage(
    uiState: LessonUiState,
    onPickOption: (Int) -> Unit,
    onToggleToken: (String) -> Unit,
    onSubmitTokens: () -> Unit,
    onMatchLeft: (Int) -> Unit,
    onMatchRight: (Int) -> Unit,
    onSubmitPairs: () -> Unit,
    onFillCloze: (String, String) -> Unit,
    onClearCloze: (String) -> Unit,
    onSubmitCloze: () -> Unit,
    onTypedText: (String) -> Unit,
    onSpeakRequest: () -> Unit,
    onSkipSpeech: () -> Unit,
    onAdvance: () -> Unit,
    onPlayAudio: () -> Unit,
    onExit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val exercise = uiState.exercises.getOrNull(uiState.index) ?: return
    val progress =
        if (uiState.exercises.isEmpty()) {
            0f
        } else {
            (uiState.index + 1) / uiState.exercises.size.toFloat()
        }

    // Haptics semánticos (F2.1): confirmación/error al evaluar y tick al
    // acoplar tokens o parejas. Se disparan solo al crecer la respuesta.
    val haptics = rememberLinguaHaptics()
    LaunchedEffect(uiState.feedback, uiState.index) {
        val feedback = uiState.feedback ?: return@LaunchedEffect
        if (feedback.isCorrect) haptics.confirm() else haptics.error()
    }
    var lastChosenCount by remember { mutableIntStateOf(uiState.chosenTokens.size) }
    LaunchedEffect(uiState.chosenTokens.size) {
        if (uiState.chosenTokens.size > lastChosenCount) haptics.tick()
        lastChosenCount = uiState.chosenTokens.size
    }
    var lastMatchedCount by remember { mutableIntStateOf(uiState.matched.size) }
    LaunchedEffect(uiState.matched.size) {
        if (uiState.matched.size > lastMatchedCount) haptics.tick()
        lastMatchedCount = uiState.matched.size
    }

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
        ) {
            Text(
                text = uiState.lessonTitle,
                style = MaterialTheme.typography.titleMedium,
                color = colors.onBackground,
                modifier = Modifier.weight(1f),
            )
            TextButton(onClick = onExit) { Text(text = "Salir") }
        }
        LinguaProgressBar(progress = progress)
        if (uiState.voiceWarmingUp) {
            Spacer(Modifier.height(8.dp))
            InfoBanner(message = "Preparando motor de voz offline…")
        }
        Spacer(Modifier.height(16.dp))

        Column(
            modifier =
                Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            AnimatedContent(
                targetState = uiState.index,
                transitionSpec = {
                    val slide =
                        spring<IntOffset>(
                            dampingRatio = Spring.DampingRatioLowBouncy,
                            stiffness = Spring.StiffnessMediumLow,
                        )
                    val fade =
                        spring<Float>(
                            dampingRatio = Spring.DampingRatioLowBouncy,
                            stiffness = Spring.StiffnessMediumLow,
                        )
                    (
                        slideInHorizontally(animationSpec = slide) { width -> width / 6 } +
                            fadeIn(animationSpec = fade)
                    ).togetherWith(
                        slideOutHorizontally(animationSpec = slide) { width -> -width / 6 } +
                            fadeOut(animationSpec = fade),
                    )
                },
                label = "exercise_transition",
            ) { index ->
                val step = uiState.exercises.getOrNull(index) ?: return@AnimatedContent
                Renderer(
                    exercise = step,
                    uiState = uiState,
                    onPickOption = onPickOption,
                    onToggleToken = onToggleToken,
                    onMatchLeft = onMatchLeft,
                    onMatchRight = onMatchRight,
                    onFillCloze = onFillCloze,
                    onClearCloze = onClearCloze,
                    onTypedText = onTypedText,
                    onSpeakRequest = onSpeakRequest,
                    onPlayAudio = onPlayAudio,
                )
            }
        }

        Spacer(Modifier.height(12.dp))
        BottomActions(
            feedback = uiState.feedback,
            exercise = exercise,
            chosenCount = uiState.chosenTokens.size,
            listening = uiState.listening,
            pairsComplete =
                uiState.matched.size == (exercise as? Exercise.MatchPairs)?.pairs?.size &&
                    exercise.pairs.isNotEmpty(),
            clozeComplete =
                exercise is Exercise.ClozeDragDrop &&
                    exercise.blanks.isNotEmpty() &&
                    uiState.clozeFilled.size == exercise.blanks.size,
            onSubmitTokens = onSubmitTokens,
            onSubmitPairs = onSubmitPairs,
            onSubmitCloze = onSubmitCloze,
            onSkipSpeech = onSkipSpeech,
            onAdvance = onAdvance,
        )
    }
}

@Composable
private fun Renderer(
    exercise: Exercise,
    uiState: LessonUiState,
    onPickOption: (Int) -> Unit,
    onToggleToken: (String) -> Unit,
    onMatchLeft: (Int) -> Unit,
    onMatchRight: (Int) -> Unit,
    onFillCloze: (String, String) -> Unit,
    onClearCloze: (String) -> Unit,
    onTypedText: (String) -> Unit,
    onSpeakRequest: () -> Unit,
    onPlayAudio: () -> Unit,
) {
    when (exercise) {
        is Exercise.MultipleChoice -> OptionsStep(exercise.question, exercise.options, onPickOption)
        is Exercise.KanaChoose -> OptionsStep(exercise.kana, exercise.romajiOptions, onPickOption)
        is Exercise.KanjiReading -> OptionsStep(exercise.kanji, exercise.readingOptions, onPickOption)
        is Exercise.ParticleFill -> OptionsStep(exercise.sentenceTemplate, exercise.particles, onPickOption)
        is Exercise.ListenChoose -> {
            AudioPanel(title = "Escucha y elige", onPlay = onPlayAudio, enabled = uiState.audioAvailable)
            OptionsStep("¿Qué escuchaste?", exercise.options, onPickOption)
        }
        is Exercise.AudioOnlyPrompt -> {
            AudioPanel(title = "Escucha y elige", onPlay = onPlayAudio, enabled = uiState.audioAvailable)
            OptionsStep(exercise.question ?: "¿Qué escuchaste?", exercise.options, onPickOption)
        }
        is Exercise.ListenType ->
            TypePanel(
                audioTitle = "Escribe lo que escuchas",
                onPlay = onPlayAudio,
                audioEnabled = uiState.audioAvailable,
                onTypedText = onTypedText,
            )
        is Exercise.SpeakRepeat ->
            SpeechPanel(
                target = exercise.targetPhrase,
                listening = uiState.listening,
                recognized = uiState.recognized,
                audioAvailable = uiState.audioAvailable,
                onPlay = onPlayAudio,
                onRecord = onSpeakRequest,
            )
        is Exercise.Translate ->
            TokenBoard(header = exercise.sourcePhrase, uiState = uiState, onToggleToken = onToggleToken)
        is Exercise.OrderWords ->
            TokenBoard(header = "Ordena la frase", uiState = uiState, onToggleToken = onToggleToken)
        is Exercise.MatchPairs ->
            MatchBoard(exercise.pairs, uiState, onMatchLeft, onMatchRight)
        is Exercise.SemanticMatch ->
            PlaceholderStep(title = "Emparejamiento semántico")
        is Exercise.ClozeDragDrop ->
            ClozeDragDropBoard(
                segments = remember(exercise.id) { ClozePassageParser.parse(exercise.passage) },
                options = exercise.blanks.flatMap { blank -> blank.options }.distinct(),
                filled = uiState.clozeFilled,
                onAssign = onFillCloze,
                onClearBlank = onClearCloze,
            )
        is Exercise.FreeSpeakingPrompt ->
            PlaceholderStep(title = "Producción oral")
    }
}

/**
 * Marcador de las mecánicas v2 (F2). El contenido v2 aún no se publica; este
 * paso evita una pantalla vacía si un fixture llegara al motor de lección.
 */
@Composable
private fun PlaceholderStep(
    title: String,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            text = "Esta mecánica se habilitará en la próxima actualización.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun BottomActions(
    feedback: ExerciseFeedback?,
    exercise: Exercise,
    chosenCount: Int,
    listening: Boolean,
    pairsComplete: Boolean,
    clozeComplete: Boolean,
    onSubmitTokens: () -> Unit,
    onSubmitPairs: () -> Unit,
    onSubmitCloze: () -> Unit,
    onSkipSpeech: () -> Unit,
    onAdvance: () -> Unit,
) {
    when {
        feedback != null -> {
            FeedbackBanner(
                isCorrect = feedback.isCorrect,
                message = if (feedback.isCorrect) "¡Correcto!" else "Revisa la respuesta y sigue",
                diagnostic = feedback.diagnostic,
                idiom = feedback.idiom,
                culturalNote = feedback.culturalNote,
            )
            Spacer(Modifier.height(8.dp))
            PrimaryButton3D(
                text = "Continuar",
                onClick = onAdvance,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        exercise is Exercise.Translate || exercise is Exercise.OrderWords ->
            PrimaryButton3D(
                text = "Comprobar",
                onClick = onSubmitTokens,
                enabled = chosenCount > 0,
                modifier = Modifier.fillMaxWidth(),
            )
        exercise is Exercise.MatchPairs ->
            PrimaryButton3D(
                text = "Comprobar",
                onClick = onSubmitPairs,
                enabled = pairsComplete,
                modifier = Modifier.fillMaxWidth(),
            )
        exercise is Exercise.ClozeDragDrop ->
            PrimaryButton3D(
                text = "Comprobar",
                onClick = onSubmitCloze,
                enabled = clozeComplete,
                modifier = Modifier.fillMaxWidth(),
            )
        exercise is Exercise.SpeakRepeat ->
            TextButton(
                onClick = onSkipSpeech,
                modifier = Modifier.fillMaxWidth(),
                enabled = !listening,
            ) { Text(text = "No puedo hablar ahora") }
    }
}

/**
 * Repetición hablada (SpeakRepeat, Fase 2.3): escucha el modelo con TTS y
 * repite con el micrófono; muestra lo que el STT reconoció. Sin STT o sin
 * permiso la UI queda inactiva y "No puedo hablar ahora" deja avanzar (I3).
 */
@Composable
private fun SpeechPanel(
    target: String,
    listening: Boolean,
    recognized: String?,
    audioAvailable: Boolean,
    onPlay: () -> Unit,
    onRecord: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Escucha y repite",
            style = MaterialTheme.typography.titleLarge,
            color = colors.onBackground,
        )
        AudioPanel(
            title = target,
            onPlay = onPlay,
            enabled = audioAvailable,
        )
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(LinguaTokens.RADIUS_CARD_DP.dp))
                    .background(if (listening) colors.tertiaryContainer else colors.secondaryContainer)
                    .clickable(enabled = !listening, onClick = onRecord)
                    .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = if (listening) "Escuchando…" else "Sostén el botón para repetir",
                style = MaterialTheme.typography.titleMedium,
                color = colors.onBackground,
            )
            recognized?.let { text ->
                Text(
                    text = "Lo que reconoció el micrófono: \"$text\"",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onBackground,
                )
            }
        }
    }
}

/** Dictado libre (ListenType): su propio campo de texto y botón Comprobar. */
@Composable
private fun TypePanel(
    audioTitle: String,
    onPlay: () -> Unit,
    audioEnabled: Boolean,
    onTypedText: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var typed by remember(audioTitle) { mutableStateOf("") }
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        AudioPanel(title = audioTitle, onPlay = onPlay, enabled = audioEnabled)
        OutlinedTextField(
            value = typed,
            onValueChange = { value -> typed = value },
            label = { Text(text = "Tu dictado") },
            modifier = Modifier.fillMaxWidth(),
        )
        PrimaryButton3D(
            text = "Comprobar",
            onClick = { onTypedText(typed) },
            enabled = typed.isNotBlank(),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun OptionsStep(
    prompt: String,
    options: List<String>,
    onPickOption: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = prompt,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
        options.forEachIndexed { index, option ->
            OptionCard(
                text = option,
                selected = false,
                onClick = { onPickOption(index) },
            )
        }
    }
}

/**
 * Chip de audio (Fase 2): toca para reproducir; si no hay voz disponible
 * el chip queda inactivo en lugar de bloquear el ejercicio (degradación I1).
 */
@Composable
private fun AudioPanel(
    title: String,
    onPlay: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(LinguaTokens.RADIUS_CARD_DP.dp))
                    .background(
                        if (enabled) {
                            MaterialTheme.colorScheme.secondaryContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        },
                    ).clickable(enabled = enabled, onClick = onPlay)
                    .semantics { role = Role.Button }
                    .padding(16.dp),
        ) {
            Text(
                text =
                    if (enabled) {
                        "Reproducir audio (toca aquí)"
                    } else {
                        "Voz no disponible en este dispositivo"
                    },
                style = MaterialTheme.typography.titleMedium,
                color =
                    if (enabled) {
                        MaterialTheme.colorScheme.onSecondaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
            )
        }
    }
}

@Composable
private fun TokenBoard(
    header: String,
    uiState: LessonUiState,
    onToggleToken: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = header,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            text = "Tu respuesta",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(LinguaTokens.RADIUS_CARD_DP.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (uiState.chosenTokens.isEmpty()) {
                Text(
                    text = "Toca las palabras de abajo",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            uiState.chosenTokens.forEach { token ->
                TokenPill(token = token, selected = true, onToggle = onToggleToken)
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            uiState.availableTokens.forEach { token ->
                TokenPill(token = token, selected = false, onToggle = onToggleToken)
            }
        }
    }
}

@Composable
private fun TokenPill(
    token: String,
    selected: Boolean,
    onToggle: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    Text(
        text = token,
        style = MaterialTheme.typography.bodyMedium,
        color = if (selected) colors.onPrimary else colors.onSurface,
        modifier =
            modifier
                .clip(RoundedCornerShape(12.dp))
                .background(if (selected) colors.primary else colors.surface)
                .clickable { onToggle(token) }
                .semantics {
                    role = Role.Button
                    this.selected = selected
                }.padding(horizontal = 12.dp, vertical = 14.dp),
    )
}

@Composable
private fun MatchBoard(
    pairs: List<Exercise.MatchPair>,
    uiState: LessonUiState,
    onMatchLeft: (Int) -> Unit,
    onMatchRight: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Empareja las columnas",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
        pairs.forEachIndexed { left, pair ->
            val matchedRight = uiState.matched[left]
            MatchTile(
                label =
                    if (matchedRight != null) {
                        "${pair.left} → ${pairs[matchedRight].right}"
                    } else {
                        pair.left
                    },
                selected = uiState.selectedLeft == left,
                onClick = { onMatchLeft(left) },
                modifier = Modifier.fillMaxWidth(),
            )
        }
        Text(
            text = "Derechas",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            uiState.shuffledRights.forEach { right ->
                val taken = uiState.matched.containsValue(right)
                MatchTile(
                    label = pairs[right].right,
                    selected = false,
                    enabled = !taken,
                    onClick = { onMatchRight(right) },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun MatchTile(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val colors = MaterialTheme.colorScheme
    val background =
        when {
            !enabled -> colors.surfaceVariant.copy(alpha = 0.5f)
            selected -> colors.primaryContainer
            else -> colors.surface
        }
    Text(
        text = label,
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
        color = colors.onSurface,
        modifier =
            modifier
                .clip(RoundedCornerShape(LinguaTokens.RADIUS_CARD_DP.dp))
                .background(background)
                .clickable(enabled = enabled, onClick = onClick)
                .semantics {
                    role = Role.Button
                    this.selected = selected
                }.padding(horizontal = 10.dp, vertical = 14.dp),
    )
}

@Composable
private fun Loading(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = "Cargando la lección…", style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun Invalid(
    modifier: Modifier = Modifier,
    onExit: () -> Unit,
) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = "No pudimos abrir la lección", style = MaterialTheme.typography.titleMedium)
        Text(
            text = "Vuelve al camino e inténtalo de nuevo",
            style = MaterialTheme.typography.bodyMedium,
        )
        TextButton(onClick = onExit) { Text(text = "Salir") }
    }
}

@Composable
private fun Finish(
    uiState: LessonUiState,
    onExit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = "Lección completada", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(8.dp))
        Text(text = "XP +${uiState.xpEarned}", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(24.dp))
        PrimaryButton3D(
            text = "Volver al camino",
            onClick = onExit,
            modifier = Modifier.width(220.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LessonScreenPreview() {
    LinguaTheme {
        val previewState =
            LessonUiState(
                loading = false,
                lessonTitle = "Saludos",
                exercises =
                    listOf(
                        Exercise.MultipleChoice(
                            id = ExerciseId("ex-1"),
                            question = "Elige la traducción",
                            options = listOf("Hello", "Bye", "Good morning"),
                            correctIndex = 0,
                        ),
                    ),
                availableTokens = listOf("hola", "mundo"),
            )
        ExerciseStage(
            uiState = previewState,
            onPickOption = {},
            onToggleToken = {},
            onSubmitTokens = {},
            onMatchLeft = {},
            onMatchRight = {},
            onSubmitPairs = {},
            onFillCloze = { _, _ -> },
            onClearCloze = {},
            onSubmitCloze = {},
            onTypedText = {},
            onSpeakRequest = {},
            onSkipSpeech = {},
            onAdvance = {},
            onPlayAudio = {},
            onExit = {},
        )
    }
}

package dev.linguago.feature.lesson

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.linguago.core.domain.logic.ClozePassageParser
import dev.linguago.core.domain.model.Exercise
import dev.linguago.core.domain.model.ExerciseId
import dev.linguago.core.ui.components.AudioOnlyPromptPanel
import dev.linguago.core.ui.components.CancelledLessonSheet
import dev.linguago.core.ui.components.ClozeDragDropBoard
import dev.linguago.core.ui.components.FeedbackBanner
import dev.linguago.core.ui.components.FreeSpeakingPanel
import dev.linguago.core.ui.components.HeartsIndicator
import dev.linguago.core.ui.components.InfoBanner
import dev.linguago.core.ui.components.LiquidXpBar
import dev.linguago.core.ui.components.OptionCard
import dev.linguago.core.ui.components.OptionStatus
import dev.linguago.core.ui.components.PrimaryButton3D
import dev.linguago.core.ui.components.PuzzleMatchBoard
import dev.linguago.core.ui.components.PuzzlePairUi
import dev.linguago.core.ui.components.RewardSheet
import dev.linguago.core.ui.components.displayText
import dev.linguago.core.ui.feedback.FeedbackCue
import dev.linguago.core.ui.feedback.LocalInteractionFeedback
import dev.linguago.core.ui.feedback.feedbackClickable
import dev.linguago.core.ui.theme.LinguaMotion
import dev.linguago.core.ui.theme.LinguaTheme
import dev.linguago.core.ui.theme.LinguaTokens
import dev.linguago.core.ui.util.rememberAnimationsEnabled

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

    /** FreeSpeaking (F2.5): mismo permiso; al concederlo arranca la captura. */
    val freeSpeakLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) viewModel.startFreeSpeaking()
        }
    val onFreeSpeakStart: () -> Unit = {
        if (ContextCompat.checkSelfPermission(context, micPermission) == PackageManager.PERMISSION_GRANTED) {
            viewModel.startFreeSpeaking()
        } else {
            freeSpeakLauncher.launch(micPermission)
        }
    }

    LessonContent(
        uiState = uiState,
        onPickOption = viewModel::pickOption,
        onToggleToken = viewModel::toggleToken,
        onSubmitTokens = viewModel::submitTokens,
        onMatchLeft = viewModel::selectLeft,
        onMatchRight = viewModel::matchLeftTo,
        onUnpair = viewModel::unpair,
        onSubmitPairs = viewModel::submitMatchedPairs,
        onFillCloze = viewModel::fillClozeBlank,
        onClearCloze = viewModel::clearClozeBlank,
        onSubmitCloze = viewModel::submitCloze,
        onTypedTextChange = viewModel::updateTypedText,
        onSubmitTyped = viewModel::submitTypedText,
        onSpeakRequest = onSpeakRequest,
        onSkipSpeech = viewModel::skipSpeech,
        onFreeSpeakStart = onFreeSpeakStart,
        onFreeSpeakStop = viewModel::stopFreeSpeaking,
        onFreeSpeakRate = viewModel::rateFreeSpeaking,
        onSubmitFreeSpeaking = viewModel::submitFreeSpeaking,
        onFreeSpeakSkip = viewModel::skipFreeSpeaking,
        onPlayModelAnswer = viewModel::playModelAnswer,
        onRetry = viewModel::retry,
        onReveal = viewModel::revealAnswer,
        onAdvance = viewModel::advance,
        onPlayAudio = viewModel::playAudio,
        onPlaySlow = viewModel::playAudioSlow,
        onBeginLesson = viewModel::beginLesson,
        onOpenBrief = viewModel::openBrief,
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
    onUnpair: (Int) -> Unit,
    onSubmitPairs: () -> Unit,
    onFillCloze: (String, String) -> Unit,
    onClearCloze: (String) -> Unit,
    onSubmitCloze: () -> Unit,
    onTypedTextChange: (String) -> Unit,
    onSubmitTyped: () -> Unit,
    onSpeakRequest: () -> Unit,
    onSkipSpeech: () -> Unit,
    onFreeSpeakStart: () -> Unit,
    onFreeSpeakStop: () -> Unit,
    onFreeSpeakRate: (Int, Int) -> Unit,
    onSubmitFreeSpeaking: () -> Unit,
    onFreeSpeakSkip: () -> Unit,
    onPlayModelAnswer: () -> Unit,
    onRetry: () -> Unit,
    onReveal: () -> Unit,
    onAdvance: () -> Unit,
    onPlayAudio: () -> Unit,
    onPlaySlow: () -> Unit,
    onBeginLesson: () -> Unit,
    onOpenBrief: () -> Unit,
    onExit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val feedback = LocalInteractionFeedback.current
    LaunchedEffect(uiState.finished) {
        if (uiState.finished) feedback.play(FeedbackCue.COMPLETE)
    }
    when {
        uiState.loading -> Loading(modifier)
        uiState.invalid -> Invalid(modifier, onExit)
        uiState.lessonCancelled -> CancelledLessonSheet(xpLost = uiState.xpEarned, onExit = onExit, modifier = modifier)
        uiState.finished -> RewardSheet(xpEarned = uiState.xpEarned, onContinue = onExit, modifier = modifier)
        uiState.briefVisible && uiState.brief != null ->
            LessonBriefScreen(
                brief = uiState.brief,
                startLabel = if (uiState.briefSeen) "Volver a la lección" else "Empezar lección",
                onStart = onBeginLesson,
                onExit = onExit,
                modifier = modifier,
            )
        else ->
            ExerciseStage(
                uiState = uiState,
                onPickOption = onPickOption,
                onToggleToken = onToggleToken,
                onSubmitTokens = onSubmitTokens,
                onMatchLeft = onMatchLeft,
                onMatchRight = onMatchRight,
                onUnpair = onUnpair,
                onSubmitPairs = onSubmitPairs,
                onFillCloze = onFillCloze,
                onClearCloze = onClearCloze,
                onSubmitCloze = onSubmitCloze,
                onTypedTextChange = onTypedTextChange,
                onSubmitTyped = onSubmitTyped,
                onSpeakRequest = onSpeakRequest,
                onSkipSpeech = onSkipSpeech,
                onFreeSpeakStart = onFreeSpeakStart,
                onFreeSpeakStop = onFreeSpeakStop,
                onFreeSpeakRate = onFreeSpeakRate,
                onSubmitFreeSpeaking = onSubmitFreeSpeaking,
                onFreeSpeakSkip = onFreeSpeakSkip,
                onPlayModelAnswer = onPlayModelAnswer,
                onRetry = onRetry,
                onReveal = onReveal,
                onAdvance = onAdvance,
                onPlayAudio = onPlayAudio,
                onPlaySlow = onPlaySlow,
                onOpenBrief = onOpenBrief,
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
    onUnpair: (Int) -> Unit,
    onSubmitPairs: () -> Unit,
    onFillCloze: (String, String) -> Unit,
    onClearCloze: (String) -> Unit,
    onSubmitCloze: () -> Unit,
    onTypedTextChange: (String) -> Unit,
    onSubmitTyped: () -> Unit,
    onSpeakRequest: () -> Unit,
    onSkipSpeech: () -> Unit,
    onFreeSpeakStart: () -> Unit,
    onFreeSpeakStop: () -> Unit,
    onFreeSpeakRate: (Int, Int) -> Unit,
    onSubmitFreeSpeaking: () -> Unit,
    onFreeSpeakSkip: () -> Unit,
    onPlayModelAnswer: () -> Unit,
    onRetry: () -> Unit,
    onReveal: () -> Unit,
    onAdvance: () -> Unit,
    onPlayAudio: () -> Unit,
    onPlaySlow: () -> Unit,
    onOpenBrief: () -> Unit,
    onExit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val exercise = uiState.exercises.getOrNull(uiState.index) ?: return
    val totalPairs =
        when (exercise) {
            is Exercise.MatchPairs -> exercise.pairs.size
            is Exercise.SemanticMatch -> exercise.pairs.size
            else -> 0
        }
    val freeSpeakingComplete =
        exercise is Exercise.FreeSpeakingPrompt &&
            uiState.freeSpeaking.transcript != null &&
            uiState.freeSpeaking.selfAssessment.size >= exercise.selfAssessmentCriteria.size
    val progress =
        if (uiState.exercises.isEmpty()) {
            0f
        } else {
            (uiState.index + 1) / uiState.exercises.size.toFloat()
        }

    // Feedback sensorial (ADR-027): acierto/error al evaluar y encaje al
    // añadir o quitar tokens y parejas. Es un refuerzo, no la única señal.
    val feedback = LocalInteractionFeedback.current
    LaunchedEffect(uiState.feedback, uiState.index) {
        val current = uiState.feedback ?: return@LaunchedEffect
        feedback.play(if (current.isCorrect) FeedbackCue.CORRECT else FeedbackCue.INCORRECT)
    }
    var lastChosenCount by remember(uiState.index) { mutableIntStateOf(uiState.chosenTokens.size) }
    LaunchedEffect(uiState.chosenTokens.size, uiState.index) {
        when {
            uiState.chosenTokens.size > lastChosenCount -> feedback.play(FeedbackCue.SNAP)
            uiState.chosenTokens.size < lastChosenCount -> feedback.play(FeedbackCue.DESELECT)
        }
        lastChosenCount = uiState.chosenTokens.size
    }
    var lastMatchedCount by remember(uiState.index) { mutableIntStateOf(uiState.matched.size) }
    LaunchedEffect(uiState.matched.size, uiState.index) {
        when {
            uiState.matched.size > lastMatchedCount -> feedback.play(FeedbackCue.SNAP)
            uiState.matched.size < lastMatchedCount -> feedback.play(FeedbackCue.DESELECT)
        }
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
            TextButton(
                onClick = {
                    feedback.play(FeedbackCue.TAP)
                    onOpenBrief()
                },
            ) { Text(text = "Guía") }
            TextButton(
                onClick = {
                    feedback.play(FeedbackCue.TAP)
                    onExit()
                },
            ) { Text(text = "Salir") }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            HeartsIndicator(hearts = uiState.hearts)
            LiquidXpBar(progress = progress, modifier = Modifier.weight(1f))
        }
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
                    onUnpair = onUnpair,
                    onFillCloze = onFillCloze,
                    onClearCloze = onClearCloze,
                    onTypedTextChange = onTypedTextChange,
                    onSubmitTyped = onSubmitTyped,
                    onSpeakRequest = onSpeakRequest,
                    onPlayAudio = onPlayAudio,
                    onPlaySlow = onPlaySlow,
                    onFreeSpeakStart = onFreeSpeakStart,
                    onFreeSpeakStop = onFreeSpeakStop,
                    onFreeSpeakRate = onFreeSpeakRate,
                    onFreeSpeakSkip = onFreeSpeakSkip,
                    onPlayModelAnswer = onPlayModelAnswer,
                )
            }
        }

        Spacer(Modifier.height(12.dp))
        BottomActions(
            feedback = uiState.feedback,
            exercise = exercise,
            chosenCount = uiState.chosenTokens.size,
            listening = uiState.listening,
            pairsComplete = totalPairs > 0 && uiState.matched.size == totalPairs,
            clozeComplete =
                exercise is Exercise.ClozeDragDrop &&
                    exercise.blanks.isNotEmpty() &&
                    uiState.clozeFilled.size == exercise.blanks.size,
            freeSpeakingReady =
                exercise is Exercise.FreeSpeakingPrompt && uiState.freeSpeaking.transcript != null,
            freeSpeakingComplete = freeSpeakingComplete,
            onSubmitTokens = onSubmitTokens,
            onSubmitPairs = onSubmitPairs,
            onSubmitCloze = onSubmitCloze,
            onSubmitFreeSpeaking = onSubmitFreeSpeaking,
            onSkipSpeech = onSkipSpeech,
            onRetry = onRetry,
            onReveal = onReveal,
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
    onUnpair: (Int) -> Unit,
    onFillCloze: (String, String) -> Unit,
    onClearCloze: (String) -> Unit,
    onTypedTextChange: (String) -> Unit,
    onSubmitTyped: () -> Unit,
    onSpeakRequest: () -> Unit,
    onPlayAudio: () -> Unit,
    onPlaySlow: () -> Unit,
    onFreeSpeakStart: () -> Unit,
    onFreeSpeakStop: () -> Unit,
    onFreeSpeakRate: (Int, Int) -> Unit,
    onFreeSpeakSkip: () -> Unit,
    onPlayModelAnswer: () -> Unit,
) {
    when (exercise) {
        is Exercise.MultipleChoice ->
            OptionsStep(
                prompt = exercise.question,
                options = exercise.options,
                selectedIndex = uiState.selectedOption,
                correctIndex = exercise.correctIndex,
                answerLocked = uiState.answerLocked,
                revealAnswer = uiState.feedback?.correctAnswer != null,
                onPickOption = onPickOption,
            )
        is Exercise.KanaChoose ->
            OptionsStep(
                prompt = exercise.kana,
                options = exercise.romajiOptions,
                selectedIndex = uiState.selectedOption,
                correctIndex = exercise.romajiOptions.indexOf(exercise.correctRomaji),
                answerLocked = uiState.answerLocked,
                revealAnswer = uiState.feedback?.correctAnswer != null,
                onPickOption = onPickOption,
            )
        is Exercise.KanjiReading ->
            OptionsStep(
                prompt = exercise.kanji,
                options = exercise.readingOptions,
                selectedIndex = uiState.selectedOption,
                correctIndex = exercise.readingOptions.indexOf(exercise.correctReading),
                answerLocked = uiState.answerLocked,
                revealAnswer = uiState.feedback?.correctAnswer != null,
                onPickOption = onPickOption,
            )
        is Exercise.ParticleFill ->
            OptionsStep(
                prompt = exercise.sentenceTemplate,
                options = exercise.particles,
                selectedIndex = uiState.selectedOption,
                correctIndex = exercise.particles.indexOf(exercise.correctParticle),
                answerLocked = uiState.answerLocked,
                revealAnswer = uiState.feedback?.correctAnswer != null,
                onPickOption = onPickOption,
            )
        is Exercise.ListenChoose ->
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                AudioPanel(
                    title = "Escucha y elige",
                    onPlay = onPlayAudio,
                    onPlaySlow = onPlaySlow,
                    enabled = uiState.audioAvailable,
                )
                OptionsStep(
                    prompt = "¿Qué escuchaste?",
                    options = exercise.options,
                    selectedIndex = uiState.selectedOption,
                    correctIndex = exercise.correctIndex,
                    answerLocked = uiState.answerLocked,
                    revealAnswer = uiState.feedback?.correctAnswer != null,
                    onPickOption = onPickOption,
                )
            }
        is Exercise.AudioOnlyPrompt ->
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                AudioOnlyPromptPanel(
                    question = exercise.question,
                    audioAvailable = uiState.audioAvailable,
                    allowSlowPlayback = exercise.allowSlowPlayback || uiState.feedback != null,
                    transcript = exercise.audioText,
                    revealTranscript = uiState.feedback?.correctAnswer != null,
                    onPlay = onPlayAudio,
                    onPlaySlow = onPlaySlow,
                )
                OptionsStep(
                    prompt = "¿Qué escuchaste?",
                    options = exercise.options,
                    selectedIndex = uiState.selectedOption,
                    correctIndex = exercise.correctIndex,
                    answerLocked = uiState.answerLocked,
                    revealAnswer = uiState.feedback?.correctAnswer != null,
                    onPickOption = onPickOption,
                )
            }
        is Exercise.ListenType ->
            TypePanel(
                audioTitle = "Escribe lo que escuchas",
                value = uiState.typedText,
                onValueChange = onTypedTextChange,
                onSubmit = onSubmitTyped,
                onPlay = onPlayAudio,
                onPlaySlow = onPlaySlow,
                audioEnabled = uiState.audioAvailable,
                enabled = !uiState.answerLocked,
            )
        is Exercise.SpeakRepeat ->
            SpeechPanel(
                target = exercise.targetPhrase,
                listening = uiState.listening,
                recognized = uiState.recognized,
                audioAvailable = uiState.audioAvailable,
                answerLocked = uiState.answerLocked,
                onPlay = onPlayAudio,
                onPlaySlow = onPlaySlow,
                onRecord = onSpeakRequest,
            )
        is Exercise.Translate ->
            TokenBoard(
                header = exercise.sourcePhrase,
                uiState = uiState,
                answerLocked = uiState.answerLocked,
                onToggleToken = onToggleToken,
            )
        is Exercise.OrderWords ->
            OrderWordsBoard(
                uiState = uiState,
                answerLocked = uiState.answerLocked,
                onToggleToken = onToggleToken,
            )
        is Exercise.MatchPairs ->
            PuzzleMatchBoard(
                pairs = exercise.pairs.map { pair -> PuzzlePairUi(left = pair.left, right = pair.right) },
                shuffledRights = uiState.shuffledRights,
                selectedLeft = uiState.selectedLeft,
                matched = uiState.matched,
                onSelectLeft = onMatchLeft,
                onMatchRight = onMatchRight,
                onUnpair = onUnpair,
                instructions = "Empareja las columnas",
                enabled = !uiState.answerLocked,
                result = uiState.feedback?.isCorrect,
            )
        is Exercise.SemanticMatch ->
            PuzzleMatchBoard(
                pairs =
                    exercise.pairs.map { pair ->
                        PuzzlePairUi(
                            left = pair.left,
                            right = pair.right,
                            rightBadge = pair.register?.let(::semanticRegisterLabel),
                            rightDetail = pair.nuance,
                        )
                    },
                shuffledRights = uiState.shuffledRights,
                selectedLeft = uiState.selectedLeft,
                matched = uiState.matched,
                onSelectLeft = onMatchLeft,
                onMatchRight = onMatchRight,
                onUnpair = onUnpair,
                instructions = exercise.instructions ?: "Empareja cada expresión con su equivalente",
                enabled = !uiState.answerLocked,
                result = uiState.feedback?.isCorrect,
            )
        is Exercise.ClozeDragDrop ->
            ClozeDragDropBoard(
                segments = remember(exercise.id) { ClozePassageParser.parse(exercise.passage) },
                options = exercise.blanks.flatMap { blank -> blank.options }.distinct(),
                filled = uiState.clozeFilled,
                onAssign = onFillCloze,
                onClearBlank = onClearCloze,
                enabled = !uiState.answerLocked,
            )
        is Exercise.FreeSpeakingPrompt ->
            FreeSpeakingPanel(
                prompt = exercise.prompt,
                speechAvailable = uiState.speechAvailable,
                recording = uiState.freeSpeaking.recording,
                waveform = uiState.freeSpeaking.waveform,
                liveKeywords = uiState.freeSpeaking.liveKeywords,
                partialTranscript = uiState.freeSpeaking.partialTranscript,
                transcript = uiState.freeSpeaking.transcript,
                durationMillis = uiState.freeSpeaking.durationMillis,
                feedback = uiState.freeSpeaking.breakdown,
                detectedGrammarLabels = uiState.freeSpeaking.grammarLabels,
                modelAnswer = exercise.modelAnswer,
                criteria = exercise.selfAssessmentCriteria,
                selfAssessment = uiState.freeSpeaking.selfAssessment,
                audioAvailable = uiState.audioAvailable,
                enabled = !uiState.answerLocked,
                onStart = onFreeSpeakStart,
                onStop = onFreeSpeakStop,
                onRate = onFreeSpeakRate,
                onPlayModel = onPlayModelAnswer,
                onSkip = onFreeSpeakSkip,
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
    freeSpeakingReady: Boolean,
    freeSpeakingComplete: Boolean,
    onSubmitTokens: () -> Unit,
    onSubmitPairs: () -> Unit,
    onSubmitCloze: () -> Unit,
    onSubmitFreeSpeaking: () -> Unit,
    onSkipSpeech: () -> Unit,
    onRetry: () -> Unit,
    onReveal: () -> Unit,
    onAdvance: () -> Unit,
) {
    val feedbackPlayer = LocalInteractionFeedback.current
    when {
        feedback != null -> {
            FeedbackBanner(
                isCorrect = feedback.isCorrect,
                message = if (feedback.isCorrect) "¡Correcto!" else "Revisa la respuesta y sigue",
                diagnostic = feedback.diagnostic,
                idiom = feedback.idiom,
                culturalNote = feedback.culturalNote,
                successExplanation = feedback.successExplanation,
                correctAnswer = feedback.correctAnswer?.displayText(),
            )
            if (feedback.retryAvailable) {
                Spacer(Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    TextButton(
                        onClick = {
                            feedbackPlayer.play(FeedbackCue.TAP)
                            onRetry()
                        },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(text = "Reintentar")
                    }
                    if (feedback.correctAnswer == null) {
                        TextButton(
                            onClick = {
                                feedbackPlayer.play(FeedbackCue.TAP)
                                onReveal()
                            },
                            modifier = Modifier.weight(1f),
                        ) {
                            Text(text = "Ver respuesta")
                        }
                    }
                }
            }
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
        exercise is Exercise.MatchPairs || exercise is Exercise.SemanticMatch ->
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
        exercise is Exercise.FreeSpeakingPrompt && freeSpeakingReady ->
            PrimaryButton3D(
                text = "Comprobar",
                onClick = onSubmitFreeSpeaking,
                enabled = freeSpeakingComplete,
                modifier = Modifier.fillMaxWidth(),
            )
        exercise is Exercise.SpeakRepeat ->
            TextButton(
                onClick = {
                    feedbackPlayer.play(FeedbackCue.TAP)
                    onSkipSpeech()
                },
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
    answerLocked: Boolean,
    onPlay: () -> Unit,
    onPlaySlow: () -> Unit,
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
            onPlaySlow = onPlaySlow,
            enabled = audioAvailable,
        )
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(LinguaTokens.RADIUS_CARD_DP.dp))
                    .background(if (listening) colors.tertiaryContainer else colors.secondaryContainer)
                    .feedbackClickable(
                        cue = FeedbackCue.TAP,
                        enabled = !listening && !answerLocked,
                        onClick = onRecord,
                    ).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text =
                    when {
                        listening -> "Escuchando…"
                        answerLocked -> "Respuesta enviada"
                        else -> "Sostén el botón para repetir"
                    },
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
    value: String,
    onValueChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onPlay: () -> Unit,
    onPlaySlow: () -> Unit,
    audioEnabled: Boolean,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        AudioPanel(
            title = audioTitle,
            onPlay = onPlay,
            onPlaySlow = onPlaySlow,
            enabled = audioEnabled,
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(text = "Tu dictado") },
            enabled = enabled,
            modifier = Modifier.fillMaxWidth(),
        )
        PrimaryButton3D(
            text = "Comprobar",
            onClick = onSubmit,
            enabled = enabled && value.isNotBlank(),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun OptionsStep(
    prompt: String,
    options: List<String>,
    selectedIndex: Int?,
    correctIndex: Int,
    answerLocked: Boolean,
    revealAnswer: Boolean,
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
            val status =
                when {
                    !revealAnswer -> OptionStatus.NEUTRAL
                    index == correctIndex -> OptionStatus.CORRECT
                    index == selectedIndex -> OptionStatus.INCORRECT
                    else -> OptionStatus.NEUTRAL
                }
            OptionCard(
                text = option,
                selected = selectedIndex == index,
                enabled = !answerLocked,
                status = status,
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
    onPlaySlow: (() -> Unit)? = null,
) {
    val feedback = LocalInteractionFeedback.current
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
                    ).feedbackClickable(FeedbackCue.TAP, enabled = enabled, onClick = onPlay)
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
        if (onPlaySlow != null && enabled) {
            TextButton(
                onClick = {
                    feedback.play(FeedbackCue.TAP)
                    onPlaySlow()
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(text = "Reproducir más despacio (0.75x)")
            }
        }
    }
}

private fun semanticRegisterLabel(register: Exercise.SemanticRegister): String =
    when (register) {
        Exercise.SemanticRegister.NEUTRAL -> "Neutro"
        Exercise.SemanticRegister.FORMAL -> "Formal"
        Exercise.SemanticRegister.INFORMAL -> "Informal"
        Exercise.SemanticRegister.LITERARY -> "Literario"
        Exercise.SemanticRegister.COLLOQUIAL -> "Coloquial"
    }

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TokenBoard(
    header: String,
    uiState: LessonUiState,
    answerLocked: Boolean,
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
                TokenPill(
                    token = token,
                    selected = true,
                    enabled = !answerLocked,
                    onToggle = onToggleToken,
                )
            }
        }
        Text(
            text = "Palabras disponibles",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            uiState.availableTokens.forEach { token ->
                TokenPill(
                    token = token,
                    selected = false,
                    enabled = !answerLocked,
                    onToggle = onToggleToken,
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun OrderWordsBoard(
    uiState: LessonUiState,
    answerLocked: Boolean,
    onToggleToken: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()
    LaunchedEffect(uiState.chosenTokens.size) {
        if (uiState.chosenTokens.isNotEmpty()) {
            listState.animateScrollToItem(uiState.chosenTokens.lastIndex)
        }
    }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Ordena la frase",
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
                    .heightIn(min = 56.dp)
                    .clip(RoundedCornerShape(LinguaTokens.RADIUS_CARD_DP.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(vertical = 8.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            if (uiState.chosenTokens.isEmpty()) {
                Text(
                    text = "Toca las palabras de abajo",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 12.dp),
                )
            } else {
                LazyRow(
                    state = listState,
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    itemsIndexed(
                        items = uiState.chosenTokens,
                        key = { index, _ -> "chosen-$index" },
                    ) { _, token ->
                        TokenPill(
                            token = token,
                            selected = true,
                            enabled = !answerLocked,
                            onToggle = onToggleToken,
                        )
                    }
                }
            }
        }
        Text(
            text = "Palabras disponibles",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            uiState.availableTokens.forEach { token ->
                TokenPill(
                    token = token,
                    selected = false,
                    enabled = !answerLocked,
                    onToggle = onToggleToken,
                )
            }
        }
    }
}

@Composable
private fun TokenPill(
    token: String,
    selected: Boolean,
    enabled: Boolean,
    onToggle: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val animations = rememberAnimationsEnabled()
    val background by animateColorAsState(
        targetValue = if (selected) colors.primary else colors.surface,
        animationSpec = if (animations) tween(durationMillis = 160) else snap(),
        label = "tokenBackground",
    )
    val contentColor by animateColorAsState(
        targetValue = if (selected) colors.onPrimary else colors.onSurface,
        animationSpec = if (animations) tween(durationMillis = 160) else snap(),
        label = "tokenContent",
    )
    val scale by animateFloatAsState(
        targetValue = if (selected && animations) 1.03f else 1f,
        animationSpec = LinguaMotion.SpringSnappy,
        label = "tokenScale",
    )

    Text(
        text = token,
        style = MaterialTheme.typography.bodyMedium,
        color = contentColor,
        modifier =
            modifier
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }.clip(RoundedCornerShape(12.dp))
                .background(background)
                .clickable(enabled = enabled) { onToggle(token) }
                .semantics {
                    role = Role.Button
                    this.selected = selected
                }.padding(horizontal = 12.dp, vertical = 14.dp),
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
            onUnpair = {},
            onSubmitPairs = {},
            onFillCloze = { _, _ -> },
            onClearCloze = {},
            onSubmitCloze = {},
            onTypedTextChange = {},
            onSubmitTyped = {},
            onSpeakRequest = {},
            onSkipSpeech = {},
            onFreeSpeakStart = {},
            onFreeSpeakStop = {},
            onFreeSpeakRate = { _, _ -> },
            onSubmitFreeSpeaking = {},
            onFreeSpeakSkip = {},
            onPlayModelAnswer = {},
            onRetry = {},
            onReveal = {},
            onAdvance = {},
            onPlayAudio = {},
            onPlaySlow = {},
            onOpenBrief = {},
            onExit = {},
        )
    }
}

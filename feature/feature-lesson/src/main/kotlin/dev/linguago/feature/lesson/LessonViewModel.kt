package dev.linguago.feature.lesson

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.linguago.core.audio.SttEngine
import dev.linguago.core.audio.TtsEngine
import dev.linguago.core.audio.isWarmingUp
import dev.linguago.core.common.Result
import dev.linguago.core.domain.hearts.HEARTS_MAX
import dev.linguago.core.domain.logic.AnswerKey
import dev.linguago.core.domain.logic.CulturalNoteMatcher
import dev.linguago.core.domain.logic.FreeSpeakingFeedback
import dev.linguago.core.domain.logic.FreeSpeakingHeuristicGrader
import dev.linguago.core.domain.logic.GradingResult
import dev.linguago.core.domain.logic.LessonGlossary
import dev.linguago.core.domain.logic.RetryPolicy
import dev.linguago.core.domain.logic.SpokenFreeResponse
import dev.linguago.core.domain.logic.answerKey
import dev.linguago.core.domain.model.CourseId
import dev.linguago.core.domain.model.Exercise
import dev.linguago.core.domain.model.GrammarPoint
import dev.linguago.core.domain.model.Language
import dev.linguago.core.domain.model.LessonId
import dev.linguago.core.domain.model.LessonSession
import dev.linguago.core.domain.model.XP_PER_CORRECT_ANSWER
import dev.linguago.core.domain.repository.ProgressRepository
import dev.linguago.core.domain.repository.ReviewRepository
import dev.linguago.core.domain.usecase.CompleteLessonUseCase
import dev.linguago.core.domain.usecase.ExerciseAnswer
import dev.linguago.core.domain.usecase.ObserveHeartsUseCase
import dev.linguago.core.domain.usecase.RegisterAnswerUseCase
import dev.linguago.core.domain.usecase.StartLessonUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Feedback de la respuesta evaluada, visible hasta que el usuario avanza. */
data class ExerciseFeedback(
    val isCorrect: Boolean,
    /** Explicación concreta del `feedbackOnError` (ADR-018); `null` en v1. */
    val diagnostic: String? = null,
    /** Modismo de la lección presente en el ejercicio (F2.2), si aplica. */
    val idiom: String? = null,
    val culturalNote: String? = null,
    /** Explicación del acierto, propia o de respaldo (ADR-028). */
    val successExplanation: String? = null,
    /** Respuesta canónica estructurada para revelarla tras calificar. */
    val correctAnswer: AnswerKey? = null,
    /** `true` si el ejercicio admite un reintento pedagógico (ADR-030). */
    val retryAvailable: Boolean = false,
    /**
     * `true` cuando el usuario omitió un ejercicio de voz (B-02): el avance es
     * neutro, sin penalización visual, de combo ni de nota.
     */
    val skipped: Boolean = false,
)

/**
 * Grabación y revisión de una respuesta oral libre (F2.5): energía RMS en
 * vivo, palabras clave detectadas, transcripción final, desglose heurístico
 * y autoevaluación por criterio (0 = aún no, 1 = casi, 2 = sí).
 */
data class FreeSpeakingState(
    val recording: Boolean = false,
    val waveform: List<Float> = emptyList(),
    val liveKeywords: List<String> = emptyList(),
    val partialTranscript: String = "",
    val transcript: String? = null,
    val durationMillis: Long = 0L,
    val breakdown: FreeSpeakingFeedback? = null,
    val grammarLabels: List<String> = emptyList(),
    val selfAssessment: Map<Int, Int> = emptyMap(),
)

/**
 * Ficha del tablero de tokens con identidad estable aunque su texto se repita
 * en la frase (B-06, D-03). El índice es único durante el ejercicio y evita
 * que tocar una ocurrencia seleccione o deseleccione todas las iguales.
 */
data class IndexedToken(
    val id: Int,
    val text: String,
)

/**
 * Estado del motor de lección (ADR-002: FSM). `exercises` nunca cambia
 * durante la sesión; lo mutable es el índice actual, los paneles de
 * respuesta (tokens/parejas) y el feedback. Las vidas son una penalización
 * no bloqueante (ADR-025): a 0 la lección se cancela y no se guarda su XP.
 */
data class LessonUiState(
    val loading: Boolean = true,
    val invalid: Boolean = false,
    val lessonTitle: String = "",
    /** Micro-clase de gramática/reglas construida al abrir la lección (ADR-028). */
    val brief: LessonBriefUi? = null,
    /** `true` mientras se muestra la micro-clase antes de los ejercicios. */
    val briefVisible: Boolean = false,
    /** `true` cuando el alumno ya cerró la micro-clase al menos una vez. */
    val briefSeen: Boolean = false,
    val exercises: List<Exercise> = emptyList(),
    val index: Int = 0,
    val xpEarned: Int = 0,
    /** Aciertos consecutivos de la sesión (ADR-033); no se persiste. */
    val combo: Int = 0,
    /** `true` mientras el TTS suspendido está reproduciendo (ADR-033). */
    val speaking: Boolean = false,
    val feedback: ExerciseFeedback? = null,
    /** Opción elegida por el usuario para pintar el estado correcto/incorrecto. */
    val selectedOption: Int? = null,
    /** Texto del dictado, propiedad del ViewModel para poder reintentar. */
    val typedText: String = "",
    /** Bloquea los controles de respuesta mientras se muestra el feedback. */
    val answerLocked: Boolean = false,
    /** Evita dobles envíos mientras el grader está en curso. */
    val submitting: Boolean = false,
    /** Reintentos usados en el ejercicio actual (máximo uno, ADR-030). */
    val retryCount: Int = 0,
    /** La vida del ejercicio ya se consumió; los reintentos no cuestan otra. */
    val heartConsumedForExercise: Boolean = false,
    /** El XP del ejercicio ya se otorgó; no se duplica al reintentar. */
    val xpAwardedForExercise: Boolean = false,
    val finished: Boolean = false,
    /** Vidas vigentes; se actualizan al responder (ADR-025). */
    val hearts: Int = HEARTS_MAX,
    /** `true` cuando el jugador se quedó sin vidas y la lección no se guarda. */
    val lessonCancelled: Boolean = false,
    // Tablero de tokens (Translate / OrderWords)
    val availableTokens: List<IndexedToken> = emptyList(),
    val chosenTokens: List<IndexedToken> = emptyList(),
    // Parejas (MatchPairs): orden de dibujo de las derechas + estado de asocio
    val shuffledRights: List<Int> = emptyList(),
    val selectedLeft: Int? = null,
    val selectedRight: Int? = null,
    val matched: Map<Int, Int> = emptyMap(),
    // Glosario de apoyo (ADR-036): palabra en idioma meta → traducción de origen.
    val glossary: Map<String, List<String>> = emptyMap(),
    // Resultado de la sesión (ADR-036): primer intento por ejercicio.
    val totalExercises: Int = 0,
    val correctCount: Int = 0,
    val incorrectCount: Int = 0,
    /** Ejercicios de voz omitidos: no cuentan para la nota (B-02). */
    val skippedCount: Int = 0,
    val score: LessonScore = LessonScore(),
    val mistakes: List<Exercise> = emptyList(),
    val showMistakeReview: Boolean = false,
    val correctingMistakes: Boolean = false,
    // Cloze (ClozeDragDrop, F2.3): hueco id → opción colocada.
    val clozeFilled: Map<String, String> = emptyMap(),
    // Producción oral libre (FreeSpeakingPrompt, F2.5).
    val freeSpeaking: FreeSpeakingState = FreeSpeakingState(),
    // Audio (Fase 2): disponibilidad de voz en el dispositivo ejercicios listen.
    val audioAvailable: Boolean = false,
    // Voz del usuario (SpeakRepeat, Fase 2.3).
    val speechAvailable: Boolean = false,
    // Warmup de motores (F2.1): la UI avisa sin bloquear (I3).
    val voiceWarmingUp: Boolean = false,
    val listening: Boolean = false,
    val recognized: String? = null,
)

/**
 * Motor de aprendizaje (RFC §3.2). Cada respuesta pasa por el
 * SubmitAnswerUseCase (grader puro, sin tocar XP) y el XP de la sesión se
 * acumula en memoria; al final la lección se persiste con
 * CompleteLessonUseCase (racha y XP total), único punto de escritura (ADR-025).
 */
@HiltViewModel
class LessonViewModel
    @Inject
    constructor(
        private val savedStateHandle: SavedStateHandle,
        private val startLessonUseCase: StartLessonUseCase,
        private val registerAnswerUseCase: RegisterAnswerUseCase,
        private val completeLessonUseCase: CompleteLessonUseCase,
        private val observeHeartsUseCase: ObserveHeartsUseCase,
        private val ttsEngine: TtsEngine,
        private val sttEngine: SttEngine,
        private val reviewRepository: ReviewRepository,
        private val progressRepository: ProgressRepository,
    ) : ViewModel() {
        private val courseId: String =
            checkNotNull(savedStateHandle[LESSON_COURSE_KEY])
        private val lessonId: String =
            checkNotNull(savedStateHandle[LESSON_ID_KEY])

        private val _uiState = MutableStateFlow(LessonUiState())
        val uiState: StateFlow<LessonUiState> = _uiState.asStateFlow()

        private var targetLanguage: Language? = null
        private var session: LessonSession? = null
        private var freeSpeakingJob: Job? = null
        private var speechJob: Job? = null
        private var freeSpeakingStartedAt = 0L
        private val gradedExercises = mutableSetOf<String>()
        private var finishing = false

        init {
            viewModelScope.launch {
                observeHeartsUseCase().collect { hearts ->
                    _uiState.update { state -> state.copy(hearts = hearts.hearts) }
                }
            }
            viewModelScope.launch {
                when (val result = startLessonUseCase(CourseId(courseId), LessonId(lessonId))) {
                    is Result.Success -> startLesson(result.value)
                    is Result.Error -> _uiState.update { it.copy(loading = false, invalid = true) }
                }
            }
        }

        /** El usuario elige una opción de lista (MC / ListenChoose / Kana / Kanji / Partícula). */
        fun pickOption(index: Int) {
            if (_uiState.value.answerLocked) return
            _uiState.update { it.copy(selectedOption = index) }
        }

        /** Valida la opción elegida solo cuando el usuario pulsa “Siguiente” (ADR-036). */
        fun submitSelection() {
            val state = _uiState.value
            if (state.answerLocked || state.feedback != null) return
            val selected = state.selectedOption ?: return
            submitAnswer(ExerciseAnswer.OptionPicked(selected))
        }

        /** Actualiza el dictado sin perderlo al recomponer o reintentar. */
        fun updateTypedText(text: String) {
            if (_uiState.value.answerLocked) return
            _uiState.update { it.copy(typedText = text) }
        }

        /** Reproduce el audio del ejercicio actual (escuchar/escribir lo escuchado). */
        fun playAudio() = speakCurrent(NORMAL_PLAYBACK_SPEED)

        /** Reproduce el audio a 0.75x para comprensión con apoyo (F2.4). */
        fun playAudioSlow() = speakCurrent(SLOW_PLAYBACK_SPEED)

        private fun speakCurrent(speed: Float) {
            val language = targetLanguage ?: return
            val audioText =
                _uiState.value.exercises
                    .getOrNull(_uiState.value.index)
                    ?.audioText ?: return
            viewModelScope.launch {
                _uiState.update { it.copy(speaking = true) }
                try {
                    ttsEngine.speak(audioText, language, speed)
                } finally {
                    _uiState.update { it.copy(speaking = false) }
                }
            }
        }

        /**
         * Inicia la captura libre (F2.5): energía RMS y parciales de Vosk
         * actualizan el visualizador y las ideas clave en vivo (ADR-019/020).
         */
        fun startFreeSpeaking() {
            val language = targetLanguage ?: return
            val exercise = currentExercise() as? Exercise.FreeSpeakingPrompt ?: return
            val state = _uiState.value
            val busy = state.freeSpeaking.recording || state.feedback != null
            if (busy || !state.speechAvailable || state.answerLocked) return
            freeSpeakingStartedAt = System.currentTimeMillis()
            _uiState.update { it.copy(freeSpeaking = FreeSpeakingState(recording = true)) }
            freeSpeakingJob =
                viewModelScope.launch {
                    var partialTranscript = ""
                    var finalTranscript = ""
                    try {
                        sttEngine.observeTranscription(language).collect { update ->
                            if (update.partialText.isNotBlank()) partialTranscript = update.partialText
                            update.finalText?.let { text -> finalTranscript = text }
                            val keywords =
                                FreeSpeakingHeuristicGrader.coveredClusterTerms(exercise, partialTranscript)
                            _uiState.update { current ->
                                current.copy(
                                    freeSpeaking =
                                        current.freeSpeaking.copy(
                                            waveform =
                                                (current.freeSpeaking.waveform + amplitudeOf(update.rmsDb))
                                                    .takeLast(WAVE_BARS),
                                            liveKeywords = keywords,
                                            partialTranscript = partialTranscript,
                                        ),
                                )
                            }
                        }
                    } catch (cancellation: CancellationException) {
                        throw cancellation
                    } catch (_: Exception) {
                        finalTranscript = partialTranscript
                    }
                    freeSpeakingJob = null
                    finishFreeSpeaking(
                        transcript = finalTranscript,
                        durationMillis = System.currentTimeMillis() - freeSpeakingStartedAt,
                    )
                }
        }

        /** Detiene la grabación y pasa al paso de revisión con el parcial actual. */
        fun stopFreeSpeaking() {
            val state = _uiState.value
            if (!state.freeSpeaking.recording) return
            freeSpeakingJob?.cancel()
            freeSpeakingJob = null
            finishFreeSpeaking(
                transcript = state.freeSpeaking.partialTranscript,
                durationMillis = System.currentTimeMillis() - freeSpeakingStartedAt,
            )
        }

        /** Autoevaluación de un criterio: 0 = aún no, 1 = casi, 2 = sí. */
        fun rateFreeSpeaking(
            criterion: Int,
            level: Int,
        ) {
            val exercise = currentExercise() as? Exercise.FreeSpeakingPrompt ?: return
            if (criterion !in exercise.selfAssessmentCriteria.indices || level !in 0..MAX_SELF_ASSESSMENT_LEVEL) return
            if (_uiState.value.feedback != null || _uiState.value.answerLocked) return
            _uiState.update { state ->
                state.copy(
                    freeSpeaking =
                        state.freeSpeaking.copy(
                            selfAssessment = state.freeSpeaking.selfAssessment + (criterion to level),
                        ),
                )
            }
        }

        /** Evalúa la respuesta oral (autoevaluación incluida) y la envía (F2.5). */
        fun submitFreeSpeaking() {
            val exercise = currentExercise() as? Exercise.FreeSpeakingPrompt ?: return
            val state = _uiState.value
            if (state.feedback != null || state.answerLocked) return
            val transcript = state.freeSpeaking.transcript ?: return
            val criteria = exercise.selfAssessmentCriteria.size
            if (state.freeSpeaking.selfAssessment.size < criteria) return
            val score =
                if (criteria == 0) {
                    null
                } else {
                    state.freeSpeaking.selfAssessment.values
                        .map { level -> level / MAX_SELF_ASSESSMENT_LEVEL.toDouble() }
                        .average()
                }
            val response =
                SpokenFreeResponse(
                    transcript = transcript,
                    durationMillis = state.freeSpeaking.durationMillis,
                    selfAssessmentScore = score,
                )
            publishFreeSpeakingBreakdown(exercise, response)
            submitAnswer(
                ExerciseAnswer.FreeSpoken(
                    transcript = transcript,
                    durationMillis = state.freeSpeaking.durationMillis,
                    selfAssessmentScore = score,
                ),
                session?.lesson?.grammarPoints.orEmpty(),
            )
        }

        /** Sin micrófono o sin permiso: avanza sin penalización (B-02, I3). */
        fun skipFreeSpeaking() {
            if (_uiState.value.feedback != null || _uiState.value.answerLocked) return
            if (currentExercise() !is Exercise.FreeSpeakingPrompt) return
            submitAnswer(
                ExerciseAnswer.FreeSpoken(transcript = "", durationMillis = 0L),
                session?.lesson?.grammarPoints.orEmpty(),
                consumeHeart = false,
                allowRetry = false,
                skipped = true,
            )
        }

        /** Reproduce la respuesta modelo del ejercicio oral (TTS, F2.5). */
        fun playModelAnswer() {
            val language = targetLanguage ?: return
            val exercise = currentExercise() as? Exercise.FreeSpeakingPrompt ?: return
            viewModelScope.launch { ttsEngine.speak(exercise.modelAnswer, language) }
        }

        private fun finishFreeSpeaking(
            transcript: String,
            durationMillis: Long,
        ) {
            val exercise = currentExercise() as? Exercise.FreeSpeakingPrompt ?: return
            val response =
                SpokenFreeResponse(
                    transcript = transcript,
                    durationMillis = durationMillis,
                )
            val breakdown = evaluateFreeSpeaking(exercise, response)
            _uiState.update {
                it.copy(
                    freeSpeaking =
                        it.freeSpeaking.copy(
                            recording = false,
                            transcript = transcript,
                            durationMillis = durationMillis,
                            breakdown = breakdown,
                            grammarLabels = grammarLabels(breakdown),
                        ),
                )
            }
        }

        private fun publishFreeSpeakingBreakdown(
            exercise: Exercise.FreeSpeakingPrompt,
            response: SpokenFreeResponse,
        ) {
            val breakdown = evaluateFreeSpeaking(exercise, response)
            _uiState.update {
                it.copy(
                    freeSpeaking =
                        it.freeSpeaking.copy(
                            breakdown = breakdown,
                            grammarLabels = grammarLabels(breakdown),
                        ),
                )
            }
        }

        private fun evaluateFreeSpeaking(
            exercise: Exercise.FreeSpeakingPrompt,
            response: SpokenFreeResponse,
        ): FreeSpeakingFeedback =
            FreeSpeakingHeuristicGrader.evaluate(
                exercise = exercise,
                grammarPoints = session?.lesson?.grammarPoints.orEmpty(),
                response = response,
                language = session?.course?.toLanguage ?: Language.ENGLISH,
            )

        private fun grammarLabels(feedback: FreeSpeakingFeedback): List<String> {
            val points = session?.lesson?.grammarPoints.orEmpty()
            return feedback.detectedGrammarRefs.mapNotNull { ref ->
                points.firstOrNull { point -> point.id == ref }?.label
            }
        }

        private fun currentExercise(): Exercise? = _uiState.value.exercises.getOrNull(_uiState.value.index)

        private fun amplitudeOf(rmsDb: Float): Float = ((rmsDb - SILENCE_DB) / -SILENCE_DB).coerceIn(0f, 1f)

        /** Escucha (una vez) y evalúa la repetición (SpeakRepeat). */
        fun transcribeSpoken() {
            val language = targetLanguage ?: return
            val state = _uiState.value
            val busy = state.listening || state.feedback != null
            if (busy || !state.speechAvailable || state.answerLocked) return
            _uiState.update { it.copy(listening = true, recognized = null) }
            speechJob =
                viewModelScope.launch {
                    val result = sttEngine.transcribe(language)
                    _uiState.update { it.copy(listening = false, recognized = result.text.ifBlank { null }) }
                    submitAnswer(ExerciseAnswer.SpokenText(result.text))
                }
        }

        /** Detiene la escucha de SpeakRepeat y permite grabar de nuevo. */
        fun stopSpoken() {
            speechJob?.cancel()
            speechJob = null
            _uiState.update { it.copy(listening = false) }
        }

        /** Sin STT/permiso: avanza sin penalización (B-02, I3). */
        fun skipSpeech() {
            if (_uiState.value.listening || _uiState.value.feedback != null || _uiState.value.answerLocked) return
            submitAnswer(
                ExerciseAnswer.SpokenText(""),
                consumeHeart = false,
                allowRetry = false,
                skipped = true,
            )
        }

        /** El usuario verifica el tablero de tokens (Translate / OrderWords). */
        fun submitTokens() {
            val tokens = _uiState.value.chosenTokens.map { token -> token.text }
            if (tokens.isNotEmpty()) submitAnswer(ExerciseAnswer.TokenSequence(tokens))
        }

        /** El usuario envía el dictado libre (ListenType). */
        fun submitTypedText() {
            val text = _uiState.value.typedText
            if (text.isNotBlank()) submitAnswer(ExerciseAnswer.TypedText(text))
        }

        /** El usuario verifica todas las parejas establecidas (MatchPairs / SemanticMatch). */
        fun submitMatchedPairs() {
            val current = _uiState.value.exercises.getOrNull(_uiState.value.index)
            val total =
                when (current) {
                    is Exercise.MatchPairs -> current.pairs.size
                    is Exercise.SemanticMatch -> current.pairs.size
                    else -> 0
                }
            if (total > 0 && _uiState.value.matched.size == total) {
                submitAnswer(ExerciseAnswer.MatchedPairs(_uiState.value.matched))
            }
        }

        /** Elige una pieza izquierda; si había una derecha, empareja (ADR-036). */
        fun selectLeft(canonicalLeft: Int) {
            val state = _uiState.value
            if (state.answerLocked || state.matched.containsKey(canonicalLeft)) return
            val right = state.selectedRight
            if (right != null) {
                pair(canonicalLeft, right)
            } else {
                _uiState.update { it.copy(selectedLeft = canonicalLeft, selectedRight = null) }
            }
        }

        /** Elige una pieza derecha; si había una izquierda, empareja (ADR-036). */
        fun selectRight(canonicalRight: Int) {
            val state = _uiState.value
            if (state.answerLocked || state.matched.containsValue(canonicalRight)) return
            val left = state.selectedLeft
            if (left != null) {
                pair(left, canonicalRight)
            } else {
                _uiState.update { it.copy(selectedRight = canonicalRight, selectedLeft = null) }
            }
        }

        private fun pair(
            left: Int,
            right: Int,
        ) {
            _uiState.update { state ->
                if (state.matched.containsKey(left) || state.matched.containsValue(right)) {
                    state
                } else {
                    state.copy(
                        matched = state.matched + (left to right),
                        selectedLeft = null,
                        selectedRight = null,
                    )
                }
            }
        }

        /** Separa una pareja ya encajada para poder reasignarla (ADR-029). */
        fun unpair(canonicalLeft: Int) {
            if (_uiState.value.answerLocked) return
            _uiState.update { state ->
                state.copy(
                    matched = state.matched - canonicalLeft,
                    selectedLeft = null,
                    selectedRight = null,
                )
            }
        }

        /** Coloca una opción en el hueco; si estaba en otro, la traslada. */
        fun fillClozeBlank(
            blankId: String,
            option: String,
        ) {
            if (_uiState.value.answerLocked) return
            _uiState.update { state ->
                val previousHolder =
                    state.clozeFilled.entries
                        .firstOrNull { entry -> entry.value == option }
                        ?.key
                val withoutPrevious = previousHolder?.let { state.clozeFilled - it } ?: state.clozeFilled
                state.copy(clozeFilled = withoutPrevious + (blankId to option))
            }
        }

        /** Libera un hueco: la opción vuelve al tablero de opciones (F2.3). */
        fun clearClozeBlank(blankId: String) {
            if (_uiState.value.answerLocked) return
            _uiState.update { state -> state.copy(clozeFilled = state.clozeFilled - blankId) }
        }

        /** El usuario verifica el cloze completo (ClozeDragDrop). */
        fun submitCloze() {
            val exercise = _uiState.value.exercises.getOrNull(_uiState.value.index) as? Exercise.ClozeDragDrop
            if (exercise != null && exercise.blanks.isNotEmpty() && _uiState.value.clozeFilled.size == exercise.blanks.size) {
                submitAnswer(ExerciseAnswer.ClozeFilled(_uiState.value.clozeFilled))
            }
        }

        /** Toca una ficha concreta del tablero y la alterna (B-06). */
        fun toggleToken(id: Int) {
            if (_uiState.value.answerLocked) return
            _uiState.update { state ->
                val board =
                    TokenBoardRules.toggle(
                        id = id,
                        board = TokenBoard(available = state.availableTokens, chosen = state.chosenTokens),
                    )
                state.copy(
                    chosenTokens = board.chosen,
                    availableTokens = board.available,
                )
            }
        }

        /** Continúa al siguiente ejercicio (o finaliza la lección). */
        fun advance() {
            val state = _uiState.value
            if (state.lessonCancelled) return
            if (state.feedback == null) return
            val nextIndex = state.index + 1
            if (nextIndex < state.exercises.size) {
                _uiState.update { it.copy(index = nextIndex, feedback = null) }
                persistSession(_uiState.value)
                prepareExercise(nextIndex)
            } else if (!state.correctingMistakes && state.mistakes.isNotEmpty()) {
                _uiState.update { it.copy(showMistakeReview = true, feedback = null) }
                persistSession(_uiState.value)
            } else {
                finishLesson()
            }
        }

        /** Repite solo los ejercicios fallados antes de cerrar la lección (ADR-036). */
        fun retryMistakes() {
            val state = _uiState.value
            if (state.mistakes.isEmpty()) {
                finishLesson()
                return
            }
            _uiState.update {
                it.copy(
                    exercises = state.mistakes,
                    index = 0,
                    correctingMistakes = true,
                    showMistakeReview = false,
                    feedback = null,
                    selectedOption = null,
                    typedText = "",
                    answerLocked = false,
                    submitting = false,
                    retryCount = 0,
                    heartConsumedForExercise = false,
                    xpAwardedForExercise = false,
                    listening = false,
                    recognized = null,
                    availableTokens = emptyList(),
                    chosenTokens = emptyList(),
                    shuffledRights = emptyList(),
                    selectedLeft = null,
                    selectedRight = null,
                    matched = emptyMap(),
                    clozeFilled = emptyMap(),
                    freeSpeaking = FreeSpeakingState(),
                )
            }
            persistSession(_uiState.value)
            prepareExercise(0)
        }

        /** Cierra la corrección y muestra la tabla final sin repetir (ADR-036). */
        fun dismissMistakeReview() {
            finishLesson()
        }

        /**
         * Reintenta un ejercicio de escucha/producción sin consumir otra vida
         * (ADR-030). En dictado se conserva el texto para editarlo; en voz se
         * limpia la transcripción anterior.
         */
        fun retry() {
            val state = _uiState.value
            val feedback = state.feedback ?: return
            if (!feedback.retryAvailable || state.retryCount >= RetryPolicy.MAX_RETRIES) return
            val exercise = state.exercises.getOrNull(state.index) ?: return
            _uiState.update { current ->
                current.copy(
                    feedback = null,
                    selectedOption = null,
                    answerLocked = false,
                    submitting = false,
                    retryCount = current.retryCount + 1,
                    listening = false,
                    recognized = null,
                    freeSpeaking =
                        if (exercise is Exercise.FreeSpeakingPrompt) {
                            FreeSpeakingState()
                        } else {
                            current.freeSpeaking
                        },
                )
            }
        }

        /** Renuncia al reintento y revela la respuesta correcta (ADR-030). */
        fun revealAnswer() {
            val state = _uiState.value
            val feedback = state.feedback ?: return
            if (!feedback.retryAvailable || feedback.correctAnswer != null) return
            val exercise = state.exercises.getOrNull(state.index) ?: return
            _uiState.update {
                it.copy(
                    feedback =
                        feedback.copy(
                            correctAnswer = exercise.answerKey(),
                            retryAvailable = false,
                        ),
                )
            }
        }

        /** Cierra la micro-clase y empieza (o retoma) los ejercicios. */
        fun beginLesson() {
            if (_uiState.value.brief == null) return
            _uiState.update { it.copy(briefVisible = false, briefSeen = true) }
        }

        /** Reabre la micro-clase desde la cabecera "Guía" (ADR-028). */
        fun openBrief() {
            val state = _uiState.value
            if (state.brief == null || state.loading) return
            if (state.invalid || state.finished || state.lessonCancelled) return
            _uiState.update { it.copy(briefVisible = true) }
        }

        private fun startLesson(session: LessonSession) {
            this.session = session
            val language = session.course.toLanguage
            targetLanguage = language
            val brief = LessonBriefBuilder.build(session.lesson)
            val allExercises = session.lesson.exercises
            val restoredMistakeIds = savedStateHandle.get<ArrayList<String>>(LESSON_MISTAKES_KEY).orEmpty()
            val restoredMistakes =
                restoredMistakeIds.mapNotNull { id ->
                    allExercises.firstOrNull { exercise -> exercise.id.value == id }
                }
            val restoredCorrecting = savedStateHandle.get<Boolean>(LESSON_CORRECTING_KEY) ?: false
            val exercises =
                if (restoredCorrecting && restoredMistakes.isNotEmpty()) {
                    restoredMistakes
                } else {
                    allExercises
                }
            val restoredIndex =
                (savedStateHandle.get<Int>(LESSON_INDEX_KEY) ?: 0)
                    .coerceIn(0, (exercises.size - 1).coerceAtLeast(0))
            val restoredCorrect = savedStateHandle.get<Int>(LESSON_CORRECT_KEY) ?: 0
            val restoredIncorrect = savedStateHandle.get<Int>(LESSON_INCORRECT_KEY) ?: 0
            val restoredSkipped = savedStateHandle.get<Int>(LESSON_SKIPPED_KEY) ?: 0
            val restoredXp = savedStateHandle.get<Int>(LESSON_XP_KEY) ?: 0
            val restoredGradedIds = savedStateHandle.get<ArrayList<String>>(LESSON_GRADED_IDS_KEY).orEmpty()
            val resumed =
                savedStateHandle.contains(LESSON_INDEX_KEY) ||
                    savedStateHandle.contains(LESSON_XP_KEY)
            gradedExercises.clear()
            gradedExercises += restoredGradedIds
            gradedExercises += restoredMistakes.map { exercise -> exercise.id.value }
            finishing = false
            _uiState.update {
                it.copy(
                    loading = false,
                    lessonTitle = session.lesson.title,
                    brief = brief,
                    briefVisible = !resumed,
                    briefSeen = resumed,
                    exercises = exercises,
                    index = restoredIndex,
                    xpEarned = restoredXp,
                    glossary = LessonGlossary.fromLesson(session.lesson),
                    totalExercises = allExercises.size,
                    correctCount = restoredCorrect,
                    incorrectCount = restoredIncorrect,
                    skippedCount = restoredSkipped,
                    score =
                        LessonAnswerRules.score(
                            totalExercises = allExercises.size,
                            skipped = restoredSkipped,
                            correct = restoredCorrect,
                        ),
                    mistakes = restoredMistakes,
                    showMistakeReview = false,
                    correctingMistakes = restoredCorrecting && restoredMistakes.isNotEmpty(),
                    audioAvailable = ttsEngine.isReadyFor(language),
                    speechAvailable = sttEngine.isReadyFor(language),
                )
            }
            prepareExercise(restoredIndex)
            viewModelScope.launch { ttsEngine.warmUp(language) }
            viewModelScope.launch { sttEngine.warmUp(language) }
            viewModelScope.launch {
                combine(ttsEngine.stateFor(language), sttEngine.stateFor(language)) { tts, stt ->
                    tts.isWarmingUp || stt.isWarmingUp
                }.collect { warmingUp ->
                    _uiState.update { state ->
                        state.copy(
                            audioAvailable = ttsEngine.isReadyFor(language),
                            speechAvailable = sttEngine.isReadyFor(language),
                            voiceWarmingUp = warmingUp,
                        )
                    }
                }
            }
        }

        /** Guarda el avance mínimo para sobrevivir a la muerte del proceso.
         *
         * Además del cursor, se guardan los ejercicios ya calificados y la
         * fase de corrección: al restaurar, un ejercicio que ya se había
         * calificado no vuelve a producir XP ni a mover los contadores, y la
         * revisión de errores sigue en el mismo punto (B-08).
         */
        private fun persistSession(state: LessonUiState) {
            savedStateHandle[LESSON_INDEX_KEY] = state.index
            savedStateHandle[LESSON_XP_KEY] = state.xpEarned
            savedStateHandle[LESSON_CORRECT_KEY] = state.correctCount
            savedStateHandle[LESSON_INCORRECT_KEY] = state.incorrectCount
            savedStateHandle[LESSON_SKIPPED_KEY] = state.skippedCount
            savedStateHandle[LESSON_MISTAKES_KEY] =
                ArrayList(state.mistakes.map { exercise -> exercise.id.value })
            savedStateHandle[LESSON_GRADED_IDS_KEY] = ArrayList(gradedExercises)
            savedStateHandle[LESSON_CORRECTING_KEY] = state.correctingMistakes
        }

        private fun clearPersistedSession() {
            savedStateHandle.remove<Int>(LESSON_INDEX_KEY)
            savedStateHandle.remove<Int>(LESSON_XP_KEY)
            savedStateHandle.remove<Int>(LESSON_CORRECT_KEY)
            savedStateHandle.remove<Int>(LESSON_INCORRECT_KEY)
            savedStateHandle.remove<Int>(LESSON_SKIPPED_KEY)
            savedStateHandle.remove<ArrayList<String>>(LESSON_MISTAKES_KEY)
            savedStateHandle.remove<ArrayList<String>>(LESSON_GRADED_IDS_KEY)
            savedStateHandle.remove<Boolean>(LESSON_CORRECTING_KEY)
        }

        /** Prepara los paneles de respuesta del ejercicio `index`. */
        private fun prepareExercise(index: Int) {
            val exercise = _uiState.value.exercises.getOrNull(index) ?: return
            _uiState.update {
                it.copy(
                    feedback = null,
                    selectedOption = null,
                    selectedRight = null,
                    typedText = "",
                    answerLocked = false,
                    submitting = false,
                    retryCount = 0,
                    heartConsumedForExercise = false,
                    xpAwardedForExercise = false,
                    listening = false,
                    recognized = null,
                )
            }
            when (exercise) {
                is Exercise.Translate ->
                    _uiState.update {
                        it.copy(
                            availableTokens =
                                (exercise.correctTokens + exercise.distractorTokens)
                                    .shuffled()
                                    .mapIndexed { index, text -> IndexedToken(index, text) },
                            chosenTokens = emptyList(),
                            shuffledRights = emptyList(),
                            matched = emptyMap(),
                            clozeFilled = emptyMap(),
                            freeSpeaking = FreeSpeakingState(),
                            selectedLeft = null,
                            selectedRight = null,
                        )
                    }
                is Exercise.OrderWords ->
                    _uiState.update {
                        it.copy(
                            availableTokens =
                                exercise.tokens
                                    .shuffled()
                                    .mapIndexed { index, text -> IndexedToken(index, text) },
                            chosenTokens = emptyList(),
                            shuffledRights = emptyList(),
                            matched = emptyMap(),
                            clozeFilled = emptyMap(),
                            freeSpeaking = FreeSpeakingState(),
                            selectedLeft = null,
                            selectedRight = null,
                        )
                    }
                is Exercise.MatchPairs -> preparePairs(exercise.pairs.size)
                is Exercise.SemanticMatch -> preparePairs(exercise.pairs.size)
                is Exercise.ClozeDragDrop ->
                    _uiState.update {
                        it.copy(
                            availableTokens = emptyList(),
                            chosenTokens = emptyList(),
                            shuffledRights = emptyList(),
                            matched = emptyMap(),
                            clozeFilled = emptyMap(),
                            freeSpeaking = FreeSpeakingState(),
                            selectedLeft = null,
                            selectedRight = null,
                        )
                    }
                else ->
                    _uiState.update {
                        it.copy(
                            availableTokens = emptyList(),
                            chosenTokens = emptyList(),
                            shuffledRights = emptyList(),
                            matched = emptyMap(),
                            clozeFilled = emptyMap(),
                            freeSpeaking = FreeSpeakingState(),
                            selectedLeft = null,
                            selectedRight = null,
                            listening = false,
                            recognized = null,
                        )
                    }
            }
        }

        /** Prepara el tablero de parejas de `SemanticMatch`/`MatchPairs`. */
        private fun preparePairs(total: Int) {
            _uiState.update {
                it.copy(
                    availableTokens = emptyList(),
                    chosenTokens = emptyList(),
                    shuffledRights = List(total) { index -> index }.shuffled(),
                    matched = emptyMap(),
                    clozeFilled = emptyMap(),
                    freeSpeaking = FreeSpeakingState(),
                    selectedLeft = null,
                    selectedRight = null,
                )
            }
        }

        private fun submitAnswer(
            answer: ExerciseAnswer,
            grammarPoints: List<GrammarPoint> = emptyList(),
            consumeHeart: Boolean = true,
            allowRetry: Boolean = true,
            skipped: Boolean = false,
        ) {
            val state = _uiState.value
            if (state.feedback != null || state.answerLocked || state.submitting) return
            val exercise = state.exercises.getOrNull(state.index) ?: return
            val correction = state.correctingMistakes
            val shouldConsumeHeart =
                !correction &&
                    RetryPolicy.shouldRequestHeartConsumption(consumeHeart, state.heartConsumedForExercise)
            _uiState.update { it.copy(submitting = true, answerLocked = true) }
            viewModelScope.launch {
                when (val result = registerAnswerUseCase(exercise, answer, grammarPoints, shouldConsumeHeart)) {
                    is Result.Success -> {
                        val outcome = result.value
                        val grading = outcome.grading
                        val firstGrading = gradedExercises.add(exercise.id.value)
                        val cultural = session?.let { CulturalNoteMatcher.noteFor(it.lesson, exercise) }
                        _uiState.update { current ->
                            val awardXp =
                                !skipped &&
                                    !correction &&
                                    RetryPolicy.shouldAwardXp(grading.isCorrect, current.xpAwardedForExercise)
                            val consumed = shouldConsumeHeart && !grading.isCorrect
                            val canRetry =
                                !skipped &&
                                    RetryPolicy.canRetry(
                                        exercise = exercise,
                                        isCorrect = grading.isCorrect,
                                        retryCount = current.retryCount,
                                        allowRetry = allowRetry,
                                    )
                            val effects =
                                LessonAnswerRules.effects(
                                    skipped = skipped,
                                    isCorrect = grading.isCorrect,
                                    firstGrading = firstGrading,
                                    combo = current.combo,
                                )
                            val skippedDelta = if (skipped) 1 else 0
                            val nextSkipped = current.skippedCount + skippedDelta
                            current.copy(
                                feedback =
                                    ExerciseFeedback(
                                        isCorrect = grading.isCorrect,
                                        diagnostic = if (skipped) null else grading.diagnosticFeedback,
                                        idiom = cultural?.idiom,
                                        culturalNote = cultural?.note,
                                        successExplanation =
                                            if (grading.isCorrect && !skipped) {
                                                successExplanation(exercise, grading)
                                            } else {
                                                null
                                            },
                                        // En tipos reintentables no revelamos la respuesta
                                        // para que el reintento siga siendo pedagógico.
                                        correctAnswer =
                                            if (canRetry || skipped) {
                                                null
                                            } else {
                                                exercise.answerKey()
                                            },
                                        retryAvailable = canRetry,
                                        skipped = skipped,
                                    ),
                                xpEarned =
                                    if (awardXp) {
                                        current.xpEarned + XP_PER_CORRECT_ANSWER
                                    } else {
                                        current.xpEarned
                                    },
                                xpAwardedForExercise =
                                    current.xpAwardedForExercise || (grading.isCorrect && !skipped),
                                hearts = outcome.hearts?.hearts ?: current.hearts,
                                combo = effects.nextCombo,
                                correctCount = current.correctCount + effects.correctDelta,
                                incorrectCount = current.incorrectCount + effects.incorrectDelta,
                                skippedCount = nextSkipped,
                                score =
                                    LessonAnswerRules.score(
                                        totalExercises = current.totalExercises,
                                        skipped = nextSkipped,
                                        correct = current.correctCount + effects.correctDelta,
                                    ),
                                mistakes =
                                    if (effects.addMistake) {
                                        current.mistakes + exercise
                                    } else {
                                        current.mistakes
                                    },
                                lessonCancelled = outcome.lessonCancelled,
                                answerLocked = true,
                                submitting = false,
                                heartConsumedForExercise = current.heartConsumedForExercise || consumed,
                            )
                        }
                        persistSession(_uiState.value)
                        if (!correction && !skipped && exercise is Exercise.FreeSpeakingPrompt) {
                            recordSpeakingMetrics(answer)
                        }
                    }
                    is Result.Error ->
                        _uiState.update {
                            it.copy(
                                invalid = true,
                                loading = false,
                                submitting = false,
                                answerLocked = false,
                            )
                        }
                }
            }
        }

        /**
         * Persiste la métrica oral del intento (ADR-050). La escritura es
         * best-effort: un fallo de Room no invalida la respuesta ya calificada.
         */
        private suspend fun recordSpeakingMetrics(answer: ExerciseAnswer) {
            val feedback = _uiState.value.freeSpeaking.breakdown ?: return
            if (feedback.wpm <= 0.0) return
            val spoken = answer as? ExerciseAnswer.FreeSpoken
            val score =
                spoken?.selfAssessmentScore?.toFloat()
                    ?: if (feedback.totalClusters > 0) {
                        feedback.coveredClusters.toFloat() / feedback.totalClusters
                    } else {
                        0f
                    }
            progressRepository.recordSpeakingMetrics(
                lessonId = LessonId(lessonId),
                wpm = feedback.wpm.toFloat(),
                score = score,
                passed = feedback.passes,
            )
        }

        private fun successExplanation(
            exercise: Exercise,
            grading: GradingResult,
        ): String {
            grading.successExplanation?.takeIf { it.isNotBlank() }?.let { return it }
            val ruleId =
                grading.matchedSuccessRuleId
                    ?: exercise.feedbackOnSuccess?.ruleRef
                    ?: exercise.feedbackOnError?.ruleRef
            val rule =
                ruleId?.let { id ->
                    session?.lesson?.grammarPoints?.firstOrNull { point -> point.id == id }
                }
            if (rule != null) {
                val description = rule.description
                return if (description != null) {
                    "La respuesta sigue la regla «${rule.label}»: $description"
                } else {
                    "La respuesta sigue la regla «${rule.label}»."
                }
            }
            if (exercise is Exercise.FreeSpeakingPrompt) {
                return "Has cubierto las ideas clave y cumples los criterios de la tarea."
            }
            exercise.feedbackOnError
                ?.explanation
                ?.takeIf { it.isNotBlank() }
                ?.let { return it }
            return when (exercise) {
                is Exercise.MultipleChoice,
                is Exercise.KanaChoose,
                is Exercise.KanjiReading,
                is Exercise.ParticleFill,
                is Exercise.ListenChoose,
                is Exercise.AudioOnlyPrompt,
                -> "Has elegido la opción que responde al ejercicio."
                is Exercise.Translate,
                is Exercise.OrderWords,
                -> "Has reconstruido la secuencia correcta."
                is Exercise.MatchPairs,
                is Exercise.SemanticMatch,
                -> "Todas las parejas son correctas."
                is Exercise.ListenType -> "Has escrito exactamente lo que se dijo."
                is Exercise.SpeakRepeat -> "La frase coincide con el modelo."
                is Exercise.ClozeDragDrop -> "Todos los huecos encajan."
                is Exercise.FreeSpeakingPrompt ->
                    "Cumples los criterios de la tarea; compara tu respuesta con la modelo."
            }
        }

        private fun finishLesson() {
            val state = _uiState.value
            if (finishing || state.lessonCancelled || state.finished) return
            finishing = true
            val earned = state.xpEarned
            val scorePercent = state.score.percentage
            viewModelScope.launch {
                completeLessonUseCase(LessonId(lessonId), earned, scorePercent)
                session?.let { current ->
                    reviewRepository.collectFromLesson(current.course, current.lesson)
                }
                _uiState.update {
                    it.copy(
                        finished = true,
                        feedback = null,
                        showMistakeReview = false,
                        score =
                            LessonAnswerRules.score(
                                totalExercises = it.totalExercises,
                                skipped = it.skippedCount,
                                correct = it.correctCount,
                            ),
                    )
                }
                clearPersistedSession()
                finishing = false
            }
        }

        companion object {
            const val LESSON_COURSE_KEY = "courseId"
            const val LESSON_ID_KEY = "lessonId"
            private const val LESSON_INDEX_KEY = "lessonIndex"
            private const val LESSON_XP_KEY = "lessonXp"
            private const val LESSON_CORRECT_KEY = "lessonCorrect"
            private const val LESSON_INCORRECT_KEY = "lessonIncorrect"
            private const val LESSON_SKIPPED_KEY = "lessonSkipped"
            private const val LESSON_MISTAKES_KEY = "lessonMistakes"
            private const val LESSON_GRADED_IDS_KEY = "lessonGradedIds"
            private const val LESSON_CORRECTING_KEY = "lessonCorrectingMistakes"
            private const val NORMAL_PLAYBACK_SPEED = 1.0f
            private const val SLOW_PLAYBACK_SPEED = 0.75f
            private const val SILENCE_DB = -60f
            private const val WAVE_BARS = 48
            private const val MAX_SELF_ASSESSMENT_LEVEL = 2
        }
    }

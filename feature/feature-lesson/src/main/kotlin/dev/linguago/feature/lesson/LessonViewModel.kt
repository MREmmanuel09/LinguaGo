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
import dev.linguago.core.domain.logic.CulturalNoteMatcher
import dev.linguago.core.domain.logic.FreeSpeakingFeedback
import dev.linguago.core.domain.logic.FreeSpeakingHeuristicGrader
import dev.linguago.core.domain.logic.SpokenFreeResponse
import dev.linguago.core.domain.model.CourseId
import dev.linguago.core.domain.model.Exercise
import dev.linguago.core.domain.model.GrammarPoint
import dev.linguago.core.domain.model.Language
import dev.linguago.core.domain.model.LessonId
import dev.linguago.core.domain.model.LessonSession
import dev.linguago.core.domain.model.XP_PER_CORRECT_ANSWER
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
 * Estado del motor de lección (ADR-002: FSM). `exercises` nunca cambia
 * durante la sesión; lo mutable es el índice actual, los paneles de
 * respuesta (tokens/parejas) y el feedback. Las vidas son una penalización
 * no bloqueante (ADR-025): a 0 la lección se cancela y no se guarda su XP.
 */
data class LessonUiState(
    val loading: Boolean = true,
    val invalid: Boolean = false,
    val lessonTitle: String = "",
    val exercises: List<Exercise> = emptyList(),
    val index: Int = 0,
    val xpEarned: Int = 0,
    val feedback: ExerciseFeedback? = null,
    val finished: Boolean = false,
    /** Vidas vigentes; se actualizan al responder (ADR-025). */
    val hearts: Int = HEARTS_MAX,
    /** `true` cuando el jugador se quedó sin vidas y la lección no se guarda. */
    val lessonCancelled: Boolean = false,
    // Tablero de tokens (Translate / OrderWords)
    val availableTokens: List<String> = emptyList(),
    val chosenTokens: List<String> = emptyList(),
    // Parejas (MatchPairs): orden de dibujo de las derechas + estado de asocio
    val shuffledRights: List<Int> = emptyList(),
    val selectedLeft: Int? = null,
    val matched: Map<Int, Int> = emptyMap(),
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
        savedStateHandle: SavedStateHandle,
        private val startLessonUseCase: StartLessonUseCase,
        private val registerAnswerUseCase: RegisterAnswerUseCase,
        private val completeLessonUseCase: CompleteLessonUseCase,
        private val observeHeartsUseCase: ObserveHeartsUseCase,
        private val ttsEngine: TtsEngine,
        private val sttEngine: SttEngine,
        private val reviewRepository: ReviewRepository,
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
        private var freeSpeakingStartedAt = 0L

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
        fun pickOption(index: Int) = submitAnswer(ExerciseAnswer.OptionPicked(index))

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
            viewModelScope.launch { ttsEngine.speak(audioText, language, speed) }
        }

        /**
         * Inicia la captura libre (F2.5): energía RMS y parciales de Vosk
         * actualizan el visualizador y las ideas clave en vivo (ADR-019/020).
         */
        fun startFreeSpeaking() {
            val language = targetLanguage ?: return
            val exercise = currentExercise() as? Exercise.FreeSpeakingPrompt ?: return
            val state = _uiState.value
            if (state.freeSpeaking.recording || !state.speechAvailable || state.feedback != null) return
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
            if (_uiState.value.feedback != null) return
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
            if (state.feedback != null) return
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

        /** Sin micrófono o sin permiso: registra el intento y deja avanzar (I3). */
        fun skipFreeSpeaking() {
            if (_uiState.value.feedback != null) return
            if (currentExercise() !is Exercise.FreeSpeakingPrompt) return
            submitAnswer(
                ExerciseAnswer.FreeSpoken(transcript = "", durationMillis = 0L),
                session?.lesson?.grammarPoints.orEmpty(),
                consumeHeart = false,
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
            if (state.listening || !state.speechAvailable) return
            _uiState.update { it.copy(listening = true, recognized = null) }
            viewModelScope.launch {
                val result = sttEngine.transcribe(language)
                _uiState.update { it.copy(listening = false, recognized = result.text.ifBlank { null }) }
                submitAnswer(ExerciseAnswer.SpokenText(result.text))
            }
        }

        /** Sin STT/permiso: indica que no puede hablar y avanza (incorrecto, I3). */
        fun skipSpeech() {
            if (_uiState.value.listening) return
            submitAnswer(ExerciseAnswer.SpokenText(""), consumeHeart = false)
        }

        /** El usuario verifica el tablero de tokens (Translate / OrderWords). */
        fun submitTokens() {
            val tokens = _uiState.value.chosenTokens
            if (tokens.isNotEmpty()) submitAnswer(ExerciseAnswer.TokenSequence(tokens))
        }

        /** El usuario envía el dictado libre (ListenType). */
        fun submitTypedText(text: String) = submitAnswer(ExerciseAnswer.TypedText(text))

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

        /** Toca una izquierda (canónica) para elegir el origen de la pareja. */
        fun selectLeft(canonicalLeft: Int) {
            _uiState.update { it.copy(selectedLeft = canonicalLeft) }
        }

        /** Coloca una opción en el hueco; si estaba en otro, la traslada. */
        fun fillClozeBlank(
            blankId: String,
            option: String,
        ) {
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
            _uiState.update { state -> state.copy(clozeFilled = state.clozeFilled - blankId) }
        }

        /** El usuario verifica el cloze completo (ClozeDragDrop). */
        fun submitCloze() {
            val exercise = _uiState.value.exercises.getOrNull(_uiState.value.index) as? Exercise.ClozeDragDrop
            if (exercise != null && exercise.blanks.isNotEmpty() && _uiState.value.clozeFilled.size == exercise.blanks.size) {
                submitAnswer(ExerciseAnswer.ClozeFilled(_uiState.value.clozeFilled))
            }
        }

        /** Empareja la izquierda seleccionada con la derecha (canónica) tocada. */
        fun matchLeftTo(canonicalRight: Int) {
            val left = _uiState.value.selectedLeft ?: return
            _uiState.update { state ->
                state.copy(
                    matched = state.matched + (left to canonicalRight),
                    selectedLeft = null,
                )
            }
        }

        /** Toca un token del tablero: alterna entre elegidos y disponibles. */
        fun toggleToken(token: String) {
            _uiState.update { state ->
                if (token in state.chosenTokens) {
                    state.copy(
                        chosenTokens = state.chosenTokens - token,
                        availableTokens = state.availableTokens + token,
                    )
                } else {
                    state.copy(
                        chosenTokens = state.chosenTokens + token,
                        availableTokens = state.availableTokens - token,
                    )
                }
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
                prepareExercise(nextIndex)
            } else {
                finishLesson()
            }
        }

        private fun startLesson(session: LessonSession) {
            this.session = session
            val language = session.course.toLanguage
            targetLanguage = language
            _uiState.update {
                it.copy(
                    loading = false,
                    lessonTitle = session.lesson.title,
                    exercises = session.lesson.exercises,
                    index = 0,
                    audioAvailable = ttsEngine.isReadyFor(language),
                    speechAvailable = sttEngine.isReadyFor(language),
                )
            }
            prepareExercise(0)
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

        /** Prepara los paneles de respuesta del ejercicio `index`. */
        private fun prepareExercise(index: Int) {
            val exercise = _uiState.value.exercises.getOrNull(index) ?: return
            when (exercise) {
                is Exercise.Translate ->
                    _uiState.update {
                        it.copy(
                            availableTokens = (exercise.correctTokens + exercise.distractorTokens).shuffled(),
                            chosenTokens = emptyList(),
                            shuffledRights = emptyList(),
                            matched = emptyMap(),
                            clozeFilled = emptyMap(),
                            freeSpeaking = FreeSpeakingState(),
                            selectedLeft = null,
                        )
                    }
                is Exercise.OrderWords ->
                    _uiState.update {
                        it.copy(
                            availableTokens = exercise.tokens.shuffled(),
                            chosenTokens = emptyList(),
                            shuffledRights = emptyList(),
                            matched = emptyMap(),
                            clozeFilled = emptyMap(),
                            freeSpeaking = FreeSpeakingState(),
                            selectedLeft = null,
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
                )
            }
        }

        private fun submitAnswer(
            answer: ExerciseAnswer,
            grammarPoints: List<GrammarPoint> = emptyList(),
            consumeHeart: Boolean = true,
        ) {
            val exercise = _uiState.value.exercises.getOrNull(_uiState.value.index) ?: return
            viewModelScope.launch {
                when (val result = registerAnswerUseCase(exercise, answer, grammarPoints, consumeHeart)) {
                    is Result.Success -> {
                        val outcome = result.value
                        val grading = outcome.grading
                        val cultural = session?.let { CulturalNoteMatcher.noteFor(it.lesson, exercise) }
                        _uiState.update { state ->
                            state.copy(
                                feedback =
                                    ExerciseFeedback(
                                        isCorrect = grading.isCorrect,
                                        diagnostic = grading.diagnosticFeedback,
                                        idiom = cultural?.idiom,
                                        culturalNote = cultural?.note,
                                    ),
                                xpEarned =
                                    if (grading.isCorrect) {
                                        state.xpEarned + XP_PER_CORRECT_ANSWER
                                    } else {
                                        state.xpEarned
                                    },
                                hearts = outcome.hearts?.hearts ?: state.hearts,
                                lessonCancelled = outcome.lessonCancelled,
                            )
                        }
                    }
                    is Result.Error -> _uiState.update { it.copy(invalid = true, loading = false) }
                }
            }
        }

        private fun finishLesson() {
            if (_uiState.value.lessonCancelled) return
            val earned = _uiState.value.xpEarned
            viewModelScope.launch {
                completeLessonUseCase(LessonId(lessonId), earned)
                session?.let { current ->
                    reviewRepository.collectFromLesson(current.course, current.lesson)
                }
                _uiState.update { it.copy(finished = true, feedback = null) }
            }
        }

        companion object {
            const val LESSON_COURSE_KEY = "courseId"
            const val LESSON_ID_KEY = "lessonId"
            private const val NORMAL_PLAYBACK_SPEED = 1.0f
            private const val SLOW_PLAYBACK_SPEED = 0.75f
            private const val SILENCE_DB = -60f
            private const val WAVE_BARS = 48
            private const val MAX_SELF_ASSESSMENT_LEVEL = 2
        }
    }

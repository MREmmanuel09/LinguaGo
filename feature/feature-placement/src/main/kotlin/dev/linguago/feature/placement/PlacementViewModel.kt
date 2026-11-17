package dev.linguago.feature.placement

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.linguago.core.audio.SttEngine
import dev.linguago.core.audio.TtsEngine
import dev.linguago.core.audio.isWarmingUp
import dev.linguago.core.common.Result
import dev.linguago.core.domain.logic.ExerciseGrader
import dev.linguago.core.domain.model.CefrLevel
import dev.linguago.core.domain.model.Course
import dev.linguago.core.domain.model.CourseId
import dev.linguago.core.domain.model.Exercise
import dev.linguago.core.domain.model.Language
import dev.linguago.core.domain.placement.PlacementEvaluator
import dev.linguago.core.domain.placement.PlacementExam
import dev.linguago.core.domain.placement.PlacementExamGenerator
import dev.linguago.core.domain.placement.PlacementQuestion
import dev.linguago.core.domain.placement.PlacementResult
import dev.linguago.core.domain.repository.ProgressRepository
import dev.linguago.core.domain.usecase.ExerciseAnswer
import dev.linguago.core.domain.usecase.LoadCourseUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

public const val PLACEMENT_COURSE_KEY: String = "courseId"

@HiltViewModel
public class PlacementViewModel
    @Inject
    constructor(
        savedStateHandle: SavedStateHandle,
        private val loadCourseUseCase: LoadCourseUseCase,
        private val progressRepository: ProgressRepository,
        private val ttsEngine: TtsEngine,
        private val sttEngine: SttEngine,
    ) : ViewModel() {
        private val courseIdArg: String = savedStateHandle[PLACEMENT_COURSE_KEY] ?: "es-en"

        private val _uiState = MutableStateFlow(PlacementUiState(loading = true, courseId = courseIdArg))
        public val uiState: StateFlow<PlacementUiState> = _uiState.asStateFlow()

        private var course: Course? = null
        private var exam: PlacementExam? = null
        private var targetLanguage: Language? = null
        private val answers = mutableMapOf<Pair<Int, Int>, ExerciseAnswer>()
        private var speechJob: Job? = null
        private var audioStateJob: Job? = null

        init {
            viewModelScope.launch { load() }
        }

        private suspend fun load() {
            _uiState.update { it.copy(loading = true, error = null) }
            when (val result = loadCourseUseCase(CourseId(courseIdArg))) {
                is Result.Error -> _uiState.update { it.copy(loading = false, error = "No se pudo cargar el curso.") }
                is Result.Success -> {
                    course = result.value
                    targetLanguage = result.value.toLanguage
                    warmUpAudio(result.value.toLanguage)
                    val generated =
                        PlacementExamGenerator.generate(
                            course = result.value,
                            questionsPerLevel = QUESTIONS_PER_LEVEL,
                            supported = ::isUiSupported,
                        )
                    exam = generated
                    answers.clear()
                    _uiState.update {
                        it
                            .copy(
                                loading = false,
                                error = if (generated.totalQuestions == 0) "No hay preguntas disponibles para este curso." else null,
                                exam = generated,
                                blockIndex = 0,
                                questionIndex = 0,
                                answered = false,
                                lastCorrect = null,
                                finished = false,
                                result = null,
                                applied = false,
                                unlockedCount = 0,
                                applyError = null,
                                speaking = false,
                                listening = false,
                            ).prepareForQuestion(generated, 0, 0)
                    }
                }
            }
        }

        private fun warmUpAudio(language: Language) {
            audioStateJob?.cancel()
            viewModelScope.launch { ttsEngine.warmUp(language) }
            viewModelScope.launch { sttEngine.warmUp(language) }
            audioStateJob =
                viewModelScope.launch {
                    combine(ttsEngine.stateFor(language), sttEngine.stateFor(language)) { tts, stt ->
                        tts.isWarmingUp || stt.isWarmingUp
                    }.collect { warmingUp ->
                        _uiState.update {
                            it.copy(
                                audioAvailable = ttsEngine.isReadyFor(language),
                                speechAvailable = sttEngine.isReadyFor(language),
                                voiceWarmingUp = warmingUp,
                            )
                        }
                    }
                }
        }

        /** Reproduce el audio del ejercicio actual (enunciado). */
        public fun playAudio() = speakCurrent(NORMAL_PLAYBACK_SPEED)

        /** Reproduce el audio a velocidad lenta. */
        public fun playAudioSlow() = speakCurrent(SLOW_PLAYBACK_SPEED)

        private fun speakCurrent(speed: Float) {
            val language = targetLanguage ?: return
            val audioText =
                _uiState.value
                    .currentQuestion()
                    ?.exercise
                    ?.audioText
                    ?.takeIf { it.isNotBlank() } ?: return
            viewModelScope.launch {
                _uiState.update { it.copy(speaking = true) }
                try {
                    ttsEngine.speak(audioText, language, speed)
                } finally {
                    _uiState.update { it.copy(speaking = false, audioAvailable = ttsEngine.isReadyFor(language)) }
                }
            }
        }

        /** Dictado por voz (STT): transcribe y rellena el texto editable. */
        public fun transcribeSpoken() {
            val language = targetLanguage ?: return
            val state = _uiState.value
            if (state.listening || state.answered || state.finished) return
            if (!state.speechAvailable) return
            _uiState.update { it.copy(listening = true) }
            speechJob?.cancel()
            speechJob =
                viewModelScope.launch {
                    try {
                        val result = sttEngine.transcribe(language)
                        _uiState.update {
                            it.copy(
                                listening = false,
                                speechAvailable = sttEngine.isReadyFor(language),
                                typedText = result.text.ifBlank { it.typedText },
                            )
                        }
                    } catch (_: Exception) {
                        _uiState.update { it.copy(listening = false) }
                    }
                }
        }

        public fun stopSpoken() {
            speechJob?.cancel()
            speechJob = null
            _uiState.update { it.copy(listening = false) }
        }

        /** Cierra el examen antes de tiempo con lo respondido hasta ahora. */
        public fun finishEarly() {
            if (_uiState.value.finished) return
            finishExam()
        }

        public fun selectOption(displayIndex: Int) {
            val state = _uiState.value
            if (state.answered || state.finished || state.loading) return
            val original = state.displayToOriginal.getOrNull(displayIndex) ?: displayIndex
            _uiState.update { it.copy(selectedOriginalIndex = original, selectedDisplayIndex = displayIndex) }
        }

        public fun updateTypedText(text: String) {
            val state = _uiState.value
            if (state.answered || state.finished) return
            _uiState.update { it.copy(typedText = text) }
        }

        public fun toggleToken(id: Int) {
            val state = _uiState.value
            if (state.answered || state.finished) return
            val chosen = state.chosenTokens.toMutableList()
            val available = state.availableTokens.toMutableList()
            val inChosen = chosen.indexOfFirst { it.id == id }
            if (inChosen >= 0) {
                val token = chosen.removeAt(inChosen)
                available += token
            } else {
                val inAvailable = available.indexOfFirst { it.id == id }
                if (inAvailable >= 0) {
                    val token = available.removeAt(inAvailable)
                    chosen += token
                }
            }
            _uiState.update { it.copy(chosenTokens = chosen, availableTokens = available) }
        }

        public fun selectLeft(left: Int) {
            val state = _uiState.value
            if (state.answered || state.finished) return
            _uiState.update { it.copy(selectedLeft = if (it.selectedLeft == left) null else left) }
        }

        public fun matchRight(right: Int) {
            val state = _uiState.value
            if (state.answered || state.finished) return
            val left = state.selectedLeft ?: return
            if (state.matched.containsKey(left) || state.matched.containsValue(right)) return
            _uiState.update { it.copy(matched = it.matched + (left to right), selectedLeft = null) }
        }

        public fun unpair(left: Int) {
            val state = _uiState.value
            if (state.answered || state.finished) return
            _uiState.update { it.copy(matched = it.matched - left) }
        }

        public fun fillClozeBlank(
            blankId: String,
            option: String,
        ) {
            val state = _uiState.value
            if (state.answered || state.finished) return
            val previous =
                state.clozeFilled.entries
                    .firstOrNull { it.value == option }
                    ?.key
            val next = state.clozeFilled.toMutableMap()
            if (previous != null) next.remove(previous)
            next[blankId] = option
            _uiState.update { it.copy(clozeFilled = next) }
        }

        public fun clearClozeBlank(blankId: String) {
            val state = _uiState.value
            if (state.answered || state.finished) return
            _uiState.update { it.copy(clozeFilled = it.clozeFilled - blankId) }
        }

        public fun canSubmit(state: PlacementUiState = _uiState.value): Boolean {
            if (state.answered || state.finished || state.loading) return false
            val question = state.currentQuestion() ?: return false
            return when (val ex = question.exercise) {
                is Exercise.MultipleChoice,
                is Exercise.ListenChoose,
                is Exercise.AudioOnlyPrompt,
                is Exercise.KanaChoose,
                is Exercise.KanjiReading,
                is Exercise.ParticleFill,
                -> state.selectedOriginalIndex != null
                is Exercise.ListenType -> state.typedText.trim().isNotEmpty()
                is Exercise.Translate,
                is Exercise.OrderWords,
                -> state.chosenTokens.isNotEmpty()
                is Exercise.MatchPairs -> state.matched.size == ex.pairs.size
                is Exercise.SemanticMatch -> state.matched.size == ex.pairs.size
                is Exercise.ClozeDragDrop -> state.clozeFilled.size == ex.blanks.size
                else -> false
            }
        }

        public fun submitAnswer() {
            val state = _uiState.value
            val currentExam = exam ?: return
            if (!canSubmit(state)) return
            val question = state.currentQuestion() ?: return
            val answer = buildAnswer(question.exercise, state) ?: return
            val key = state.blockIndex to state.questionIndex
            answers[key] = answer
            val correct = ExerciseGrader.isCorrect(question.exercise, answer)
            _uiState.update { it.copy(answered = true, lastCorrect = correct) }
        }

        private fun buildAnswer(
            exercise: Exercise,
            state: PlacementUiState,
        ): ExerciseAnswer? =
            when (exercise) {
                is Exercise.MultipleChoice,
                is Exercise.ListenChoose,
                is Exercise.AudioOnlyPrompt,
                is Exercise.KanaChoose,
                is Exercise.KanjiReading,
                is Exercise.ParticleFill,
                -> state.selectedOriginalIndex?.let { ExerciseAnswer.OptionPicked(it) }
                is Exercise.ListenType -> ExerciseAnswer.TypedText(state.typedText)
                is Exercise.Translate,
                is Exercise.OrderWords,
                -> ExerciseAnswer.TokenSequence(state.chosenTokens.map { it.text })
                is Exercise.MatchPairs,
                is Exercise.SemanticMatch,
                -> ExerciseAnswer.MatchedPairs(state.matched)
                is Exercise.ClozeDragDrop -> ExerciseAnswer.ClozeFilled(state.clozeFilled)
                else -> null
            }

        public fun next() {
            val state = _uiState.value
            val currentExam = exam ?: return
            if (!state.answered || state.finished) return

            val block = currentExam.blocks.getOrNull(state.blockIndex) ?: return
            val nextQuestion = state.questionIndex + 1
            if (nextQuestion < block.questions.size) {
                _uiState.update {
                    it
                        .copy(blockIndex = it.blockIndex, questionIndex = nextQuestion)
                        .prepareForQuestion(currentExam, it.blockIndex, nextQuestion)
                }
                return
            }

            // El examen evalúa cada nivel de forma independiente: avanzar al
            // siguiente bloque aunque el anterior no llegue al umbral permite
            // demostrar un nivel superior y no obliga a aprobar todo (ADR-058).
            val nextBlock = state.blockIndex + 1
            if (nextBlock < currentExam.blocks.size) {
                _uiState.update {
                    it
                        .copy(blockIndex = nextBlock, questionIndex = 0)
                        .prepareForQuestion(currentExam, nextBlock, 0)
                }
            } else {
                finishExam()
            }
        }

        private fun finishExam() {
            stopSpoken()
            val currentExam = exam ?: return
            val currentCourse = course ?: return
            val result = PlacementEvaluator.evaluate(currentExam, answers.toMap(), currentCourse)
            _uiState.update { it.copy(finished = true, result = result) }
        }

        public fun applyPlacement() {
            val state = _uiState.value
            val result = state.result ?: return
            if (state.applied || state.applying || result.unlockedLessonIds.isEmpty()) {
                _uiState.update { it.copy(applied = true) }
                return
            }
            viewModelScope.launch {
                _uiState.update { it.copy(applying = true) }
                when (val res = progressRepository.markLessonsCompletedByPlacement(result.unlockedLessonIds)) {
                    is Result.Error ->
                        _uiState.update { it.copy(applying = false, applyError = "No se pudo aplicar el desbloqueo.") }
                    is Result.Success ->
                        _uiState.update {
                            it.copy(
                                applying = false,
                                applied = true,
                                unlockedCount = res.value,
                                applyError = null,
                            )
                        }
                }
            }
        }

        public fun retry() {
            viewModelScope.launch { load() }
        }

        private companion object {
            const val QUESTIONS_PER_LEVEL = 10
            const val NORMAL_PLAYBACK_SPEED = 1.0f
            const val SLOW_PLAYBACK_SPEED = 0.75f
        }
    }

public data class PlacementToken(
    val id: Int,
    val text: String,
)

public data class PlacementUiState(
    val loading: Boolean = false,
    val courseId: String = "",
    val error: String? = null,
    val exam: PlacementExam? = null,
    val blockIndex: Int = 0,
    val questionIndex: Int = 0,
    val selectedOriginalIndex: Int? = null,
    val selectedDisplayIndex: Int? = null,
    val displayToOriginal: List<Int> = emptyList(),
    val typedText: String = "",
    val availableTokens: List<PlacementToken> = emptyList(),
    val chosenTokens: List<PlacementToken> = emptyList(),
    val shuffledRights: List<Int> = emptyList(),
    val matched: Map<Int, Int> = emptyMap(),
    val selectedLeft: Int? = null,
    val clozeFilled: Map<String, String> = emptyMap(),
    val answered: Boolean = false,
    val lastCorrect: Boolean? = null,
    val audioAvailable: Boolean = false,
    val speechAvailable: Boolean = false,
    val voiceWarmingUp: Boolean = true,
    val speaking: Boolean = false,
    val listening: Boolean = false,
    val finished: Boolean = false,
    val result: PlacementResult? = null,
    val applying: Boolean = false,
    val applied: Boolean = false,
    val unlockedCount: Int = 0,
    val applyError: String? = null,
)

public fun PlacementUiState.currentQuestion(): PlacementQuestion? =
    exam
        ?.blocks
        ?.getOrNull(blockIndex)
        ?.questions
        ?.getOrNull(questionIndex)

public fun PlacementUiState.currentLevel(): CefrLevel? = exam?.blocks?.getOrNull(blockIndex)?.level

public fun PlacementUiState.progressInBlock(): Pair<Int, Int> {
    val block = exam?.blocks?.getOrNull(blockIndex) ?: return 0 to 0
    return (questionIndex + 1) to block.questions.size
}

public fun isUiSupported(exercise: Exercise): Boolean =
    when (exercise) {
        is Exercise.MultipleChoice,
        is Exercise.ListenChoose,
        is Exercise.AudioOnlyPrompt,
        is Exercise.KanaChoose,
        is Exercise.KanjiReading,
        is Exercise.ParticleFill,
        is Exercise.ListenType,
        is Exercise.Translate,
        is Exercise.OrderWords,
        is Exercise.MatchPairs,
        is Exercise.SemanticMatch,
        is Exercise.ClozeDragDrop,
        -> true
        else -> false
    }

private fun PlacementUiState.prepareForQuestion(
    exam: PlacementExam,
    blockIndex: Int,
    questionIndex: Int,
): PlacementUiState {
    val question =
        exam.blocks
            .getOrNull(blockIndex)
            ?.questions
            ?.getOrNull(questionIndex)
    val reset =
        copy(
            answered = false,
            lastCorrect = null,
            selectedOriginalIndex = null,
            selectedDisplayIndex = null,
            displayToOriginal = emptyList(),
            typedText = "",
            availableTokens = emptyList(),
            chosenTokens = emptyList(),
            shuffledRights = emptyList(),
            matched = emptyMap(),
            selectedLeft = null,
            clozeFilled = emptyMap(),
        )
    val ex = question?.exercise ?: return reset
    return when (ex) {
        is Exercise.MultipleChoice -> reset.withShuffledOptions(ex.options)
        is Exercise.ListenChoose -> reset.withShuffledOptions(ex.options)
        is Exercise.AudioOnlyPrompt -> reset.withShuffledOptions(ex.options)
        is Exercise.KanaChoose -> reset.withShuffledOptions(ex.romajiOptions)
        is Exercise.KanjiReading -> reset.withShuffledOptions(ex.readingOptions)
        is Exercise.ParticleFill -> reset.withShuffledOptions(ex.particles)
        is Exercise.Translate ->
            reset.copy(
                availableTokens = (ex.correctTokens + ex.distractorTokens).shuffled().mapIndexed { i, t -> PlacementToken(i, t) },
            )
        is Exercise.OrderWords ->
            reset.copy(
                availableTokens = ex.tokens.shuffled().mapIndexed { i, t -> PlacementToken(i, t) },
            )
        is Exercise.MatchPairs ->
            reset.copy(shuffledRights = List(ex.pairs.size) { it }.shuffled())
        is Exercise.SemanticMatch ->
            reset.copy(shuffledRights = List(ex.pairs.size) { it }.shuffled())
        is Exercise.ClozeDragDrop -> reset
        is Exercise.ListenType -> reset
        else -> reset
    }
}

private fun PlacementUiState.withShuffledOptions(options: List<String>): PlacementUiState {
    val order = options.indices.shuffled()
    return copy(displayToOriginal = order)
}

public fun Exercise.optionPrompt(): String? =
    when (this) {
        is Exercise.MultipleChoice -> question
        is Exercise.ListenChoose -> "Escucha y elige la opción correcta"
        is Exercise.AudioOnlyPrompt -> question ?: "Escucha y elige la opción correcta"
        is Exercise.KanaChoose -> kana
        is Exercise.KanjiReading -> kanji
        is Exercise.ParticleFill -> sentenceTemplate
        else -> null
    }

public fun Exercise.displayOptions(displayToOriginal: List<Int>): List<String> =
    when (this) {
        is Exercise.MultipleChoice -> displayToOriginal.map { options[it] }
        is Exercise.ListenChoose -> displayToOriginal.map { options[it] }
        is Exercise.AudioOnlyPrompt -> displayToOriginal.map { options[it] }
        is Exercise.KanaChoose -> displayToOriginal.map { romajiOptions[it] }
        is Exercise.KanjiReading -> displayToOriginal.map { readingOptions[it] }
        is Exercise.ParticleFill -> displayToOriginal.map { particles[it] }
        else -> emptyList()
    }

public fun Exercise.correctOriginalIndex(): Int =
    when (this) {
        is Exercise.MultipleChoice -> correctIndex
        is Exercise.ListenChoose -> correctIndex
        is Exercise.AudioOnlyPrompt -> correctIndex
        is Exercise.KanaChoose -> romajiOptions.indexOf(correctRomaji)
        is Exercise.KanjiReading -> readingOptions.indexOf(correctReading)
        is Exercise.ParticleFill -> particles.indexOf(correctParticle)
        else -> -1
    }

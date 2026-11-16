package dev.linguago.feature.placement

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.linguago.core.common.Result
import dev.linguago.core.domain.model.CefrLevel
import dev.linguago.core.domain.model.Course
import dev.linguago.core.domain.model.CourseId
import dev.linguago.core.domain.model.Exercise
import dev.linguago.core.domain.placement.PlacementEvaluator
import dev.linguago.core.domain.placement.PlacementExam
import dev.linguago.core.domain.placement.PlacementExamGenerator
import dev.linguago.core.domain.placement.PlacementQuestion
import dev.linguago.core.domain.placement.PlacementResult
import dev.linguago.core.domain.repository.ProgressRepository
import dev.linguago.core.domain.usecase.ExerciseAnswer
import dev.linguago.core.domain.usecase.LoadCourseUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    ) : ViewModel() {
        private val courseIdArg: String = savedStateHandle[PLACEMENT_COURSE_KEY] ?: "es-en"

        private val _uiState = MutableStateFlow(PlacementUiState(loading = true, courseId = courseIdArg))
        public val uiState: StateFlow<PlacementUiState> = _uiState.asStateFlow()

        private var course: Course? = null
        private var exam: PlacementExam? = null
        private val answers = mutableMapOf<Pair<Int, Int>, ExerciseAnswer>()

        init {
            viewModelScope.launch { load() }
        }

        private suspend fun load() {
            _uiState.update { it.copy(loading = true, error = null) }
            when (val result = loadCourseUseCase(CourseId(courseIdArg))) {
                is Result.Error -> _uiState.update { it.copy(loading = false, error = "No se pudo cargar el curso.") }
                is Result.Success -> {
                    course = result.value
                    val generated =
                        PlacementExamGenerator.generate(
                            course = result.value,
                            questionsPerLevel = QUESTIONS_PER_LEVEL,
                        )
                    val filtered = generated.filterToOptionTypes()
                    exam = filtered
                    answers.clear()
                    _uiState.update {
                        it.copy(
                            loading = false,
                            error = if (filtered.totalQuestions == 0) "No hay preguntas disponibles para este curso." else null,
                            exam = filtered,
                            blockIndex = 0,
                            questionIndex = 0,
                            selectedIndex = null,
                            answered = false,
                            lastCorrect = null,
                            finished = false,
                            result = null,
                            applied = false,
                            unlockedCount = 0,
                        )
                    }
                }
            }
        }

        public fun selectOption(index: Int) {
            val state = _uiState.value
            if (state.answered || state.finished || state.loading) return
            _uiState.update { it.copy(selectedIndex = index) }
        }

        public fun submitAnswer() {
            val state = _uiState.value
            val currentExam = exam ?: return
            if (state.answered || state.finished) return
            val selected = state.selectedIndex ?: return
            val question = currentQuestion(currentExam, state.blockIndex, state.questionIndex) ?: return
            val key = state.blockIndex to state.questionIndex
            val answer = ExerciseAnswer.OptionPicked(selected)
            answers[key] = answer
            val correct =
                dev.linguago.core.domain.logic.ExerciseGrader
                    .isCorrect(question.exercise, answer)
            _uiState.update { it.copy(answered = true, lastCorrect = correct) }
        }

        public fun next() {
            val state = _uiState.value
            val currentExam = exam ?: return
            val currentCourse = course ?: return
            if (!state.answered || state.finished) return

            val block = currentExam.blocks.getOrNull(state.blockIndex) ?: return
            val nextQuestion = state.questionIndex + 1
            if (nextQuestion < block.questions.size) {
                _uiState.update {
                    it.copy(
                        questionIndex = nextQuestion,
                        selectedIndex = null,
                        answered = false,
                        lastCorrect = null,
                    )
                }
                return
            }

            // fin de bloque: evaluar progreso acumulado y decidir si continuar
            val partial = PlacementEvaluator.evaluate(currentExam, answers.toMap(), currentCourse)
            val blockPassed = partial.levelResults.getOrNull(state.blockIndex)?.passed == true
            val nextBlock = state.blockIndex + 1
            if (blockPassed && nextBlock < currentExam.blocks.size) {
                _uiState.update {
                    it.copy(
                        blockIndex = nextBlock,
                        questionIndex = 0,
                        selectedIndex = null,
                        answered = false,
                        lastCorrect = null,
                    )
                }
            } else {
                finishExam()
            }
        }

        private fun finishExam() {
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

        private fun currentQuestion(
            exam: PlacementExam,
            blockIndex: Int,
            questionIndex: Int,
        ): PlacementQuestion? =
            exam.blocks
                .getOrNull(blockIndex)
                ?.questions
                ?.getOrNull(questionIndex)

        private companion object {
            const val QUESTIONS_PER_LEVEL = 4
        }
    }

public data class PlacementUiState(
    val loading: Boolean = false,
    val courseId: String = "",
    val error: String? = null,
    val exam: PlacementExam? = null,
    val blockIndex: Int = 0,
    val questionIndex: Int = 0,
    val selectedIndex: Int? = null,
    val answered: Boolean = false,
    val lastCorrect: Boolean? = null,
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

/** Restringe a ejercicios de selección de opción para una UI simple y robusta. */
private fun PlacementExam.filterToOptionTypes(): PlacementExam {
    val blocks =
        blocks.mapNotNull { block ->
            val qs =
                block.questions.filter { q ->
                    when (q.exercise) {
                        is Exercise.MultipleChoice,
                        is Exercise.KanaChoose,
                        is Exercise.KanjiReading,
                        is Exercise.ParticleFill,
                        -> true
                        else -> false
                    }
                }
            if (qs.isEmpty()) null else block.copy(questions = qs)
        }
    return copy(blocks = blocks)
}

public fun Exercise.optionPromptAndChoices(): Pair<String, List<String>>? =
    when (this) {
        is Exercise.MultipleChoice -> question to options
        is Exercise.KanaChoose -> kana to romajiOptions
        is Exercise.KanjiReading -> kanji to readingOptions
        is Exercise.ParticleFill -> sentenceTemplate to particles
        else -> null
    }

public fun Exercise.optionCorrectIndex(): Int =
    when (this) {
        is Exercise.MultipleChoice -> correctIndex
        is Exercise.KanaChoose -> romajiOptions.indexOf(correctRomaji)
        is Exercise.KanjiReading -> readingOptions.indexOf(correctReading)
        is Exercise.ParticleFill -> particles.indexOf(correctParticle)
        else -> -1
    }

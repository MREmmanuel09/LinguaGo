package dev.linguago.feature.lesson

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.linguago.core.common.Result
import dev.linguago.core.domain.model.CourseId
import dev.linguago.core.domain.model.Exercise
import dev.linguago.core.domain.model.Lesson
import dev.linguago.core.domain.model.LessonId
import dev.linguago.core.domain.model.XP_PER_CORRECT_ANSWER
import dev.linguago.core.domain.usecase.CompleteLessonUseCase
import dev.linguago.core.domain.usecase.ExerciseAnswer
import dev.linguago.core.domain.usecase.StartLessonUseCase
import dev.linguago.core.domain.usecase.SubmitAnswerUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Feedback de la respuesta evaluada, visible hasta que el usuario avanza. */
data class ExerciseFeedback(
    val isCorrect: Boolean,
)

/**
 * Estado del motor de lección (ADR-002: FSM). `exercises` nunca cambia
 * durante la sesión; lo mutable es el índice actual, los paneles de
 * respuesta (tokens/parejas) y el feedback (I3: sin vidas ni penalización).
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
    // Tablero de tokens (Translate / OrderWords)
    val availableTokens: List<String> = emptyList(),
    val chosenTokens: List<String> = emptyList(),
    // Parejas (MatchPairs): orden de dibujo de las derechas + estado de asocio
    val shuffledRights: List<Int> = emptyList(),
    val selectedLeft: Int? = null,
    val matched: Map<Int, Int> = emptyMap(),
)

/**
 * Motor de aprendizaje (RFC §3.2). Cada respuesta pasa por el
 * SubmitAnswerUseCase (grader puro + XP +10 por acierto) y al final la
 * lección se persiste con CompleteLessonUseCase (racha y XP total).
 */
@HiltViewModel
class LessonViewModel
    @Inject
    constructor(
        savedStateHandle: SavedStateHandle,
        private val startLessonUseCase: StartLessonUseCase,
        private val submitAnswerUseCase: SubmitAnswerUseCase,
        private val completeLessonUseCase: CompleteLessonUseCase,
    ) : ViewModel() {
        private val courseId: String =
            checkNotNull(savedStateHandle[LESSON_COURSE_KEY])
        private val lessonId: String =
            checkNotNull(savedStateHandle[LESSON_ID_KEY])

        private val _uiState = MutableStateFlow(LessonUiState())
        val uiState: StateFlow<LessonUiState> = _uiState.asStateFlow()

        init {
            viewModelScope.launch {
                when (val result = startLessonUseCase(CourseId(courseId), LessonId(lessonId))) {
                    is Result.Success -> startLesson(result.value)
                    is Result.Error -> _uiState.update { it.copy(loading = false, invalid = true) }
                }
            }
        }

        /** El usuario elige una opción de lista (MC / ListenChoose / Kana / Kanji / Partícula). */
        fun pickOption(index: Int) = submitAnswer(ExerciseAnswer.OptionPicked(index))

        /** El usuario verifica el tablero de tokens (Translate / OrderWords). */
        fun submitTokens() {
            val tokens = _uiState.value.chosenTokens
            if (tokens.isNotEmpty()) submitAnswer(ExerciseAnswer.TokenSequence(tokens))
        }

        /** El usuario envía el dictado libre (ListenType). */
        fun submitTypedText(text: String) = submitAnswer(ExerciseAnswer.TypedText(text))

        /** El usuario verifica todas las parejas establecidas (MatchPairs). */
        fun submitMatchedPairs() {
            val current = _uiState.value.exercises.getOrNull(_uiState.value.index)
            val total = (current as? Exercise.MatchPairs)?.pairs?.size ?: 0
            if (_uiState.value.matched.size == total && total > 0) {
                submitAnswer(ExerciseAnswer.MatchedPairs(_uiState.value.matched))
            }
        }

        /** Toca una izquierda (canónica) para elegir el origen de la pareja. */
        fun selectLeft(canonicalLeft: Int) {
            _uiState.update { it.copy(selectedLeft = canonicalLeft) }
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
            if (state.feedback == null) return
            val nextIndex = state.index + 1
            if (nextIndex < state.exercises.size) {
                _uiState.update { it.copy(index = nextIndex, feedback = null) }
                prepareExercise(nextIndex)
            } else {
                finishLesson()
            }
        }

        private fun startLesson(lesson: Lesson) {
            _uiState.update {
                it.copy(
                    loading = false,
                    lessonTitle = lesson.title,
                    exercises = lesson.exercises,
                    index = 0,
                )
            }
            prepareExercise(0)
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
                            selectedLeft = null,
                        )
                    }
                is Exercise.MatchPairs ->
                    _uiState.update {
                        it.copy(
                            availableTokens = emptyList(),
                            chosenTokens = emptyList(),
                            shuffledRights = exercise.pairs.indices.shuffled(),
                            matched = emptyMap(),
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
                            selectedLeft = null,
                        )
                    }
            }
        }

        private fun submitAnswer(answer: ExerciseAnswer) {
            val exercise = _uiState.value.exercises.getOrNull(_uiState.value.index) ?: return
            viewModelScope.launch {
                when (val result = submitAnswerUseCase(exercise, answer)) {
                    is Result.Success -> {
                        val correct = result.value
                        _uiState.update { state ->
                            state.copy(
                                feedback = ExerciseFeedback(isCorrect = correct),
                                xpEarned = if (correct) state.xpEarned + XP_PER_CORRECT_ANSWER else state.xpEarned,
                            )
                        }
                    }
                    is Result.Error -> _uiState.update { it.copy(invalid = true, loading = false) }
                }
            }
        }

        private fun finishLesson() {
            val earned = _uiState.value.xpEarned
            viewModelScope.launch {
                completeLessonUseCase(LessonId(lessonId), earned)
                _uiState.update { it.copy(finished = true, feedback = null) }
            }
        }

        companion object {
            const val LESSON_COURSE_KEY = "courseId"
            const val LESSON_ID_KEY = "lessonId"
        }
    }

package dev.linguago.feature.review

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.linguago.core.common.Result
import dev.linguago.core.domain.hearts.HEARTS_MAX
import dev.linguago.core.domain.logic.ReviewRating
import dev.linguago.core.domain.model.CourseId
import dev.linguago.core.domain.model.WordProgress
import dev.linguago.core.domain.repository.ReviewRepository
import dev.linguago.core.domain.usecase.ConsumeHeartUseCase
import dev.linguago.core.domain.usecase.ObserveHeartsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Estado del flujo de repaso espaciado (ADR-016/ADR-025). */
public data class ReviewUiState(
    public val loading: Boolean = true,
    public val empty: Boolean = false,
    public val finished: Boolean = false,
    public val total: Int = 0,
    public val index: Int = 0,
    public val current: WordProgress? = null,
    public val revealed: Boolean = false,
    public val message: String? = null,
    /** Vidas vigentes; `Otra vez` consume una (ADR-025). */
    public val hearts: Int = HEARTS_MAX,
)

/**
 * ViewModel del repaso (FSRS): carga las tarjetas vencidas del curso activo
 * y aplica la calificación del usuario. `Otra vez` consume una vida, pero el
 * repaso nunca se bloquea: a 0 vidas la sesión continúa (ADR-025, I3).
 */
@HiltViewModel
public class ReviewViewModel
    @Inject
    constructor(
        savedStateHandle: SavedStateHandle,
        private val reviewRepository: ReviewRepository,
        private val observeHeartsUseCase: ObserveHeartsUseCase,
        private val consumeHeartUseCase: ConsumeHeartUseCase,
    ) : ViewModel() {
        private val courseId: CourseId =
            CourseId(checkNotNull(savedStateHandle[REVIEW_COURSE_KEY]))

        private var queue: List<WordProgress> = emptyList()

        private val _uiState = MutableStateFlow(ReviewUiState())
        public val uiState: StateFlow<ReviewUiState> = _uiState.asStateFlow()

        init {
            viewModelScope.launch {
                observeHeartsUseCase().collect { hearts ->
                    _uiState.update { it.copy(hearts = hearts.hearts) }
                }
            }
            load()
        }

        /** Muestra la traducción de la tarjeta actual. */
        public fun reveal() {
            _uiState.update { current -> current.copy(revealed = true) }
        }

        /** Califica la tarjeta actual y avanza a la siguiente. */
        public fun rate(rating: ReviewRating) {
            val state = _uiState.value
            val current = state.current ?: return
            if (!state.revealed) return
            _uiState.update { it.copy(message = null) }
            viewModelScope.launch {
                when (reviewRepository.grade(courseId, current.word, rating)) {
                    is Result.Error ->
                        _uiState.update {
                            it.copy(message = "No se pudo guardar el repaso.")
                        }
                    is Result.Success -> {
                        if (rating == ReviewRating.AGAIN) {
                            // Penalización no bloqueante: si falla la escritura
                            // de vidas, el repaso continúa igual (I3).
                            consumeHeartUseCase()
                        }
                        advance()
                    }
                }
            }
        }

        private fun load() {
            viewModelScope.launch {
                when (val result = reviewRepository.due(courseId, QUEUE_LIMIT)) {
                    is Result.Error ->
                        _uiState.update {
                            it.copy(loading = false, message = "No se pudo cargar el repaso.")
                        }
                    is Result.Success -> {
                        queue = result.value
                        _uiState.update { current ->
                            when (queue.isEmpty()) {
                                true -> current.copy(loading = false, empty = true)
                                false ->
                                    current.copy(
                                        loading = false,
                                        total = queue.size,
                                        index = 0,
                                        current = queue.first(),
                                    )
                            }
                        }
                    }
                }
            }
        }

        private fun advance() {
            val next = _uiState.value.index + 1
            _uiState.update { current ->
                when {
                    next < queue.size ->
                        current.copy(
                            index = next,
                            current = queue[next],
                            revealed = false,
                        )
                    else -> current.copy(finished = true, current = null, revealed = false)
                }
            }
        }

        public companion object {
            public const val REVIEW_COURSE_KEY: String = "courseId"
            private const val QUEUE_LIMIT = 20
        }
    }

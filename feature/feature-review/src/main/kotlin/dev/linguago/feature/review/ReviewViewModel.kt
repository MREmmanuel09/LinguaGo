package dev.linguago.feature.review

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.linguago.core.audio.TtsEngine
import dev.linguago.core.common.Result
import dev.linguago.core.domain.hearts.HEARTS_MAX
import dev.linguago.core.domain.logic.ReviewRating
import dev.linguago.core.domain.logic.VocabularyExtractor
import dev.linguago.core.domain.model.CourseId
import dev.linguago.core.domain.model.Language
import dev.linguago.core.domain.model.WordProgress
import dev.linguago.core.domain.repository.CourseRepository
import dev.linguago.core.domain.repository.ReviewRepository
import dev.linguago.core.domain.repository.SettingsRepository
import dev.linguago.core.domain.usecase.ConsumeHeartUseCase
import dev.linguago.core.domain.usecase.ObserveHeartsUseCase
import dev.linguago.core.domain.usecase.RecoverHeartUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
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
    /** `true` mientras se reproduce la pronunciación (ADR-034). */
    public val speaking: Boolean = false,
    /** `true` si hay voz lista para el idioma meta (ADR-034). */
    public val speechAvailable: Boolean = false,
    /** Tarjetas superadas desde la última vida recuperada, 0..4 (ADR-048). */
    public val heartProgress: Int = 0,
    /** Nota cultural del modismo de la tarjeta, si existe (ADR-048). */
    public val culturalNote: String? = null,
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
        private val recoverHeartUseCase: RecoverHeartUseCase,
        private val courseRepository: CourseRepository,
        private val settingsRepository: SettingsRepository,
        private val ttsEngine: TtsEngine,
    ) : ViewModel() {
        private val argumentCourseId: CourseId? =
            savedStateHandle.get<String>(REVIEW_COURSE_KEY)?.let(::CourseId)

        private var activeCourseId: CourseId? = argumentCourseId
        private var queue: List<WordProgress> = emptyList()
        private var targetLanguage: Language? = null
        private var reviewedSinceLastHeart = 0
        private var culturalNotes: Map<String, String> = emptyMap()

        private val _uiState = MutableStateFlow(ReviewUiState())
        public val uiState: StateFlow<ReviewUiState> = _uiState.asStateFlow()

        init {
            viewModelScope.launch {
                observeHeartsUseCase().collect { hearts ->
                    _uiState.update { it.copy(hearts = hearts.hearts) }
                }
            }
        }

        /**
         * Recarga la cola del curso activo (ADR-048). La pestaña Repaso puede
         * restaurarse desde el back stack: sin esto mostraría la cola vacía
         * anterior aunque Home ya tenga tarjetas vencidas.
         */
        public fun refresh() {
            viewModelScope.launch {
                val resolved =
                    argumentCourseId
                        ?: settingsRepository.selectedCourseId.first()?.let(::CourseId)
                if (resolved == null) {
                    _uiState.update { it.copy(loading = false, empty = true) }
                    return@launch
                }
                // No se recarga al rotar si ya hay una tarjeta activa: la
                // pestaña restaurada solo refresca cuando está vacía/fin.
                if (resolved == activeCourseId && _uiState.value.current != null) return@launch
                if (resolved != activeCourseId) {
                    activeCourseId = resolved
                    queue = emptyList()
                    reviewedSinceLastHeart = 0
                    _uiState.value = ReviewUiState()
                }
                loadCourse(resolved)
                load(resolved)
            }
        }

        private suspend fun loadCourse(courseId: CourseId) {
            when (val result = courseRepository.loadCourse(courseId)) {
                is Result.Success -> {
                    val language = result.value.toLanguage
                    targetLanguage = language
                    culturalNotes =
                        result.value.levels
                            .flatMap { level -> level.units }
                            .flatMap { unit -> unit.lessons }
                            .flatMap { lesson -> VocabularyExtractor.fromLesson(lesson, language) }
                            .mapNotNull { pair ->
                                pair.culturalNote?.let { note -> pair.source.lowercase() to note }
                            }.toMap()
                    ttsEngine.warmUp(language)
                    _uiState.update { it.copy(speechAvailable = ttsEngine.isReadyFor(language)) }
                }
                is Result.Error -> Unit
            }
        }

        /** Muestra la traducción de la tarjeta actual. */
        public fun reveal() {
            _uiState.update { current -> current.copy(revealed = true) }
        }

        /** Reproduce la pronunciación de la traducción (ADR-034). */
        public fun speakTranslation(slow: Boolean = false) {
            val state = _uiState.value
            val card = state.current ?: return
            val language = targetLanguage ?: return
            if (state.speaking) return
            // En japonés la tarjeta puede ser kana/kanji (frente) o una pareja
            // normalizada español→japonés (reverso); se pronuncia siempre el
            // lado que está en escritura japonesa (ADR-045).
            val spoken =
                if (language == Language.JAPANESE && JAPANESE_SCRIPT.containsMatchIn(card.word)) {
                    card.word
                } else {
                    card.translation
                }
            viewModelScope.launch {
                _uiState.update { it.copy(speaking = true) }
                try {
                    ttsEngine.speak(spoken, language, speed = if (slow) SLOW_SPEED else NORMAL_SPEED)
                } finally {
                    _uiState.update { it.copy(speaking = false) }
                }
            }
        }

        /** Califica la tarjeta actual y avanza a la siguiente. */
        public fun rate(rating: ReviewRating) {
            val state = _uiState.value
            val current = state.current ?: return
            val courseId = activeCourseId ?: return
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
                        } else {
                            val next = ReviewHeartRules.afterPassed(reviewedSinceLastHeart)
                            reviewedSinceLastHeart = next.progress
                            if (next.recovered) {
                                recoverHeartUseCase()
                            }
                            _uiState.update {
                                it.copy(heartProgress = reviewedSinceLastHeart)
                            }
                        }
                        advance()
                    }
                }
            }
        }

        private fun load(courseId: CourseId) {
            viewModelScope.launch {
                _uiState.update {
                    it.copy(
                        loading = true,
                        empty = false,
                        finished = false,
                        current = null,
                        revealed = false,
                        culturalNote = null,
                    )
                }
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
                                        culturalNote =
                                            queue.first().culturalNote
                                                ?: culturalNotes[queue.first().word.lowercase()],
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
                            culturalNote =
                                queue[next].culturalNote
                                    ?: culturalNotes[queue[next].word.lowercase()],
                        )
                    else -> current.copy(finished = true, current = null, revealed = false, culturalNote = null)
                }
            }
        }

        public companion object {
            public const val REVIEW_COURSE_KEY: String = "courseId"
            private const val QUEUE_LIMIT = 20
            private const val NORMAL_SPEED = 1.0f
            private const val SLOW_SPEED = 0.75f
            private val JAPANESE_SCRIPT = Regex("[\\u3040-\\u30FF\\u3400-\\u4DBF\\u4E00-\\u9FFF]")
        }
    }

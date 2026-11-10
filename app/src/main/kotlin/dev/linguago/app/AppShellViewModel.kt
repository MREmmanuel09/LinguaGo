package dev.linguago.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.linguago.core.domain.model.CourseId
import dev.linguago.core.domain.repository.ReviewRepository
import dev.linguago.core.domain.repository.SettingsRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * Estado del shell de navegación (ADR-048): alimenta el badge de repaso de la
 * barra inferior con las tarjetas vencidas del curso activo.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class AppShellViewModel
    @Inject
    constructor(
        settingsRepository: SettingsRepository,
        reviewRepository: ReviewRepository,
    ) : ViewModel() {
        val dueReviews: StateFlow<Int> =
            settingsRepository.selectedCourseId
                .flatMapLatest { courseId ->
                    if (courseId == null) {
                        flowOf(0)
                    } else {
                        reviewRepository.observeDueCount(CourseId(courseId))
                    }
                }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)
    }

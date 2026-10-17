package dev.linguago.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.linguago.core.domain.repository.ProgressRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/** Estado del perfil: racha, XP total y lecciones completadas. */
data class ProfileUiState(
    val streakDays: Int = 0,
    val totalXp: Int = 0,
    val lessonsCompleted: Int = 0,
    val longestStreak: Int = 0,
)

/**
 * Perfil observado (RFC §2.2): vive sobre ProgressRepository (Room),
 * por lo que se actualiza solo al completar lecciones o sumar XP.
 */
@HiltViewModel
class ProfileViewModel
    @Inject
    constructor(
        progressRepository: ProgressRepository,
    ) : ViewModel() {
        val uiState: StateFlow<ProfileUiState> =
            combine(
                progressRepository.observeStreak(),
                progressRepository.observeLessonProgress(),
            ) { streak, lessonProgress ->
                ProfileUiState(
                    streakDays = streak.current,
                    longestStreak = streak.longest,
                    totalXp = streak.totalXp,
                    lessonsCompleted = lessonProgress.count { progress -> progress.completed },
                )
            }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ProfileUiState())
    }

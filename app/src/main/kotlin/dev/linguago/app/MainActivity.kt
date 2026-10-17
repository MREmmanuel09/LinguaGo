package dev.linguago.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import dagger.hilt.android.AndroidEntryPoint
import dev.linguago.core.domain.model.ThemeMode
import dev.linguago.core.domain.repository.SettingsRepository
import dev.linguago.core.ui.theme.LinguaTheme
import javax.inject.Inject

/**
 * Única Activity del sistema: toda la navegación es Compose (RFC §2.1).
 * El tema (SYSTEM/LIGHT/DARK) y el destino inicial (onboarding/home) se
 * observan desde SettingsRepository (Fase 1).
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var settingsRepository: SettingsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            val themeMode by settingsRepository.themeMode.collectAsState(initial = ThemeMode.SYSTEM)
            val selectedCourseId by settingsRepository.selectedCourseId.collectAsState(initial = null)

            LinguaTheme(themeMode = themeMode) {
                LinguaApp(
                    startDestination =
                        if (selectedCourseId == null) {
                            Routes.ONBOARDING
                        } else {
                            Routes.HOME
                        },
                )
            }
        }
    }
}

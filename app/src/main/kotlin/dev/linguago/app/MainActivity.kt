package dev.linguago.app

import android.os.Bundle
import android.os.StrictMode
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import dagger.hilt.android.AndroidEntryPoint
import dev.linguago.core.domain.model.ThemeMode
import dev.linguago.core.domain.repository.SettingsRepository
import dev.linguago.core.ui.feedback.FeedbackSettings
import dev.linguago.core.ui.feedback.LocalFeedbackSettings
import dev.linguago.core.ui.theme.LinguaTheme
import javax.inject.Inject

/**
 * Única Activity del sistema: toda la navegación es Compose (RFC §2.1).
 * El tema (SYSTEM/LIGHT/DARK) y el destino inicial (onboarding/home) se
 * observan desde SettingsRepository (Fase 1). En debug activa StrictMode
 * (detectDiskReads + penaltyLog) para vigilar el hilo principal (ADR-020).
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var settingsRepository: SettingsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(
                StrictMode.ThreadPolicy
                    .Builder()
                    .detectDiskReads()
                    .penaltyLog()
                    .build(),
            )
        }
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            val themeMode by settingsRepository.themeMode.collectAsState(initial = ThemeMode.SYSTEM)
            val selectedCourseId by settingsRepository.selectedCourseId.collectAsState(initial = null)
            val soundEffectsEnabled by settingsRepository.soundEffectsEnabled.collectAsState(initial = true)
            val hapticsEnabled by settingsRepository.hapticsEnabled.collectAsState(initial = true)

            CompositionLocalProvider(
                LocalFeedbackSettings provides
                    FeedbackSettings(
                        soundEffectsEnabled = soundEffectsEnabled,
                        hapticsEnabled = hapticsEnabled,
                    ),
            ) {
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
}

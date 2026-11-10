package dev.linguago.app

import android.os.Bundle
import android.os.StrictMode
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.view.WindowCompat
import dagger.hilt.android.AndroidEntryPoint
import dev.linguago.core.domain.model.ThemeMode
import dev.linguago.core.domain.repository.SettingsRepository
import dev.linguago.core.ui.feedback.FeedbackSettings
import dev.linguago.core.ui.feedback.LocalFeedbackSettings
import dev.linguago.core.ui.theme.LinguaTheme
import dev.linguago.core.ui.theme.LinguaTokens
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

/** Fotografía de preferencias con la que se compone el primer frame. */
private data class AppSettings(
    val themeMode: ThemeMode,
    val selectedCourseId: String?,
    val soundEffectsEnabled: Boolean,
    val hapticsEnabled: Boolean,
)

/**
 * Única Activity del sistema: toda la navegación es Compose (RFC §2.1).
 * El tema (SYSTEM/LIGHT/DARK) y el destino inicial (onboarding/home) se
 * observan desde SettingsRepository (Fase 1). La composición espera la
 * primera lectura completa (B-07): así el `NavHost` no se reinicia con el
 * valor por defecto al rotar dentro de una lección. En debug activa
 * StrictMode (detectDiskReads + penaltyLog) para vigilar el hilo principal
 * (ADR-020).
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
            val settingsFlow =
                remember(settingsRepository) {
                    combine(
                        settingsRepository.themeMode,
                        settingsRepository.selectedCourseId,
                        settingsRepository.soundEffectsEnabled,
                        settingsRepository.hapticsEnabled,
                    ) { themeMode, selectedCourseId, soundEffectsEnabled, hapticsEnabled ->
                        AppSettings(themeMode, selectedCourseId, soundEffectsEnabled, hapticsEnabled)
                    }
                }
            val settings by settingsFlow.collectAsState(initial = null)
            val loaded = settings

            // Sin la primera lectura no se monta el NavHost: evitamos el
            // flash de Onboarding y el reinicio de la pila al rotar (B-07).
            if (loaded == null) {
                Box(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .background(
                                if (isSystemInDarkTheme()) {
                                    Color(LinguaTokens.DARK_BG_HEX)
                                } else {
                                    LinguaTokens.Bg
                                },
                            ),
                )
                return@setContent
            }
            val dark =
                when (loaded.themeMode) {
                    ThemeMode.SYSTEM -> isSystemInDarkTheme()
                    ThemeMode.LIGHT -> false
                    ThemeMode.DARK -> true
                }
            SideEffect {
                WindowCompat
                    .getInsetsController(this@MainActivity.window, this@MainActivity.window.decorView)
                    .apply {
                        isAppearanceLightStatusBars = !dark
                        isAppearanceLightNavigationBars = !dark
                    }
            }

            // Se fija el destino inicial tras la primera lectura; los cambios
            // posteriores de curso no reconstruyen el grafo de navegación.
            val startDestination =
                rememberSaveable {
                    if (loaded.selectedCourseId == null) Routes.ONBOARDING else Routes.HOME
                }

            CompositionLocalProvider(
                LocalFeedbackSettings provides
                    FeedbackSettings(
                        soundEffectsEnabled = loaded.soundEffectsEnabled,
                        hapticsEnabled = loaded.hapticsEnabled,
                    ),
            ) {
                LinguaTheme(themeMode = loaded.themeMode) {
                    LinguaApp(startDestination = startDestination)
                }
            }
        }
    }
}

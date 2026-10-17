package dev.linguago.core.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import dev.linguago.core.domain.model.ThemeMode

/**
 * Tema LinguaGo: identidad original (RFC §7.1) sobre Material 3. Respeta el
 * `themeMode` del usuario (Fase 1) y mantiene contraste y tamaños dinámicos
 * (I8).
 */
@Composable
fun LinguaTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit,
) {
    val darkTheme =
        when (themeMode) {
            ThemeMode.SYSTEM -> isSystemInDarkTheme()
            ThemeMode.LIGHT -> false
            ThemeMode.DARK -> true
        }
    val colors = if (darkTheme) LinguaDarkColors else LinguaLightColors
    MaterialTheme(
        colorScheme = colors,
        typography = LinguaTypography,
        content = content,
    )
}

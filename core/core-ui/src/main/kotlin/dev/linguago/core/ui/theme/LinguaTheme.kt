package dev.linguago.core.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

/**
 * Tema LinguaGo: identidad original (RFC §7.1) sobre Material 3. Respeta el
 * modo del sistema y mantiene contraste y tamaños dinámicos (I8).
 */
@Composable
fun LinguaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colors = if (darkTheme) LinguaDarkColors else LinguaLightColors
    MaterialTheme(
        colorScheme = colors,
        typography = LinguaTypography,
        content = content,
    )
}

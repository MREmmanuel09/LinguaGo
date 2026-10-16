package dev.linguago.core.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/**
 * Voces de color Material 3 derivadas de los tokens LinguaGo (RFC §7.1) para
 * modo claro y oscuro. Los roles se mapean manteniendo la identidad original.
 */
val LinguaLightColors =
    lightColorScheme(
        primary = LinguaTokens.Primary,
        onPrimary = LinguaTokens.Bg,
        primaryContainer = LinguaTokens.PrimaryEdge,
        onPrimaryContainer = LinguaTokens.Bg,
        secondary = LinguaTokens.Accent,
        onSecondary = LinguaTokens.Bg,
        tertiary = LinguaTokens.Info,
        onTertiary = LinguaTokens.Bg,
        error = LinguaTokens.Destructive,
        onError = LinguaTokens.Bg,
        background = LinguaTokens.Bg,
        onBackground = LinguaTokens.Fg,
        surface = LinguaTokens.Bg,
        onSurface = LinguaTokens.Fg,
        surfaceVariant = LinguaTokens.BgSecondary,
        onSurfaceVariant = LinguaTokens.FgMuted,
        outline = LinguaTokens.Border,
    )

val LinguaDarkColors =
    darkColorScheme(
        primary = LinguaTokens.Primary,
        onPrimary = LinguaTokens.Bg,
        primaryContainer = LinguaTokens.PrimaryEdge,
        onPrimaryContainer = LinguaTokens.Bg,
        secondary = LinguaTokens.Accent,
        onSecondary = LinguaTokens.Bg,
        tertiary = LinguaTokens.Info,
        onTertiary = LinguaTokens.Bg,
        error = LinguaTokens.Destructive,
        onError = LinguaTokens.Bg,
        background = Color(LinguaTokens.DARK_BG_HEX),
        onBackground = Color(LinguaTokens.DARK_FG_HEX),
        surface = Color(LinguaTokens.DARK_BG_HEX),
        onSurface = Color(LinguaTokens.DARK_FG_HEX),
        surfaceVariant = Color(LinguaTokens.DARK_BG_SECONDARY_HEX),
        onSurfaceVariant = Color(LinguaTokens.DARK_FG_MUTED_HEX),
        outline = Color(LinguaTokens.DARK_BORDER_HEX),
    )

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
        secondaryContainer = Color(LinguaTokens.SECONDARY_CONTAINER_HEX),
        onSecondaryContainer = Color(LinguaTokens.ON_SECONDARY_CONTAINER_HEX),
        tertiary = LinguaTokens.Info,
        onTertiary = LinguaTokens.Bg,
        tertiaryContainer = Color(LinguaTokens.TERTIARY_CONTAINER_HEX),
        onTertiaryContainer = Color(LinguaTokens.ON_TERTIARY_CONTAINER_HEX),
        error = LinguaTokens.Destructive,
        onError = LinguaTokens.Bg,
        errorContainer = Color(LinguaTokens.ERROR_CONTAINER_HEX),
        onErrorContainer = Color(LinguaTokens.ON_ERROR_CONTAINER_HEX),
        background = LinguaTokens.Bg,
        onBackground = LinguaTokens.Fg,
        surface = LinguaTokens.Bg,
        onSurface = LinguaTokens.Fg,
        surfaceVariant = LinguaTokens.BgSecondary,
        onSurfaceVariant = LinguaTokens.FgMuted,
        outline = LinguaTokens.Border,
        outlineVariant = LinguaTokens.Border,
    )

val LinguaDarkColors =
    darkColorScheme(
        primary = LinguaTokens.Primary,
        onPrimary = LinguaTokens.Bg,
        primaryContainer = LinguaTokens.PrimaryEdge,
        onPrimaryContainer = LinguaTokens.Bg,
        secondary = LinguaTokens.Accent,
        onSecondary = LinguaTokens.Bg,
        secondaryContainer = Color(LinguaTokens.DARK_SECONDARY_CONTAINER_HEX),
        onSecondaryContainer = Color(LinguaTokens.DARK_ON_SECONDARY_CONTAINER_HEX),
        tertiary = LinguaTokens.Info,
        onTertiary = LinguaTokens.Bg,
        tertiaryContainer = Color(LinguaTokens.DARK_TERTIARY_CONTAINER_HEX),
        onTertiaryContainer = Color(LinguaTokens.DARK_ON_TERTIARY_CONTAINER_HEX),
        error = LinguaTokens.Destructive,
        onError = LinguaTokens.Bg,
        errorContainer = Color(LinguaTokens.DARK_ERROR_CONTAINER_HEX),
        onErrorContainer = Color(LinguaTokens.DARK_ON_ERROR_CONTAINER_HEX),
        background = Color(LinguaTokens.DARK_BG_HEX),
        onBackground = Color(LinguaTokens.DARK_FG_HEX),
        surface = Color(LinguaTokens.DARK_BG_HEX),
        onSurface = Color(LinguaTokens.DARK_FG_HEX),
        surfaceVariant = Color(LinguaTokens.DARK_BG_SECONDARY_HEX),
        onSurfaceVariant = Color(LinguaTokens.DARK_FG_MUTED_HEX),
        outline = Color(LinguaTokens.DARK_BORDER_HEX),
        outlineVariant = Color(LinguaTokens.DARK_BORDER_HEX),
    )

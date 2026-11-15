package dev.linguago.core.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/**
 * Voces de color Material 3 derivadas de los tokens LinguaGo (ADR-055,
 * prototipo «Warm Cream & Teal») para modo claro y oscuro.
 */
val LinguaLightColors =
    lightColorScheme(
        primary = LinguaTokens.Primary,
        onPrimary = LinguaTokens.Bg,
        primaryContainer = LinguaTokens.PrimaryEdge,
        onPrimaryContainer = LinguaTokens.Bg,
        secondary = LinguaTokens.Accent,
        onSecondary = LinguaTokens.AccentInk,
        secondaryContainer = Color(LinguaTokens.SECONDARY_CONTAINER_HEX),
        onSecondaryContainer = Color(LinguaTokens.ON_SECONDARY_CONTAINER_HEX),
        tertiary = LinguaTokens.Info,
        onTertiary = LinguaTokens.OnInfoFill,
        tertiaryContainer = Color(LinguaTokens.TERTIARY_CONTAINER_HEX),
        onTertiaryContainer = Color(LinguaTokens.ON_TERTIARY_CONTAINER_HEX),
        error = LinguaTokens.Destructive,
        onError = LinguaTokens.DestructiveInk,
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
        primary = Color(LinguaTokens.DARK_PRIMARY_HEX),
        onPrimary = Color(LinguaTokens.DARK_BG_HEX),
        primaryContainer = Color(LinguaTokens.DARK_PRIMARY_EDGE_HEX),
        onPrimaryContainer = Color(LinguaTokens.DARK_PRIMARY_INK_HEX),
        secondary = LinguaTokens.Accent,
        onSecondary = LinguaTokens.AccentInk,
        secondaryContainer = Color(LinguaTokens.DARK_SECONDARY_CONTAINER_HEX),
        onSecondaryContainer = Color(LinguaTokens.DARK_ON_SECONDARY_CONTAINER_HEX),
        tertiary = Color(LinguaTokens.DARK_INFO_HEX),
        onTertiary = Color(LinguaTokens.DARK_ON_INFO_FILL_HEX),
        tertiaryContainer = Color(LinguaTokens.DARK_TERTIARY_CONTAINER_HEX),
        onTertiaryContainer = Color(LinguaTokens.DARK_ON_TERTIARY_CONTAINER_HEX),
        error = LinguaTokens.Destructive,
        onError = Color(LinguaTokens.DARK_BG_HEX),
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

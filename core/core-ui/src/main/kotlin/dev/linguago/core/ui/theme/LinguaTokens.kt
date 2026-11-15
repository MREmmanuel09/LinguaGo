package dev.linguago.core.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Tokens de identidad visual LinguaGo — valores canónicos del prototipo
 * «Warm Cream & Teal» (ADR-055). Cualquier cambio aquí es un cambio de
 * identidad y requiere ADR.
 */
object LinguaTokens {
    // Hex canónicos (ADR-055) — exponerlos como Longs permite testear la
    // identidad sin requerir Android en tests unitarios.
    const val PRIMARY_HEX = 0xFF1F6E64
    const val PRIMARY_EDGE_HEX = 0xFF0E4B44
    const val ACCENT_HEX = 0xFFA3C4A8
    const val DESTRUCTIVE_HEX = 0xFFE2725B
    const val WARNING_HEX = 0xFFE2A83D
    const val INFO_HEX = 0xFF5D6B7A
    const val BG_HEX = 0xFFF6F2E9
    const val BG_SECONDARY_HEX = 0xFFFCF8EF
    const val FG_HEX = 0xFF2F2C27
    const val FG_MUTED_HEX = 0xFF71695C
    const val BORDER_HEX = 0xFFE2D8C3

    // Modo oscuro derivado de la paleta cálida (ADR-055; R7 afina detalles).
    const val DARK_BG_HEX = 0xFF14120E
    const val DARK_BG_SECONDARY_HEX = 0xFF1D1A12
    const val DARK_FG_HEX = 0xFFF2EDE2
    const val DARK_FG_MUTED_HEX = 0xFFB7AE9C
    const val DARK_BORDER_HEX = 0xFF332E24
    const val DARK_PRIMARY_HEX = 0xFF2AA18A
    const val DARK_PRIMARY_EDGE_HEX = 0xFF17564C
    const val DARK_INFO_HEX = 0xFF8FA0B3

    // Tokens extendidos del camino (ADR-026): oro de cofre y corazones.
    const val CHEST_GOLD_HEX = 0xFFFFD700
    const val CHEST_EDGE_HEX = 0xFFB8860B
    const val HEART_RED_HEX = 0xFFFF6B6B
    const val HEART_EMPTY_HEX = 0xFFDFE6E9

    // Variantes oscuras de los tokens extendidos (ADR-026).
    const val DARK_CHEST_GOLD_HEX = 0xFFE6C200
    const val DARK_CHEST_EDGE_HEX = 0xFF8A6A00
    const val DARK_HEART_RED_HEX = 0xFFE05A5A
    const val DARK_HEART_EMPTY_HEX = 0xFF4A4560

    // Tokens nuevos del prototipo (ADR-055): superficies cálidas, tan 3D,
    // terracota y sheet de feedback.
    const val TAN_HEX = 0xFFE4D3B4
    const val TAN_EDGE_HEX = 0xFFC9B58C
    const val TAN_SHADOW_HEX = 0xFFB99F76
    const val TERRACOTTA_HEX = 0xFFC96A4A
    const val TERRACOTTA_INK_HEX = 0xFFFBEFE9
    const val SHEET_WARM_HEX = 0xFFF6DFC8
    const val SHEET_WARM_SURFACE_HEX = 0xFFEBD5B5
    const val SHEET_TITLE_INK_HEX = 0xFF8F4526
    const val CORAL_STRONG_HEX = 0xFFC25A3F
    const val ON_CORAL_STRONG_HEX = 0xFFF6F2E9
    const val ON_INFO_FILL_HEX = 0xFFF6F2E9

    // Variantes oscuras de los tokens nuevos.
    const val DARK_TAN_HEX = 0xFF3B3324
    const val DARK_TAN_EDGE_HEX = 0xFF4A4030
    const val DARK_TAN_SHADOW_HEX = 0xFF241E14
    const val DARK_TERRACOTTA_HEX = 0xFFD8825F
    const val DARK_TERRACOTTA_INK_HEX = 0xFF2A170F
    const val DARK_SHEET_WARM_HEX = 0xFF2A2118
    const val DARK_SHEET_WARM_SURFACE_HEX = 0xFF332A1E
    const val DARK_SHEET_TITLE_INK_HEX = 0xFFE8A87C
    const val DARK_ON_INFO_FILL_HEX = 0xFF14120E

    // Tintas sobre color (ADR-037): texto e iconos de estado sobre los
    // fondos tintados, con ratio WCAG AA ≥ 4.5:1 verificado por test JVM.
    // Modo claro: tintas oscuras sobre tintes claros.
    const val PRIMARY_INK_HEX = 0xFF0E4B44
    const val ACCENT_INK_HEX = 0xFF2F4A33
    const val DESTRUCTIVE_INK_HEX = 0xFF6E2413

    // ADR-045: tintas AA para avisos e información sobre sus tintes claros.
    const val WARNING_INK_HEX = 0xFF5A3E08
    const val INFO_INK_HEX = 0xFF2F3B49

    // Modo oscuro: tintas claras sobre tintes oscuros.
    const val DARK_PRIMARY_INK_HEX = 0xFF8FD6C6
    const val DARK_ACCENT_INK_HEX = 0xFFD8E8D6
    const val DARK_DESTRUCTIVE_INK_HEX = 0xFFFFC9B8
    const val DARK_WARNING_INK_HEX = 0xFFFFE0A8
    const val DARK_INFO_INK_HEX = 0xFFC9D3DE

    // Contenedores de marca (ADR-045): evitan los pasteles por defecto de
    // Material 3 en audio, voz y estados informativos.
    const val SECONDARY_CONTAINER_HEX = 0xFFEAF2E7
    const val ON_SECONDARY_CONTAINER_HEX = 0xFF2F4A33
    const val TERTIARY_CONTAINER_HEX = 0xFFE7ECF2
    const val ON_TERTIARY_CONTAINER_HEX = 0xFF2F3B49
    const val ERROR_CONTAINER_HEX = 0xFFFBE7E1
    const val ON_ERROR_CONTAINER_HEX = 0xFF6E2413
    const val DARK_SECONDARY_CONTAINER_HEX = 0xFF2B3530
    const val DARK_ON_SECONDARY_CONTAINER_HEX = 0xFFCFE3CF
    const val DARK_TERTIARY_CONTAINER_HEX = 0xFF2A2F36
    const val DARK_ON_TERTIARY_CONTAINER_HEX = 0xFFC9D3DE
    const val DARK_ERROR_CONTAINER_HEX = 0xFF3A241D
    const val DARK_ON_ERROR_CONTAINER_HEX = 0xFFFFC9B8

    val Primary = Color(PRIMARY_HEX)
    val PrimaryEdge = Color(PRIMARY_EDGE_HEX)
    val Accent = Color(ACCENT_HEX)
    val Destructive = Color(DESTRUCTIVE_HEX)
    val Warning = Color(WARNING_HEX)
    val Info = Color(INFO_HEX)

    // Neutros del modo claro (superficies y texto)
    val Bg = Color(BG_HEX)
    val BgSecondary = Color(BG_SECONDARY_HEX)
    val Fg = Color(FG_HEX)
    val FgMuted = Color(FG_MUTED_HEX)
    val Border = Color(BORDER_HEX)

    // Tokens nuevos del prototipo (ADR-055)
    val Tan = Color(TAN_HEX)
    val TanEdge = Color(TAN_EDGE_HEX)
    val TanShadow = Color(TAN_SHADOW_HEX)
    val Terracotta = Color(TERRACOTTA_HEX)
    val TerracottaInk = Color(TERRACOTTA_INK_HEX)
    val SheetWarm = Color(SHEET_WARM_HEX)
    val SheetWarmSurface = Color(SHEET_WARM_SURFACE_HEX)
    val SheetTitleInk = Color(SHEET_TITLE_INK_HEX)
    val CoralStrong = Color(CORAL_STRONG_HEX)
    val OnCoralStrong = Color(ON_CORAL_STRONG_HEX)
    val OnInfoFill = Color(ON_INFO_FILL_HEX)

    // Tintas sobre color (ADR-037) — se resuelven por tema en
    // `linguaExtendedColors()`.
    val PrimaryInk = Color(PRIMARY_INK_HEX)
    val AccentInk = Color(ACCENT_INK_HEX)
    val DestructiveInk = Color(DESTRUCTIVE_INK_HEX)
    val WarningInk = Color(WARNING_INK_HEX)
    val InfoInk = Color(INFO_INK_HEX)
    val DarkPrimaryInk = Color(DARK_PRIMARY_INK_HEX)
    val DarkAccentInk = Color(DARK_ACCENT_INK_HEX)
    val DarkDestructiveInk = Color(DARK_DESTRUCTIVE_INK_HEX)
    val DarkWarningInk = Color(DARK_WARNING_INK_HEX)
    val DarkInfoInk = Color(DARK_INFO_INK_HEX)

    // Radio canónicos (§7.2) en dp
    const val RADIUS_BUTTON_DP = 16
    const val RADIUS_CARD_DP = 12
    const val EDGE_BUTTON_DP = 4

    // Radios nuevos del camino (ADR-026); los existentes no se migran aún.
    const val RADIUS_CARD_LARGE_DP = 20
    const val RADIUS_NODE_DP = 24
    const val RADIUS_PILL_DP = 999

    // Física flat-3D (ADR-031): hundimiento del cuerpo y bisel de luz.
    const val PRESS_DEPTH_DP = 4
    const val HIGHLIGHT_ALPHA = 0.18f

    /** ADR-045: el bisel de luz se atenúa en superficies oscuras. */
    const val DARK_HIGHLIGHT_ALPHA = 0.06f
}

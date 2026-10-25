package dev.linguago.core.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Tokens de identidad visual LinguaGo (RFC §7.1) — valores canónicos.
 * Cualquier cambio aquí es un cambio de identidad y requiere ADR.
 */
object LinguaTokens {
    // Hex canónicos (§7.1) — exponerlos como Longs permite testear la
    // identidad sin requerir Android en tests unitarios.
    const val PRIMARY_HEX = 0xFF6C5CE7
    const val PRIMARY_EDGE_HEX = 0xFF5A4BD1
    const val ACCENT_HEX = 0xFF00B894
    const val DESTRUCTIVE_HEX = 0xFFE17055
    const val WARNING_HEX = 0xFFFDCB6E
    const val INFO_HEX = 0xFF74B9FF
    const val BG_HEX = 0xFFFFFFFF
    const val BG_SECONDARY_HEX = 0xFFF8F9FA
    const val FG_HEX = 0xFF2D3436
    const val FG_MUTED_HEX = 0xFF636E72
    const val BORDER_HEX = 0xFFDFE6E9

    // Tokens derivados del modo oscuro (diseño original, coherente con la
    // identidad clara: mismas acentuaciones, superficies invertidas).
    const val DARK_BG_HEX = 0xFF17161F
    const val DARK_BG_SECONDARY_HEX = 0xFF221F30
    const val DARK_FG_HEX = 0xFFEDEBF5
    const val DARK_FG_MUTED_HEX = 0xFFA5A1B8
    const val DARK_BORDER_HEX = 0xFF34304A

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

    // Radio canónicos (§7.2) en dp
    const val RADIUS_BUTTON_DP = 16
    const val RADIUS_CARD_DP = 12
    const val EDGE_BUTTON_DP = 4

    // Radios nuevos del camino (ADR-026); los existentes no se migran aún.
    const val RADIUS_CARD_LARGE_DP = 20
    const val RADIUS_NODE_DP = 24
    const val RADIUS_PILL_DP = 999
}

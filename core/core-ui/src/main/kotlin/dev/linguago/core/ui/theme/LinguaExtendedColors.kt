package dev.linguago.core.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

/**
 * Colores extendidos del camino (ADR-026): oro de cofre y corazones. Se
 * resuelven según la luminancia del fondo del tema activo, de modo que
 * `LinguaTheme` (claro/oscuro/sistema) no necesita cambios.
 */
@Stable
public data class LinguaExtendedColors(
    val chestGold: Color,
    val chestEdge: Color,
    val heartRed: Color,
    val heartEmpty: Color,
)

/** Paleta extendida del tema activo (ADR-026). */
@Composable
public fun linguaExtendedColors(): LinguaExtendedColors {
    val dark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    return if (dark) {
        LinguaExtendedColors(
            chestGold = Color(LinguaTokens.DARK_CHEST_GOLD_HEX),
            chestEdge = Color(LinguaTokens.DARK_CHEST_EDGE_HEX),
            heartRed = Color(LinguaTokens.DARK_HEART_RED_HEX),
            heartEmpty = Color(LinguaTokens.DARK_HEART_EMPTY_HEX),
        )
    } else {
        LinguaExtendedColors(
            chestGold = Color(LinguaTokens.CHEST_GOLD_HEX),
            chestEdge = Color(LinguaTokens.CHEST_EDGE_HEX),
            heartRed = Color(LinguaTokens.HEART_RED_HEX),
            heartEmpty = Color(LinguaTokens.HEART_EMPTY_HEX),
        )
    }
}

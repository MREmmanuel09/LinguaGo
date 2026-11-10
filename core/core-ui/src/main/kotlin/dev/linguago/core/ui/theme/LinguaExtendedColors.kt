package dev.linguago.core.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

/**
 * Paleta extendida resuelta según la luminancia del fondo del tema activo:
 * oro de cofre y corazones (ADR-026) más las tintas sobre color (ADR-037),
 * de modo que `LinguaTheme` (claro/oscuro/sistema) no necesita cambios.
 */
@Stable
public data class LinguaExtendedColors(
    /** Oro de cofre del camino (ADR-026). */
    val chestGold: Color,
    /** Borde inferior del cofre (ADR-026). */
    val chestEdge: Color,
    /** Rojo de las vidas llenas (ADR-026). */
    val heartRed: Color,
    /** Gris de las vidas vacías (ADR-026). */
    val heartEmpty: Color,
    /** Tinta para texto `primary` (seleccionado/enlaces) que cumple AA (ADR-037). */
    val primaryInk: Color,
    /** Tinta de estado correcto que cumple AA sobre su tinte (ADR-037). */
    val accentInk: Color,
    /** Tinta de estado incorrecto que cumple AA sobre su tinte (ADR-037). */
    val destructiveInk: Color,
    /** Tinta de aviso que cumple AA sobre el tinte de advertencia (ADR-045). */
    val warningInk: Color,
    /** Tinta informativa que cumple AA sobre el tinte informativo (ADR-045). */
    val infoInk: Color,
)

/** Paleta extendida del tema activo (ADR-026/ADR-037). */
@Composable
public fun linguaExtendedColors(): LinguaExtendedColors {
    val dark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    return if (dark) {
        LinguaExtendedColors(
            chestGold = Color(LinguaTokens.DARK_CHEST_GOLD_HEX),
            chestEdge = Color(LinguaTokens.DARK_CHEST_EDGE_HEX),
            heartRed = Color(LinguaTokens.DARK_HEART_RED_HEX),
            heartEmpty = Color(LinguaTokens.DARK_HEART_EMPTY_HEX),
            primaryInk = Color(LinguaTokens.DARK_PRIMARY_INK_HEX),
            accentInk = Color(LinguaTokens.DARK_ACCENT_INK_HEX),
            destructiveInk = Color(LinguaTokens.DARK_DESTRUCTIVE_INK_HEX),
            warningInk = Color(LinguaTokens.DARK_WARNING_INK_HEX),
            infoInk = Color(LinguaTokens.DARK_INFO_INK_HEX),
        )
    } else {
        LinguaExtendedColors(
            chestGold = Color(LinguaTokens.CHEST_GOLD_HEX),
            chestEdge = Color(LinguaTokens.CHEST_EDGE_HEX),
            heartRed = Color(LinguaTokens.HEART_RED_HEX),
            heartEmpty = Color(LinguaTokens.HEART_EMPTY_HEX),
            primaryInk = Color(LinguaTokens.PRIMARY_INK_HEX),
            accentInk = Color(LinguaTokens.ACCENT_INK_HEX),
            destructiveInk = Color(LinguaTokens.DESTRUCTIVE_INK_HEX),
            warningInk = Color(LinguaTokens.WARNING_INK_HEX),
            infoInk = Color(LinguaTokens.INFO_INK_HEX),
        )
    }
}

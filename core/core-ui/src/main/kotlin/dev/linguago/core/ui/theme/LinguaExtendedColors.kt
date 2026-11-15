package dev.linguago.core.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

/**
 * Paleta extendida resuelta según la luminancia del fondo del tema activo:
 * oro de cofre y corazones (ADR-026), tintas sobre color (ADR-037) y los
 * tokens nuevos del prototipo «Warm Cream & Teal» (ADR-055), de modo que
 * `LinguaTheme` (claro/oscuro/sistema) no necesita cambios.
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
    /** Superficie tan de chips y tarjetas del prototipo (ADR-055). */
    val tan: Color,
    /** Borde tan de chips y tarjetas (ADR-055). */
    val tanEdge: Color,
    /** Sombra inferior marrón del efecto 3D (ADR-055). */
    val tanShadow: Color,
    /** Terracota de la línea ondulada y acentos cálidos (ADR-055). */
    val terracotta: Color,
    /** Tinta sobre terracota (ADR-055). */
    val terracottaInk: Color,
    /** Fondo del sheet de feedback «Revisa la respuesta» (ADR-055). */
    val sheetWarm: Color,
    /** Superficie interna del sheet (bloque «Ver por qué») (ADR-055). */
    val sheetWarmSurface: Color,
    /** Tinta del título del sheet, AA sobre `sheetWarm` (ADR-055). */
    val sheetTitleInk: Color,
    /** Texto sobre píldoras slate (insignias de tarea) (ADR-055). */
    val onInfoFill: Color,
)

/** Paleta extendida del tema activo (ADR-026/ADR-037/ADR-055). */
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
            tan = Color(LinguaTokens.DARK_TAN_HEX),
            tanEdge = Color(LinguaTokens.DARK_TAN_EDGE_HEX),
            tanShadow = Color(LinguaTokens.DARK_TAN_SHADOW_HEX),
            terracotta = Color(LinguaTokens.DARK_TERRACOTTA_HEX),
            terracottaInk = Color(LinguaTokens.DARK_TERRACOTTA_INK_HEX),
            sheetWarm = Color(LinguaTokens.DARK_SHEET_WARM_HEX),
            sheetWarmSurface = Color(LinguaTokens.DARK_SHEET_WARM_SURFACE_HEX),
            sheetTitleInk = Color(LinguaTokens.DARK_SHEET_TITLE_INK_HEX),
            onInfoFill = Color(LinguaTokens.DARK_ON_INFO_FILL_HEX),
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
            tan = Color(LinguaTokens.TAN_HEX),
            tanEdge = Color(LinguaTokens.TAN_EDGE_HEX),
            tanShadow = Color(LinguaTokens.TAN_SHADOW_HEX),
            terracotta = Color(LinguaTokens.TERRACOTTA_HEX),
            terracottaInk = Color(LinguaTokens.TERRACOTTA_INK_HEX),
            sheetWarm = Color(LinguaTokens.SHEET_WARM_HEX),
            sheetWarmSurface = Color(LinguaTokens.SHEET_WARM_SURFACE_HEX),
            sheetTitleInk = Color(LinguaTokens.SHEET_TITLE_INK_HEX),
            onInfoFill = Color(LinguaTokens.ON_INFO_FILL_HEX),
        )
    }
}

package dev.linguago.core.ui.theme

import dev.linguago.core.ui.components.path.PathStage
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.pow

/**
 * Contraste de las tintas sobre color (ADR-037): cada tinta debe cumplir
 * WCAG AA (≥ 4.5:1) sobre su fondo tintado, calculado por composición alfa
 * sobre el peor caso de cada tema. Trabaja solo con los hex canónicos, sin
 * Android, como el resto de tests de identidad (I7/I8).
 */
class LinguaContrastTest {
    @Test
    fun tintas_claras_cumplen_aa_en_modo_claro() {
        assertTrue(
            "primaryInk sobre primary 6%",
            ratio(LinguaTokens.PRIMARY_INK_HEX, tint(LinguaTokens.PRIMARY_HEX, 0.06f, LinguaTokens.BG_HEX)) >= AA,
        )
        assertTrue(
            "accentInk sobre accent 12%",
            ratio(LinguaTokens.ACCENT_INK_HEX, tint(LinguaTokens.ACCENT_HEX, 0.12f, LinguaTokens.BG_HEX)) >= AA,
        )
        assertTrue(
            "accentInk sobre accent 18% (banner)",
            ratio(LinguaTokens.ACCENT_INK_HEX, tint(LinguaTokens.ACCENT_HEX, 0.18f, LinguaTokens.BG_HEX)) >= AA,
        )
        assertTrue(
            "destructiveInk sobre destructive 12%",
            ratio(
                LinguaTokens.DESTRUCTIVE_INK_HEX,
                tint(LinguaTokens.DESTRUCTIVE_HEX, 0.12f, LinguaTokens.BG_HEX),
            ) >= AA,
        )
        assertTrue(
            "destructiveInk sobre destructive 18% (banner)",
            ratio(
                LinguaTokens.DESTRUCTIVE_INK_HEX,
                tint(LinguaTokens.DESTRUCTIVE_HEX, 0.18f, LinguaTokens.BG_HEX),
            ) >= AA,
        )
        assertTrue(
            "warningInk sobre warning 18%",
            ratio(
                LinguaTokens.WARNING_INK_HEX,
                tint(LinguaTokens.WARNING_HEX, 0.18f, LinguaTokens.BG_HEX),
            ) >= AA,
        )
        assertTrue(
            "infoInk sobre info 18%",
            ratio(
                LinguaTokens.INFO_INK_HEX,
                tint(LinguaTokens.INFO_HEX, 0.18f, LinguaTokens.BG_HEX),
            ) >= AA,
        )
    }

    @Test
    fun path_stage_on_accent_cumple_aa() {
        PathStage.entries.forEach { stage ->
            assertTrue(
                "onAccent de ${stage.label} sobre ${stage.label}",
                ratio(toHex(stage.onAccent), toHex(stage.accent)) >= AA,
            )
        }
    }

    private fun toHex(color: androidx.compose.ui.graphics.Color): Long =
        (0xFFL shl 24) or
            ((color.red * 255f).toLong() shl 16) or
            ((color.green * 255f).toLong() shl 8) or
            (color.blue * 255f).toLong()

    @Test
    fun tintas_oscuras_cumplen_aa_en_modo_oscuro() {
        // Peor caso oscuro: el tinte se compone sobre la superficie clara
        // del tema (DARK_BG_SECONDARY), que aclara más el fondo tintado.
        assertTrue(
            "primaryInk sobre primary 6%",
            ratio(
                LinguaTokens.DARK_PRIMARY_INK_HEX,
                tint(LinguaTokens.PRIMARY_HEX, 0.06f, LinguaTokens.DARK_BG_SECONDARY_HEX),
            ) >= AA,
        )
        assertTrue(
            "accentInk sobre accent 12%",
            ratio(
                LinguaTokens.DARK_ACCENT_INK_HEX,
                tint(LinguaTokens.ACCENT_HEX, 0.12f, LinguaTokens.DARK_BG_SECONDARY_HEX),
            ) >= AA,
        )
        assertTrue(
            "accentInk sobre accent 18% (banner)",
            ratio(
                LinguaTokens.DARK_ACCENT_INK_HEX,
                tint(LinguaTokens.ACCENT_HEX, 0.18f, LinguaTokens.DARK_BG_SECONDARY_HEX),
            ) >= AA,
        )
        assertTrue(
            "destructiveInk sobre destructive 12%",
            ratio(
                LinguaTokens.DARK_DESTRUCTIVE_INK_HEX,
                tint(LinguaTokens.DESTRUCTIVE_HEX, 0.12f, LinguaTokens.DARK_BG_SECONDARY_HEX),
            ) >= AA,
        )
        assertTrue(
            "destructiveInk sobre destructive 18% (banner)",
            ratio(
                LinguaTokens.DARK_DESTRUCTIVE_INK_HEX,
                tint(LinguaTokens.DESTRUCTIVE_HEX, 0.18f, LinguaTokens.DARK_BG_SECONDARY_HEX),
            ) >= AA,
        )
        assertTrue(
            "warningInk sobre warning 18%",
            ratio(
                LinguaTokens.DARK_WARNING_INK_HEX,
                tint(LinguaTokens.WARNING_HEX, 0.18f, LinguaTokens.DARK_BG_SECONDARY_HEX),
            ) >= AA,
        )
        assertTrue(
            "infoInk sobre info 18%",
            ratio(
                LinguaTokens.DARK_INFO_INK_HEX,
                tint(LinguaTokens.INFO_HEX, 0.18f, LinguaTokens.DARK_BG_SECONDARY_HEX),
            ) >= AA,
        )
    }

    /** Composición alfa sRGB: `foreground` con [alpha] sobre `background`. */
    private fun tint(
        foreground: Long,
        alpha: Float,
        background: Long,
    ): Long {
        fun channel(shift: Int): Long {
            val fg = (foreground shr shift) and 0xFF
            val bg = (background shr shift) and 0xFF
            return (fg * alpha + bg * (1f - alpha)).toLong()
        }
        return 0xFF000000L or (channel(16) shl 16) or (channel(8) shl 8) or channel(0)
    }

    /** Luminancia relativa WCAG 2.1 de un color hex `0xAARRGGBB`. */
    private fun luminance(color: Long): Double {
        fun channel(shift: Int): Double {
            val value = ((color shr shift) and 0xFF) / 255.0
            return if (value <= 0.03928) value / 12.92 else ((value + 0.055) / 1.055).pow(2.4)
        }
        return 0.2126 * channel(16) + 0.7152 * channel(8) + 0.0722 * channel(0)
    }

    /** Razón de contraste WCAG entre dos colores. */
    private fun ratio(
        first: Long,
        second: Long,
    ): Double {
        val firstLum = luminance(first)
        val secondLum = luminance(second)
        val higher = maxOf(firstLum, secondLum)
        val lower = minOf(firstLum, secondLum)
        return (higher + 0.05) / (lower + 0.05)
    }

    private companion object {
        const val AA = 4.5
    }
}

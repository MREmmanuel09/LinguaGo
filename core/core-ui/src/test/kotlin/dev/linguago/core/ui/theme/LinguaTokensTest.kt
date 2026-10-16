package dev.linguago.core.ui.theme

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Test de identidad (I5): garantiza que los tokens canónicos del §7.1 del
 * RFC no cambian de value sin revisión explícita vía ADR.
 */
class LinguaTokensTest {
    @Test
    fun tokens_de_identidad_coinciden_con_el_RFC() {
        assertEquals(0xFF6C5CE7L, LinguaTokens.PRIMARY_HEX)
        assertEquals(0xFF5A4BD1L, LinguaTokens.PRIMARY_EDGE_HEX)
        assertEquals(0xFF00B894L, LinguaTokens.ACCENT_HEX)
        assertEquals(0xFFE17055L, LinguaTokens.DESTRUCTIVE_HEX)
        assertEquals(0xFFFDCB6EL, LinguaTokens.WARNING_HEX)
        assertEquals(0xFF74B9FFL, LinguaTokens.INFO_HEX)
        assertEquals(0xFFFFFFFFL, LinguaTokens.BG_HEX)
        assertEquals(0xFFF8F9FAL, LinguaTokens.BG_SECONDARY_HEX)
        assertEquals(0xFF2D3436L, LinguaTokens.FG_HEX)
        assertEquals(0xFF636E72L, LinguaTokens.FG_MUTED_HEX)
        assertEquals(0xFFDFE6E9L, LinguaTokens.BORDER_HEX)
    }

    @Test
    fun tokens_derivados_modo_oscuro_definidos() {
        assertEquals(0xFF17161FL, LinguaTokens.DARK_BG_HEX)
        assertEquals(0xFF221F30L, LinguaTokens.DARK_BG_SECONDARY_HEX)
        assertEquals(0xFFEDEBF5L, LinguaTokens.DARK_FG_HEX)
        assertEquals(0xFFA5A1B8L, LinguaTokens.DARK_FG_MUTED_HEX)
        assertEquals(0xFF34304AL, LinguaTokens.DARK_BORDER_HEX)
    }

    @Test
    fun radii_canonicos() {
        assertEquals(16, LinguaTokens.RADIUS_BUTTON_DP)
        assertEquals(12, LinguaTokens.RADIUS_CARD_DP)
        assertEquals(4, LinguaTokens.EDGE_BUTTON_DP)
    }
}

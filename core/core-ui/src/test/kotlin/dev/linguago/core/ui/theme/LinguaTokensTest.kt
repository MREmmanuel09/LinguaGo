package dev.linguago.core.ui.theme

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Test de identidad (I5): garantiza que los tokens canónicos del prototipo
 * «Warm Cream & Teal» (ADR-055) no cambian de valor sin revisión explícita
 * vía ADR.
 */
class LinguaTokensTest {
    @Test
    fun tokens_de_identidad_coinciden_con_el_adr_055() {
        assertEquals(0xFF1F6E64L, LinguaTokens.PRIMARY_HEX)
        assertEquals(0xFF0E4B44L, LinguaTokens.PRIMARY_EDGE_HEX)
        assertEquals(0xFFA3C4A8L, LinguaTokens.ACCENT_HEX)
        assertEquals(0xFFE2725BL, LinguaTokens.DESTRUCTIVE_HEX)
        assertEquals(0xFFE2A83DL, LinguaTokens.WARNING_HEX)
        assertEquals(0xFF5D6B7AL, LinguaTokens.INFO_HEX)
        assertEquals(0xFFF6F2E9L, LinguaTokens.BG_HEX)
        assertEquals(0xFFFCF8EFL, LinguaTokens.BG_SECONDARY_HEX)
        assertEquals(0xFF2F2C27L, LinguaTokens.FG_HEX)
        assertEquals(0xFF71695CL, LinguaTokens.FG_MUTED_HEX)
        assertEquals(0xFFE2D8C3L, LinguaTokens.BORDER_HEX)
    }

    @Test
    fun tokens_derivados_modo_oscuro_definidos() {
        assertEquals(0xFF14120EL, LinguaTokens.DARK_BG_HEX)
        assertEquals(0xFF1D1A12L, LinguaTokens.DARK_BG_SECONDARY_HEX)
        assertEquals(0xFFF2EDE2L, LinguaTokens.DARK_FG_HEX)
        assertEquals(0xFFB7AE9CL, LinguaTokens.DARK_FG_MUTED_HEX)
        assertEquals(0xFF332E24L, LinguaTokens.DARK_BORDER_HEX)
        assertEquals(0xFF2AA18AL, LinguaTokens.DARK_PRIMARY_HEX)
        assertEquals(0xFF17564CL, LinguaTokens.DARK_PRIMARY_EDGE_HEX)
        assertEquals(0xFF8FA0B3L, LinguaTokens.DARK_INFO_HEX)
    }

    @Test
    fun tokens_nuevos_del_prototipo() {
        assertEquals(0xFFE4D3B4L, LinguaTokens.TAN_HEX)
        assertEquals(0xFFC9B58CL, LinguaTokens.TAN_EDGE_HEX)
        assertEquals(0xFFB99F76L, LinguaTokens.TAN_SHADOW_HEX)
        assertEquals(0xFFC96A4AL, LinguaTokens.TERRACOTTA_HEX)
        assertEquals(0xFFFBEFE9L, LinguaTokens.TERRACOTTA_INK_HEX)
        assertEquals(0xFFF6DFC8L, LinguaTokens.SHEET_WARM_HEX)
        assertEquals(0xFFEBD5B5L, LinguaTokens.SHEET_WARM_SURFACE_HEX)
        assertEquals(0xFF8F4526L, LinguaTokens.SHEET_TITLE_INK_HEX)
        assertEquals(0xFFC25A3FL, LinguaTokens.CORAL_STRONG_HEX)
        assertEquals(0xFFF6F2E9L, LinguaTokens.ON_CORAL_STRONG_HEX)
        assertEquals(0xFFF6F2E9L, LinguaTokens.ON_INFO_FILL_HEX)
    }

    @Test
    fun tokens_nuevos_modo_oscuro() {
        assertEquals(0xFF3B3324L, LinguaTokens.DARK_TAN_HEX)
        assertEquals(0xFF4A4030L, LinguaTokens.DARK_TAN_EDGE_HEX)
        assertEquals(0xFF241E14L, LinguaTokens.DARK_TAN_SHADOW_HEX)
        assertEquals(0xFFD8825FL, LinguaTokens.DARK_TERRACOTTA_HEX)
        assertEquals(0xFF2A170FL, LinguaTokens.DARK_TERRACOTTA_INK_HEX)
        assertEquals(0xFF2A2118L, LinguaTokens.DARK_SHEET_WARM_HEX)
        assertEquals(0xFF332A1EL, LinguaTokens.DARK_SHEET_WARM_SURFACE_HEX)
        assertEquals(0xFFE8A87CL, LinguaTokens.DARK_SHEET_TITLE_INK_HEX)
        assertEquals(0xFF14120EL, LinguaTokens.DARK_ON_INFO_FILL_HEX)
    }

    @Test
    fun radii_canonicos() {
        assertEquals(16, LinguaTokens.RADIUS_BUTTON_DP)
        assertEquals(12, LinguaTokens.RADIUS_CARD_DP)
        assertEquals(4, LinguaTokens.EDGE_BUTTON_DP)
    }

    @Test
    fun tokens_extendidos_del_camino() {
        assertEquals(0xFFFFD700L, LinguaTokens.CHEST_GOLD_HEX)
        assertEquals(0xFFB8860BL, LinguaTokens.CHEST_EDGE_HEX)
        assertEquals(0xFFFF6B6BL, LinguaTokens.HEART_RED_HEX)
        assertEquals(0xFFDFE6E9L, LinguaTokens.HEART_EMPTY_HEX)
        assertEquals(0xFFE6C200L, LinguaTokens.DARK_CHEST_GOLD_HEX)
        assertEquals(0xFFE05A5AL, LinguaTokens.DARK_HEART_RED_HEX)
    }

    @Test
    fun radii_nuevos_del_camino() {
        assertEquals(20, LinguaTokens.RADIUS_CARD_LARGE_DP)
        assertEquals(24, LinguaTokens.RADIUS_NODE_DP)
        assertEquals(999, LinguaTokens.RADIUS_PILL_DP)
    }

    @Test
    fun fisica_tactil_3d() {
        assertEquals(4, LinguaTokens.PRESS_DEPTH_DP)
        assertEquals(0.18f, LinguaTokens.HIGHLIGHT_ALPHA, 0.0001f)
    }

    @Test
    fun tintas_sobre_color_adr_055() {
        assertEquals(0xFF0E4B44L, LinguaTokens.PRIMARY_INK_HEX)
        assertEquals(0xFF2F4A33L, LinguaTokens.ACCENT_INK_HEX)
        assertEquals(0xFF6E2413L, LinguaTokens.DESTRUCTIVE_INK_HEX)
        assertEquals(0xFF5A3E08L, LinguaTokens.WARNING_INK_HEX)
        assertEquals(0xFF2F3B49L, LinguaTokens.INFO_INK_HEX)
        assertEquals(0xFF8FD6C6L, LinguaTokens.DARK_PRIMARY_INK_HEX)
        assertEquals(0xFFD8E8D6L, LinguaTokens.DARK_ACCENT_INK_HEX)
        assertEquals(0xFFFFC9B8L, LinguaTokens.DARK_DESTRUCTIVE_INK_HEX)
        assertEquals(0xFFFFE0A8L, LinguaTokens.DARK_WARNING_INK_HEX)
        assertEquals(0xFFC9D3DEL, LinguaTokens.DARK_INFO_INK_HEX)
    }

    @Test
    fun contenedores_adr_055() {
        assertEquals(0xFFEAF2E7L, LinguaTokens.SECONDARY_CONTAINER_HEX)
        assertEquals(0xFF2F4A33L, LinguaTokens.ON_SECONDARY_CONTAINER_HEX)
        assertEquals(0xFFE7ECF2L, LinguaTokens.TERTIARY_CONTAINER_HEX)
        assertEquals(0xFF2F3B49L, LinguaTokens.ON_TERTIARY_CONTAINER_HEX)
        assertEquals(0xFFFBE7E1L, LinguaTokens.ERROR_CONTAINER_HEX)
        assertEquals(0xFF6E2413L, LinguaTokens.ON_ERROR_CONTAINER_HEX)
        assertEquals(0xFF2B3530L, LinguaTokens.DARK_SECONDARY_CONTAINER_HEX)
        assertEquals(0xFFCFE3CFL, LinguaTokens.DARK_ON_SECONDARY_CONTAINER_HEX)
        assertEquals(0xFF2A2F36L, LinguaTokens.DARK_TERTIARY_CONTAINER_HEX)
        assertEquals(0xFFC9D3DEL, LinguaTokens.DARK_ON_TERTIARY_CONTAINER_HEX)
        assertEquals(0xFF3A241DL, LinguaTokens.DARK_ERROR_CONTAINER_HEX)
        assertEquals(0xFFFFC9B8L, LinguaTokens.DARK_ON_ERROR_CONTAINER_HEX)
    }
}

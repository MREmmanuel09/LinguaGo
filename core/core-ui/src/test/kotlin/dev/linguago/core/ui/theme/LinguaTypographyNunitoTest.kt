package dev.linguago.core.ui.theme

import androidx.compose.ui.text.TextStyle
import org.junit.Assert.assertEquals
import org.junit.Test

/** Garantiza que ningún slot tipográfico caiga a Roboto (B-12, V-08). */
class LinguaTypographyNunitoTest {
    private fun assertNunito(
        style: TextStyle,
        name: String,
    ) {
        assertEquals("Nunito esperado en $name", Nunito, style.fontFamily)
    }

    @Test
    fun todos_los_estilos_usan_nunito() {
        assertNunito(LinguaTypography.displayLarge, "displayLarge")
        assertNunito(LinguaTypography.displayMedium, "displayMedium")
        assertNunito(LinguaTypography.displaySmall, "displaySmall")
        assertNunito(LinguaTypography.headlineLarge, "headlineLarge")
        assertNunito(LinguaTypography.headlineMedium, "headlineMedium")
        assertNunito(LinguaTypography.headlineSmall, "headlineSmall")
        assertNunito(LinguaTypography.titleLarge, "titleLarge")
        assertNunito(LinguaTypography.titleMedium, "titleMedium")
        assertNunito(LinguaTypography.titleSmall, "titleSmall")
        assertNunito(LinguaTypography.bodyLarge, "bodyLarge")
        assertNunito(LinguaTypography.bodyMedium, "bodyMedium")
        assertNunito(LinguaTypography.bodySmall, "bodySmall")
        assertNunito(LinguaTypography.labelLarge, "labelLarge")
        assertNunito(LinguaTypography.labelMedium, "labelMedium")
        assertNunito(LinguaTypography.labelSmall, "labelSmall")
    }
}

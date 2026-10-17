package dev.linguago.core.audio

import dev.linguago.core.domain.model.Language
import org.junit.Assert.assertEquals
import org.junit.Test

class LanguageHooksTest {
    @Test
    fun `cada idioma mapea a su locale BCP-47`() {
        assertEquals("es-ES", Language.SPANISH.toLocale().toLanguageTag())
        assertEquals("en-US", Language.ENGLISH.toLocale().toLanguageTag())
        assertEquals("pt-BR", Language.PORTUGUESE.toLocale().toLanguageTag())
        assertEquals("fr-FR", Language.FRENCH.toLocale().toLanguageTag())
        assertEquals("ja-JP", Language.JAPANESE.toLocale().toLanguageTag())
    }
}

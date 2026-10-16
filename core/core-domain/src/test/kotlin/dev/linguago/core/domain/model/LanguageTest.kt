package dev.linguago.core.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class LanguageTest {
    @Test
    fun fromIsoCode_resuelve_idiomas_soportados() {
        assertEquals(Language.SPANISH, Language.fromIsoCode("es"))
        assertEquals(Language.ENGLISH, Language.fromIsoCode("en"))
        assertEquals(Language.PORTUGUESE, Language.fromIsoCode("pt"))
        assertEquals(Language.FRENCH, Language.fromIsoCode("fr"))
        assertEquals(Language.JAPANESE, Language.fromIsoCode("ja"))
    }

    @Test
    fun fromIsoCode_devuelve_nulo_para_desconocido() {
        assertEquals(null, Language.fromIsoCode("zh"))
    }
}

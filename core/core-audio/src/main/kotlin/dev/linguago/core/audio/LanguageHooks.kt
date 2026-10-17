package dev.linguago.core.audio

import dev.linguago.core.domain.model.Language
import java.util.Locale

/** Mapeo de idiomas del dominio → locales de Android/voz. */
public fun Language.toLocale(): Locale =
    when (this) {
        Language.SPANISH -> Locale.forLanguageTag("es-ES")
        Language.ENGLISH -> Locale.forLanguageTag("en-US")
        Language.PORTUGUESE -> Locale.forLanguageTag("pt-BR")
        Language.FRENCH -> Locale.forLanguageTag("fr-FR")
        Language.JAPANESE -> Locale.forLanguageTag("ja-JP")
    }

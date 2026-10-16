package dev.linguago.core.ui.components

import dev.linguago.core.ui.R

/**
 * Íconos vectoriales originales de LinguaGo (invariante I5: identidad
 * original — ningún asset copiado). Se referencian por id y se pintan con
 * `Image(painterResource(...), tint = ...)` para heredar el color del tema.
 */
object LinguaIcons {
    const val FlameRes: Int = R.drawable.ic_flame
    const val HomeRes: Int = R.drawable.ic_home
    const val ProfileRes: Int = R.drawable.ic_profile
    const val SettingsRes: Int = R.drawable.ic_settings
    const val CheckRes: Int = R.drawable.ic_check
    const val CloseRes: Int = R.drawable.ic_close
}

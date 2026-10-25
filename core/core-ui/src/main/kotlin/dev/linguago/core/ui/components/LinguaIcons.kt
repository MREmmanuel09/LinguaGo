package dev.linguago.core.ui.components

import dev.linguago.core.ui.R

/**
 * Íconos vectoriales originales de LinguaGo (invariante I5: identidad
 * original — ningún asset copiado). Se referencian por id y se pintan con
 * `Image(painterResource(...), tint = ...)` para heredar el color del tema.
 * Los ids NO son `const`: el R de un library es no-final y el id real lo
 * asigna el app en el merge (un `const` inlinea 0x0 y rompe en runtime).
 */
object LinguaIcons {
    val FlameRes: Int = R.drawable.ic_flame
    val HomeRes: Int = R.drawable.ic_home
    val ProfileRes: Int = R.drawable.ic_profile
    val SettingsRes: Int = R.drawable.ic_settings
    val CheckRes: Int = R.drawable.ic_check
    val CloseRes: Int = R.drawable.ic_close
    val HeartRes: Int = R.drawable.ic_heart
    val ChestRes: Int = R.drawable.ic_chest
    val LockRes: Int = R.drawable.ic_lock
    val PlayRes: Int = R.drawable.ic_play
    val LumiRes: Int = R.drawable.ic_lumi
}

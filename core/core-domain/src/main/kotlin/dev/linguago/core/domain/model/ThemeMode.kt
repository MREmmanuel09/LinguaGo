package dev.linguago.core.domain.model

/**
 * Modo de tema de la app (persistido en DataStore por core-data).
 * NO confundir con [Language]: este enum habla del tema visual.
 */
public enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK,
}

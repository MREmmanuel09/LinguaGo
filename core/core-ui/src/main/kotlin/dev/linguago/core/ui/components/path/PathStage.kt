package dev.linguago.core.ui.components.path

import androidx.compose.ui.graphics.Color
import dev.linguago.core.ui.theme.LinguaTokens

/**
 * Etapa CEFR del camino (ADR-032). Se asigna por el índice del nivel en la
 * rampa del curso; no se persiste. Usa tokens existentes, sin hex nuevos.
 */
public enum class PathStage(
    public val label: String,
) {
    A1("A1"),
    A2("A2"),
    B1("B1"),
    B2("B2"),
    ;

    /** Color de acento de la etapa. */
    public val accent: Color
        get() =
            when (this) {
                A1 -> LinguaTokens.Accent
                A2 -> LinguaTokens.Info
                B1 -> LinguaTokens.Primary
                B2 -> LinguaTokens.Warning
            }

    /** Color de contenido sobre [accent] con contraste suficiente (I8). */
    public val onAccent: Color
        get() = if (this == B2) LinguaTokens.Fg else Color.White
}

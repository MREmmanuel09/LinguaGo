package dev.linguago.core.ui.components.path

import androidx.compose.ui.graphics.Color
import dev.linguago.core.ui.theme.LinguaTokens

/**
 * Etapa CEFR del camino (ADR-032; C1/C2 en ADR-042) con la paleta
 * «Warm Cream & Teal» (ADR-055). Se asigna por el CEFR del contenido del
 * nivel; no se persiste. Usa tokens existentes, sin hex nuevos: A1 sage,
 * A2 slate, B1 teal, B2 ámbar, C1 teal profundo y C2 el oro de cofre que
 * marca la cima del camino.
 */
public enum class PathStage(
    public val label: String,
) {
    A1("A1"),
    A2("A2"),
    B1("B1"),
    B2("B2"),
    C1("C1"),
    C2("C2"),
    ;

    /** Color de acento de la etapa. */
    public val accent: Color
        get() =
            when (this) {
                A1 -> LinguaTokens.Accent
                A2 -> LinguaTokens.Info
                B1 -> LinguaTokens.Primary
                B2 -> LinguaTokens.Warning
                C1 -> LinguaTokens.PrimaryEdge
                C2 -> Color(LinguaTokens.CHEST_GOLD_HEX)
            }

    /**
     * Color de contenido sobre [accent] con contraste AA (I8). A1, A2, B2 y
     * C2 usan tintes claros y necesitan tinta oscura; B1 y C1 son tonos
     * profundos y mantienen crema (ADR-055).
     */
    public val onAccent: Color
        get() =
            when (this) {
                PathStage.A1 -> LinguaTokens.AccentInk
                PathStage.A2 -> LinguaTokens.OnInfoFill
                PathStage.B2 -> LinguaTokens.WarningInk
                PathStage.C2 -> LinguaTokens.Fg
                else -> LinguaTokens.Bg
            }
}

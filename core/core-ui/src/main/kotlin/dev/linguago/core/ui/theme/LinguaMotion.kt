package dev.linguago.core.ui.theme

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.spring

/**
 * Especificaciones de movimiento del sistema "Feather" (ADR-026). Compose
 * nativo: sin librerías externas. Rebote elástico para el toque de nodos y
 * respuesta seca para estados y contadores.
 */
public object LinguaMotion {
    /** Rebote elástico (nodos del camino al presionar/soltar). */
    public val SpringBouncy: SpringSpec<Float> =
        spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow,
        )

    /** Respuesta rápida sin rebote (contadores, corazones, checks). */
    public val SpringSnappy: SpringSpec<Float> =
        spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessHigh,
        )
}

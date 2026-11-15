package dev.linguago.core.ui.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.spring

/**
 * Especificaciones de movimiento del prototipo «Warm Cream & Teal» (ADR-055).
 * Compose nativo: sin librerías externas. Los nodos del camino conservan el
 * rebote elástico del sistema Feather; el resto del prototipo usa las
 * duraciones y curvas del vídeo de referencia.
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

    /** Curva desacelerada del prototipo (sheets y transiciones). */
    public val EasingDecelerate: Easing = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1f)

    /** Curva estándar del prototipo (cambios de estado). */
    public val EasingStandard: Easing = CubicBezierEasing(0.2f, 0f, 0f, 1f)

    /** Pulsación de chips 3D (escala 0,97). */
    public const val DURATION_CHIP_MS: Int = 80

    /** Botones y estados (Continuar, calificación). */
    public const val DURATION_PRESS_MS: Int = 200

    /** Nodo del camino al activarse. */
    public const val DURATION_PATH_NODE_MS: Int = 220

    /** Transición entre tarjetas del repaso. */
    public const val DURATION_DECK_MS: Int = 260

    /** Entrada del sheet «Revisa la respuesta». */
    public const val DURATION_SHEET_MS: Int = 350

    /** Anillos del micrófono en escucha (bucle). */
    public const val DURATION_MIC_RIPPLE_MS: Int = 1200
}

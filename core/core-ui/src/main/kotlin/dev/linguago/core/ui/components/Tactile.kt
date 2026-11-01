package dev.linguago.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import dev.linguago.core.ui.theme.LinguaTokens

/**
 * Bisel de luz superior del lenguaje flat-3D (ADR-031): un degradado blanco
 * sutil que se apaga hacia la mitad del cuerpo. Se aplica después del color
 * de fondo y antes del contenido; el recorte lo aporta el `clip` previo.
 */
internal fun Modifier.topHighlight(): Modifier =
    background(
        Brush.verticalGradient(
            0f to Color.White.copy(alpha = LinguaTokens.HIGHLIGHT_ALPHA),
            0.55f to Color.Transparent,
        ),
    )

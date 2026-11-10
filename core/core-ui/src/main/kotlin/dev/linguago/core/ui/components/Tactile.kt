package dev.linguago.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import dev.linguago.core.ui.theme.LinguaTokens

/**
 * Bisel de luz superior del lenguaje flat-3D (ADR-031): un degradado blanco
 * sutil que se apaga hacia la mitad del cuerpo. Se aplica después del color
 * de fondo y antes del contenido; el recorte lo aporta el `clip` previo.
 * En superficies oscuras el brillo se atenúa para no dejar una neblina gris
 * sobre el color de marca (V-03, ADR-045).
 */
@Composable
internal fun Modifier.topHighlight(): Modifier {
    val dark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val alpha = if (dark) LinguaTokens.DARK_HIGHLIGHT_ALPHA else LinguaTokens.HIGHLIGHT_ALPHA
    return background(
        Brush.verticalGradient(
            0f to Color.White.copy(alpha = alpha),
            0.55f to Color.Transparent,
        ),
    )
}

package dev.linguago.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import dev.linguago.core.ui.theme.LinguaTokens
import dev.linguago.core.ui.theme.linguaExtendedColors

private val DECK_SHAPE = RoundedCornerShape(LinguaTokens.RADIUS_CARD_LARGE_DP.dp)

/**
 * Baraja de tarjetas del repaso según el prototipo «Warm Cream & Teal»
 * (ADR-055): dos tarjetas crema con borde tan asoman 8dp y 16dp por debajo
 * de la activa. El tamaño de la pila lo manda el contenido de la tarjeta
 * activa (matchParentSize); la física de volteo y la lógica FSRS viven fuera
 * (I4).
 */
@Composable
public fun ReviewDeckStack(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val extended = linguaExtendedColors()
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.TopCenter,
    ) {
        Box(
            modifier =
                Modifier
                    .matchParentSize()
                    .offset(y = 16.dp)
                    .clip(DECK_SHAPE)
                    .background(colors.surfaceVariant)
                    .border(1.5.dp, extended.tanEdge, DECK_SHAPE),
        )
        Box(
            modifier =
                Modifier
                    .matchParentSize()
                    .offset(y = 8.dp)
                    .clip(DECK_SHAPE)
                    .background(colors.surfaceVariant)
                    .border(1.5.dp, extended.tanEdge, DECK_SHAPE),
        )
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clip(DECK_SHAPE)
                    .background(colors.surfaceVariant)
                    .border(1.5.dp, extended.tanEdge, DECK_SHAPE)
                    .padding(20.dp),
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                content()
            }
        }
    }
}

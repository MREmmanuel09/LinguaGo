package dev.linguago.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties

/**
 * Burbuja con la traducción de una palabra o frase (ADR-036/ADR-040). Admite
 * el término, una o varias traducciones y el anclaje a una palabra concreta
 * del componente mediante [anchorOffset]/[anchorWidth] (en píxeles). No es
 * focusable y no bloquea la interacción.
 */
@Composable
internal fun TranslationTooltip(
    translations: List<String>,
    visible: Boolean,
    onDismiss: () -> Unit,
    term: String? = null,
    anchorOffset: IntOffset = IntOffset.Zero,
    anchorWidth: Int = 0,
    /** Mantiene la burbuja tras soltar y la cierra al tocar fuera (ADR-040). */
    dismissible: Boolean = false,
) {
    if (!visible || translations.isEmpty()) return
    val marginPx = with(LocalDensity.current) { 8.dp.roundToPx() }
    val provider =
        remember(marginPx, anchorOffset, anchorWidth) {
            AboveAnchorPositionProvider(marginPx, anchorOffset, anchorWidth)
        }
    val colors = MaterialTheme.colorScheme

    Popup(
        popupPositionProvider = provider,
        onDismissRequest = onDismiss,
        properties =
            PopupProperties(
                focusable = dismissible,
                dismissOnClickOutside = dismissible,
                dismissOnBackPress = dismissible,
            ),
    ) {
        Column(
            modifier =
                Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(colors.inverseSurface)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
        ) {
            term?.let { text ->
                Text(
                    text = text,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = colors.inverseOnSurface,
                )
            }
            translations.forEach { translation ->
                Text(
                    text = translation,
                    style = MaterialTheme.typography.labelLarge,
                    color = colors.inverseOnSurface,
                )
            }
        }
    }
}

/**
 * Coloca el popup encima del ancla (o de la palabra apuntada por
 * [anchorOffset]/[anchorWidth]), centrado y sin taparla.
 */
private class AboveAnchorPositionProvider(
    private val marginPx: Int,
    private val anchorOffset: IntOffset,
    private val anchorWidth: Int,
) : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize,
    ): IntOffset {
        val maxX = (windowSize.width - popupContentSize.width).coerceAtLeast(0)
        val anchorCenterX =
            if (anchorWidth > 0) {
                anchorBounds.left + anchorOffset.x + anchorWidth / 2
            } else {
                anchorBounds.center.x
            }
        val x = (anchorCenterX - popupContentSize.width / 2).coerceIn(0, maxX)
        val anchorTop =
            if (anchorWidth > 0) {
                anchorBounds.top + anchorOffset.y
            } else {
                anchorBounds.top
            }
        val y = (anchorTop - popupContentSize.height - marginPx).coerceAtLeast(0)
        return IntOffset(x, y)
    }
}

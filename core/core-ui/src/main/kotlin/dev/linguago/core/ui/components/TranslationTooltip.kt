package dev.linguago.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties

/**
 * Nota emergente con la traducción de una palabra (ADR-036). Se coloca encima
 * del ancla, centrada y con margen, para no tapar la palabra. No es focusable
 * y no bloquea la interacción de la ficha.
 */
@Composable
internal fun TranslationTooltip(
    translation: String,
    visible: Boolean,
    onDismiss: () -> Unit,
) {
    if (!visible) return
    val marginPx = with(LocalDensity.current) { 8.dp.roundToPx() }
    val provider = remember(marginPx) { AboveAnchorPositionProvider(marginPx) }
    val colors = MaterialTheme.colorScheme

    Popup(
        popupPositionProvider = provider,
        onDismissRequest = onDismiss,
        properties =
            PopupProperties(
                focusable = false,
                dismissOnClickOutside = false,
                dismissOnBackPress = false,
            ),
    ) {
        Box(
            modifier =
                Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(colors.inverseSurface)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
        ) {
            Text(
                text = translation,
                style = MaterialTheme.typography.labelLarge,
                color = colors.inverseOnSurface,
            )
        }
    }
}

/** Coloca el popup justo encima del ancla, centrado y sin taparla. */
private class AboveAnchorPositionProvider(
    private val marginPx: Int,
) : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize,
    ): IntOffset {
        val maxX = (windowSize.width - popupContentSize.width).coerceAtLeast(0)
        val x = (anchorBounds.center.x - popupContentSize.width / 2).coerceIn(0, maxX)
        val y = (anchorBounds.top - popupContentSize.height - marginPx).coerceAtLeast(0)
        return IntOffset(x, y)
    }
}

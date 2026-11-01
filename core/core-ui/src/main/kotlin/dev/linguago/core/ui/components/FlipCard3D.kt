package dev.linguago.core.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import dev.linguago.core.ui.util.rememberAnimationsEnabled

/**
 * Tarjeta 3D que gira en el eje Y para revelar su reverso (ADR-034). Es un
 * botón accesible con estado; con reduced motion cambia sin giro. El contenido
 * frontal y trasero se compone dentro de `LinguaCard3D`, así que hereda el
 * lenguaje flat-3D del design system.
 */
@Composable
public fun FlipCard3D(
    revealed: Boolean,
    onFlip: () -> Unit,
    modifier: Modifier = Modifier,
    front: @Composable ColumnScope.() -> Unit,
    back: @Composable ColumnScope.() -> Unit,
) {
    val animations = rememberAnimationsEnabled()
    val interaction = remember { MutableInteractionSource() }
    val rotation by animateFloatAsState(
        targetValue = if (revealed) 180f else 0f,
        animationSpec = tween(durationMillis = if (animations) 520 else 0),
        label = "flipCardRotation",
    )
    val showBack = rotation > 90f

    Box(
        modifier =
            modifier
                .graphicsLayer {
                    rotationY = rotation
                    cameraDistance = 14f * density
                }.clickable(
                    interactionSource = interaction,
                    indication = null,
                    onClickLabel = if (revealed) null else "Mostrar traducción",
                    onClick = {
                        if (!revealed) onFlip()
                    },
                ).semantics {
                    role = Role.Button
                    stateDescription = if (revealed) "Traducción visible" else "Palabra visible"
                },
    ) {
        if (showBack) {
            LinguaCard3D(
                modifier = Modifier.fillMaxWidth().graphicsLayer { rotationY = 180f },
                contentPadding = PaddingValues(20.dp),
                content = back,
            )
        } else {
            LinguaCard3D(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(20.dp),
                content = front,
            )
        }
    }
}

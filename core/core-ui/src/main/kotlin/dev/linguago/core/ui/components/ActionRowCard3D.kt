package dev.linguago.core.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.foundation.Image
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.linguago.core.ui.feedback.FeedbackCue
import dev.linguago.core.ui.feedback.LocalInteractionFeedback
import dev.linguago.core.ui.theme.LinguaMotion
import dev.linguago.core.ui.theme.LinguaTheme
import dev.linguago.core.ui.theme.LinguaTokens
import dev.linguago.core.ui.util.rememberAnimationsEnabled

/**
 * Fila de acción con la física flat-3D de Feather (ADR-031/ADR-045): cuerpo
 * opaco, borde inferior de 4 dp y bisel superior. Declara `Role.Button` (no
 * `RadioButton`) y añade un chevron, para ajustes, respaldo y licencias.
 */
@Composable
fun ActionRowCard3D(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    enabled: Boolean = true,
    leading: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
    bodyColor: Color? = null,
) {
    val colors = MaterialTheme.colorScheme
    val feedback = LocalInteractionFeedback.current
    val animations = rememberAnimationsEnabled()
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val press by animateFloatAsState(
        targetValue = if (pressed && animations) 1f else 0f,
        animationSpec = if (animations) LinguaMotion.SpringBouncy else snap(),
        label = "actionRowPress",
    )
    val shape = RoundedCornerShape(LinguaTokens.RADIUS_CARD_DP.dp)
    val body = bodyColor ?: colors.surface
    val contentAlpha = if (enabled) 1f else 0.45f

    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(bottom = LinguaTokens.PRESS_DEPTH_DP.dp),
    ) {
        Box(
            modifier =
                Modifier
                    .matchParentSize()
                    .offset(y = LinguaTokens.PRESS_DEPTH_DP.dp)
                    .clip(shape)
                    .background(colors.outline),
        )
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .offset(y = (press * LinguaTokens.PRESS_DEPTH_DP).dp)
                    .clip(shape)
                    .background(body)
                    .topHighlight()
                    .border(2.dp, colors.outline, shape)
                    .clickable(
                        interactionSource = interaction,
                        indication = LocalIndication.current,
                        enabled = enabled,
                        onClick = {
                            feedback.play(FeedbackCue.TAP)
                            onClick()
                        },
                    ).semantics { role = Role.Button }
                    .heightIn(min = 56.dp)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            leading?.invoke()
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurface.copy(alpha = contentAlpha),
                )
                subtitle?.let { text ->
                    Text(
                        text = text,
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant.copy(alpha = contentAlpha),
                    )
                }
            }
            trailing?.invoke()
            Text(
                text = "›",
                style = MaterialTheme.typography.titleLarge,
                color = colors.onSurfaceVariant.copy(alpha = contentAlpha),
            )
        }
    }
}

/**
 * Cabecera estándar de pantallas secundarias (ADR-045): botón Volver de
 * 48 × 48 dp con descripción accesible y título con la tipografía de marca.
 */
@Composable
fun LinguaTopBar(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    actions: (@Composable () -> Unit)? = null,
) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp)
                .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        IconButton(onClick = onBack) {
            Image(
                painter = painterResource(LinguaIcons.ArrowBackRes),
                contentDescription = "Volver",
                modifier = Modifier.size(24.dp),
                colorFilter = ColorFilter.tint(colors.onSurface),
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(1.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                color = colors.onSurface,
            )
            subtitle?.let { text ->
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant,
                )
            }
        }
        actions?.invoke()
        Spacer(Modifier.size(8.dp))
    }
}

@Preview(showBackground = true)
@Composable
private fun ActionRowCard3DPreview() {
    LinguaTheme {
        ActionRowCard3D(title = "Cambiar de curso", subtitle = "Inglés · A2", onClick = {})
    }
}

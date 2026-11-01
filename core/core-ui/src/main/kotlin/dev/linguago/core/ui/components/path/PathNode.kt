package dev.linguago.core.ui.components.path

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.linguago.core.ui.components.LinguaIcons
import dev.linguago.core.ui.components.topHighlight
import dev.linguago.core.ui.feedback.FeedbackCue
import dev.linguago.core.ui.feedback.LocalInteractionFeedback
import dev.linguago.core.ui.theme.LinguaMotion
import dev.linguago.core.ui.theme.LinguaTheme
import dev.linguago.core.ui.theme.LinguaTokens
import dev.linguago.core.ui.theme.linguaExtendedColors
import dev.linguago.core.ui.util.rememberAnimationsEnabled

/** Estado visual de un nodo del camino (ADR-026). */
enum class PathNodeState {
    LOCKED,
    AVAILABLE,
    IN_PROGRESS,
    COMPLETED,
    CHEST,

    /** Cofre de una unidad aún no completada: visible pero inerte. */
    CHEST_LOCKED,
}

/**
 * Modelo de presentación de un nodo del camino. Lo construye el adaptador de
 * la feature (Etapa 4); la UI no conoce el dominio.
 */
data class PathNodeUi(
    val id: String,
    val label: String,
    val state: PathNodeState,
)

/**
 * Nodo circular del camino con lenguaje flat-3D (ADR-026): cuerpo + borde
 * inferior de 4 dp, rebote elástico al presionar y pulso suave solo en el
 * nodo disponible. Habla por TalkBack con rol de botón y estado (I8).
 */
@Composable
fun PathNode(
    node: PathNodeUi,
    onClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val extended = linguaExtendedColors()
    val animations = rememberAnimationsEnabled()
    val colors = MaterialTheme.colorScheme
    val feedback = LocalInteractionFeedback.current

    val body: Color
    val edge: Color
    val iconRes: Int
    val iconTint: Color
    when (node.state) {
        PathNodeState.COMPLETED -> {
            body = LinguaTokens.Accent
            edge = LinguaTokens.Accent.copy(alpha = 0.72f)
            iconRes = LinguaIcons.CheckRes
            iconTint = Color.White
        }
        PathNodeState.AVAILABLE -> {
            body = LinguaTokens.Primary
            edge = LinguaTokens.PrimaryEdge
            iconRes = LinguaIcons.PlayRes
            iconTint = Color.White
        }
        PathNodeState.IN_PROGRESS -> {
            body = LinguaTokens.Warning
            edge = LinguaTokens.Warning.copy(alpha = 0.72f)
            iconRes = LinguaIcons.PlayRes
            iconTint = LinguaTokens.Fg
        }
        PathNodeState.CHEST -> {
            body = extended.chestGold
            edge = extended.chestEdge
            iconRes = LinguaIcons.ChestRes
            iconTint = LinguaTokens.Fg
        }
        PathNodeState.CHEST_LOCKED -> {
            body = colors.surfaceVariant
            edge = colors.outline
            iconRes = LinguaIcons.ChestRes
            iconTint = colors.onSurfaceVariant
        }
        PathNodeState.LOCKED -> {
            body = colors.surfaceVariant
            edge = colors.outline
            iconRes = LinguaIcons.LockRes
            iconTint = colors.onSurfaceVariant
        }
    }

    val stateLabel =
        when (node.state) {
            PathNodeState.LOCKED -> "Bloqueada"
            PathNodeState.AVAILABLE -> "Disponible"
            PathNodeState.IN_PROGRESS -> "En progreso"
            PathNodeState.COMPLETED -> "Completada"
            PathNodeState.CHEST -> "Cofre"
            PathNodeState.CHEST_LOCKED -> "Cofre bloqueado"
        }
    val isChest = node.state == PathNodeState.CHEST || node.state == PathNodeState.CHEST_LOCKED

    val transition = rememberInfiniteTransition(label = "pathNodePulse")
    val pulse by transition.animateFloat(
        initialValue = 1f,
        targetValue = if (node.state == PathNodeState.AVAILABLE && animations) 1.08f else 1f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(durationMillis = 900),
                repeatMode = RepeatMode.Reverse,
            ),
        label = "pathNodeScale",
    )

    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val press by animateFloatAsState(
        targetValue = if (pressed) 1f else 0f,
        animationSpec = LinguaMotion.SpringSnappy,
        label = "pathNodePress",
    )

    Box(
        modifier =
            modifier
                .size(72.dp)
                .graphicsLayer {
                    scaleX = pulse
                    scaleY = pulse
                    translationY = press * 4.dp.toPx()
                }.semantics(mergeDescendants = true) {
                    role = Role.Button
                    this.stateDescription = stateLabel
                    contentDescription =
                        if (isChest) {
                            "Cofre: ${node.label}"
                        } else {
                            "Lección: ${node.label}. $stateLabel"
                        }
                }.clickable(
                    interactionSource = interaction,
                    indication = null,
                    enabled = node.state != PathNodeState.LOCKED && node.state != PathNodeState.CHEST_LOCKED,
                    onClick = {
                        feedback.play(FeedbackCue.TAP)
                        onClick(node.id)
                    },
                ),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .offset(y = 4.dp)
                    .clip(CircleShape)
                    .background(edge),
        )
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(body)
                    .then(
                        if (node.state == PathNodeState.LOCKED || node.state == PathNodeState.CHEST_LOCKED) {
                            Modifier
                        } else {
                            Modifier.topHighlight()
                        },
                    ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(28.dp),
            )
        }
    }
}

/** Cofre de fin de unidad: nodo independiente que nunca reemplaza la lección. */
@Composable
fun ChestNode(
    id: String,
    label: String,
    onClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        PathNode(node = PathNodeUi(id = id, label = label, state = PathNodeState.CHEST), onClick = onClick)
        Text(
            text = "Cofre",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
        )
    }
}

/** Cabecera de unidad como banner flat-3D con etapa y progreso (ADR-032). */
@Composable
fun UnitHeader(
    title: String,
    completed: Int,
    total: Int,
    modifier: Modifier = Modifier,
    stage: PathStage? = null,
) {
    val colors = MaterialTheme.colorScheme
    val accent = stage?.accent ?: LinguaTokens.Accent
    val target = if (total > 0) completed.toFloat() / total else 0f
    val animations = rememberAnimationsEnabled()
    val progress by animateFloatAsState(
        targetValue = target.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = if (animations) 600 else 0),
        label = "unitHeaderProgress",
    )
    val shape = RoundedCornerShape(LinguaTokens.RADIUS_CARD_LARGE_DP.dp)

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
                    .background(colors.outline.copy(alpha = 0.55f)),
        )
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clip(shape)
                    .background(colors.surfaceVariant)
                    .topHighlight()
                    .semantics(mergeDescendants = true) { heading() }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                stage?.let { current ->
                    Box(
                        modifier =
                            Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(current.accent),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = current.label,
                            style = MaterialTheme.typography.labelLarge,
                            color = current.onAccent,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        color = colors.onBackground,
                    )
                    Text(
                        text = "$completed de $total lecciones",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant,
                    )
                }
            }
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(colors.outline.copy(alpha = 0.25f)),
            ) {
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth(fraction = progress)
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(accent),
                )
            }
        }
    }
}

@Preview(name = "Nodos claro", showBackground = true)
@Composable
private fun PathNodePreview() {
    LinguaTheme {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            PathNode(PathNodeUi("1", "Bloqueada", PathNodeState.LOCKED), onClick = {})
            PathNode(PathNodeUi("2", "Disponible", PathNodeState.AVAILABLE), onClick = {})
            PathNode(PathNodeUi("3", "En progreso", PathNodeState.IN_PROGRESS), onClick = {})
            PathNode(PathNodeUi("4", "Completada", PathNodeState.COMPLETED), onClick = {})
            PathNode(PathNodeUi("5", "Cofre", PathNodeState.CHEST), onClick = {})
        }
    }
}

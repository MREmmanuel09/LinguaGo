package dev.linguago.core.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import dev.linguago.core.ui.feedback.FeedbackCue
import dev.linguago.core.ui.feedback.LocalInteractionFeedback
import dev.linguago.core.ui.theme.LinguaTokens

/**
 * Emparejamiento tipo rompecabezas (ADR-029): las piezas izquierdas tienen una
 * ranura genérica y las derechas una pestaña del mismo color. Se puede tocar
 * o arrastrar; el resultado se muestra al comprobar. No usa colores por pareja
 * para no dar pistas.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
public fun PuzzleMatchBoard(
    pairs: List<PuzzlePairUi>,
    shuffledRights: List<Int>,
    selectedLeft: Int?,
    matched: Map<Int, Int>,
    onSelectLeft: (Int) -> Unit,
    onMatchRight: (Int) -> Unit,
    onUnpair: (Int) -> Unit,
    modifier: Modifier = Modifier,
    instructions: String? = null,
    enabled: Boolean = true,
    result: Boolean? = null,
) {
    val colors = MaterialTheme.colorScheme
    val feedback = LocalInteractionFeedback.current
    val leftCoords = remember { mutableMapOf<Int, LayoutCoordinates>() }
    val drag = remember(pairs) { PuzzleDragState() }
    val freeRights = shuffledRights.filterNot { right -> matched.containsValue(right) }

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = instructions ?: "Empareja las piezas",
            style = MaterialTheme.typography.titleLarge,
            color = colors.onBackground,
            modifier = Modifier.semantics { heading() },
        )
        Text(
            text =
                "Toca una pieza de la izquierda y luego su pareja, o arrastra la pieza derecha. " +
                    "Toca una pareja encajada para separarla.",
            style = MaterialTheme.typography.bodySmall,
            color = colors.onSurfaceVariant,
        )
        Text(
            text = "Piezas del rompecabezas",
            style = MaterialTheme.typography.labelLarge,
            color = colors.primary,
        )

        pairs.forEachIndexed { left, pair ->
            val right = matched[left]
            if (right != null) {
                val rightPair = pairs.getOrNull(right)
                val wrong = result == false && left != right
                val status =
                    when {
                        result == null -> OptionStatus.NEUTRAL
                        wrong -> OptionStatus.INCORRECT
                        else -> OptionStatus.CORRECT
                    }
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Min),
                    horizontalArrangement = if (wrong) Arrangement.spacedBy(8.dp) else Arrangement.spacedBy(0.dp),
                ) {
                    PuzzlePiece(
                        label = pair.left,
                        edge = PuzzleEdge.SOCKET,
                        status = status,
                        selected = false,
                        enabled = enabled && result == null,
                        onClick = { onUnpair(left) },
                        modifier =
                            Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                    )
                    if (rightPair != null) {
                        PuzzlePiece(
                            label = rightPair.right,
                            edge = PuzzleEdge.TAB,
                            status = status,
                            selected = false,
                            enabled = false,
                            onClick = {},
                            badge = rightPair.rightBadge,
                            detail = rightPair.rightDetail,
                            modifier =
                                Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .offset(x = if (wrong) 0.dp else -PUZZLE_TAB_DP),
                        )
                    }
                }
            } else {
                PuzzlePiece(
                    label = pair.left,
                    edge = PuzzleEdge.SOCKET,
                    status = OptionStatus.NEUTRAL,
                    selected = selectedLeft == left,
                    enabled = enabled,
                    hovered = drag.hoveredLeft == left,
                    onClick = {
                        feedback.play(FeedbackCue.SELECT)
                        onSelectLeft(left)
                    },
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .onGloballyPositioned { coordinates -> leftCoords[left] = coordinates },
                )
            }
        }

        if (freeRights.isNotEmpty()) {
            Text(
                text = "Piezas disponibles",
                style = MaterialTheme.typography.labelLarge,
                color = colors.onSurfaceVariant,
            )
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                freeRights.forEach { right ->
                    val pair = pairs.getOrNull(right) ?: return@forEach
                    val dragging = drag.draggingRight == right
                    Box(
                        modifier =
                            Modifier
                                .widthIn(min = 160.dp)
                                .zIndex(if (dragging) 1f else 0f)
                                .graphicsLayer {
                                    if (dragging) {
                                        translationX = drag.offset.value.x
                                        translationY = drag.offset.value.y
                                    }
                                }.onGloballyPositioned { coordinates -> drag.rightCoords[right] = coordinates }
                                .pointerInput(right, enabled, matched) {
                                    if (!enabled) return@pointerInput
                                    detectDragGestures(
                                        onDragStart = { start ->
                                            val coordinates = drag.rightCoords[right]
                                            drag.draggingRight = right
                                            drag.offset.value = Offset.Zero
                                            drag.pointerGlobal =
                                                (coordinates?.positionInWindow() ?: Offset.Zero) + start
                                            drag.hoveredLeft = null
                                        },
                                        onDrag = { change, dragAmount ->
                                            change.consume()
                                            drag.offset.value += dragAmount
                                            drag.pointerGlobal += dragAmount
                                            val hovered =
                                                leftCoords.entries
                                                    .firstOrNull { (left, coordinates) ->
                                                        !matched.containsKey(left) &&
                                                            coordinates.boundsInWindow().contains(drag.pointerGlobal)
                                                    }?.key
                                            if (hovered != drag.hoveredLeft) drag.hoveredLeft = hovered
                                        },
                                        onDragEnd = {
                                            val target = drag.hoveredLeft
                                            if (target != null && !matched.containsKey(target)) {
                                                onSelectLeft(target)
                                                onMatchRight(right)
                                            }
                                            drag.reset()
                                        },
                                        onDragCancel = { drag.reset() },
                                    )
                                },
                    ) {
                        PuzzlePiece(
                            label = pair.right,
                            edge = PuzzleEdge.TAB,
                            status = OptionStatus.NEUTRAL,
                            selected = false,
                            enabled = enabled,
                            onClick = { onMatchRight(right) },
                            badge = pair.rightBadge,
                            detail = pair.rightDetail,
                        )
                    }
                }
            }
        }
    }
}

private enum class PuzzleEdge {
    SOCKET,
    TAB,
}

private val PUZZLE_TAB_DP = 12.dp

private class PuzzlePieceShape(
    private val edge: PuzzleEdge,
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density,
    ): Outline {
        val tab = with(density) { PUZZLE_TAB_DP.toPx() }
        val radius = tab * 0.8f
        val path = Path()
        when (edge) {
            PuzzleEdge.SOCKET -> {
                path.moveTo(0f, 0f)
                path.lineTo(size.width, 0f)
                path.lineTo(size.width, size.height / 2f - radius)
                path.cubicTo(
                    size.width - tab,
                    size.height / 2f - radius,
                    size.width - tab,
                    size.height / 2f + radius,
                    size.width,
                    size.height / 2f + radius,
                )
                path.lineTo(size.width, size.height)
                path.lineTo(0f, size.height)
                path.close()
            }
            PuzzleEdge.TAB -> {
                path.moveTo(tab, 0f)
                path.lineTo(size.width, 0f)
                path.lineTo(size.width, size.height)
                path.lineTo(tab, size.height)
                path.lineTo(tab, size.height / 2f + radius)
                path.cubicTo(
                    0f,
                    size.height / 2f + radius,
                    0f,
                    size.height / 2f - radius,
                    tab,
                    size.height / 2f - radius,
                )
                path.close()
            }
        }
        return Outline.Generic(path)
    }
}

@Composable
private fun PuzzlePiece(
    label: String,
    edge: PuzzleEdge,
    status: OptionStatus,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    badge: String? = null,
    detail: String? = null,
    hovered: Boolean = false,
) {
    val colors = MaterialTheme.colorScheme
    val shape = remember(edge) { PuzzlePieceShape(edge) }
    val background =
        when (status) {
            OptionStatus.CORRECT -> LinguaTokens.Accent.copy(alpha = 0.12f)
            OptionStatus.INCORRECT -> LinguaTokens.Destructive.copy(alpha = 0.12f)
            OptionStatus.NEUTRAL ->
                if (selected || hovered) {
                    colors.primary.copy(alpha = 0.08f)
                } else {
                    colors.surface
                }
        }
    val border =
        when (status) {
            OptionStatus.CORRECT -> LinguaTokens.Accent
            OptionStatus.INCORRECT -> LinguaTokens.Destructive
            OptionStatus.NEUTRAL -> if (selected || hovered) colors.primary else colors.outline
        }
    val state =
        when {
            status == OptionStatus.CORRECT -> "Pareja correcta"
            status == OptionStatus.INCORRECT -> "Pareja incorrecta"
            selected -> "Seleccionada"
            else -> "Sin seleccionar"
        }
    val horizontalPadding = PUZZLE_TAB_DP + 6.dp
    val contentColor =
        when (status) {
            OptionStatus.CORRECT -> LinguaTokens.Accent
            OptionStatus.INCORRECT -> LinguaTokens.Destructive
            OptionStatus.NEUTRAL -> colors.onSurface
        }

    Column(
        modifier =
            modifier
                .heightIn(min = 64.dp)
                .clip(shape)
                .background(background)
                .border(2.dp, border, shape)
                .clickable(enabled = enabled, onClick = onClick)
                .semantics(mergeDescendants = true) {
                    role = Role.Button
                    this.selected = selected
                    stateDescription = state
                }.padding(
                    start = if (edge == PuzzleEdge.TAB) horizontalPadding else 12.dp,
                    end = if (edge == PuzzleEdge.SOCKET) horizontalPadding else 12.dp,
                    top = 10.dp,
                    bottom = 10.dp,
                ),
        verticalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = contentColor,
                modifier = Modifier.weight(1f),
            )
            if (status != OptionStatus.NEUTRAL) {
                Image(
                    painter =
                        painterResource(
                            if (status == OptionStatus.CORRECT) LinguaIcons.CheckRes else LinguaIcons.CloseRes,
                        ),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    colorFilter = ColorFilter.tint(contentColor),
                )
            }
        }
        badge?.let { text ->
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                color = colors.primary,
            )
        }
        detail?.let { text ->
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant,
            )
        }
    }
}

private class PuzzleDragState {
    var draggingRight: Int? by mutableStateOf(null)
    var hoveredLeft: Int? by mutableStateOf(null)
    val offset = mutableStateOf(Offset.Zero)
    var pointerGlobal: Offset = Offset.Zero
    val rightCoords = mutableMapOf<Int, LayoutCoordinates>()

    fun reset() {
        draggingRight = null
        hoveredLeft = null
        offset.value = Offset.Zero
    }
}

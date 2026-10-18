package dev.linguago.core.ui.components

import android.content.Context
import android.view.accessibility.AccessibilityManager
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import dev.linguago.core.domain.logic.ClozePassageParser
import dev.linguago.core.domain.logic.ClozeSegment
import dev.linguago.core.ui.theme.LinguaTheme

/**
 * Tablero de cloze con arrastre (F2.3): el pasaje se pinta con `FlowRow` y
 * los huecos aceptan opciones arrastradas o colocadas por toque. El modo
 * táctil de equivalencia 1:1 es el camino natural con TalkBack. Durante el
 * arrastre no hay recomposiciones por frame: el delta del gesto solo se lee
 * en la fase de `graphicsLayer`, la colisión usa `LayoutCoordinates` y la
 * única recomposición es el resaltado del hueco objetivo (I8, I3).
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
public fun ClozeDragDropBoard(
    segments: List<ClozeSegment>,
    options: List<String>,
    filled: Map<String, String>,
    onAssign: (blankId: String, option: String) -> Unit,
    onClearBlank: (blankId: String) -> Unit,
    modifier: Modifier = Modifier,
    touchExplorationOverride: Boolean? = null,
) {
    val colors = MaterialTheme.colorScheme
    val tapMode = touchExplorationOverride ?: rememberTouchExplorationEnabled()
    val drag = remember(segments) { ClozeDragState() }
    val blankNumbers =
        remember(segments) {
            val numbers = mutableMapOf<String, Int>()
            var next = 1
            for (segment in segments) {
                if (segment is ClozeSegment.BlankReference) {
                    numbers[segment.id] = next
                    next += 1
                }
            }
            numbers.toMap()
        }
    var selectedOption by remember(options) { mutableStateOf<String?>(null) }
    val freeOptions = options.distinct().filterNot { option -> option in filled.values }

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            segments.forEach { segment ->
                when (segment) {
                    is ClozeSegment.Text ->
                        Text(
                            text = segment.value,
                            style = MaterialTheme.typography.bodyLarge,
                            color = colors.onBackground,
                        )
                    is ClozeSegment.BlankReference ->
                        ClozeBlankSlot(
                            number = blankNumbers[segment.id] ?: 0,
                            blankId = segment.id,
                            option = filled[segment.id],
                            isTarget = drag.target == segment.id,
                            register = { coordinates -> drag.blankCoords[segment.id] = coordinates },
                            onClick = {
                                val chosen = selectedOption
                                when {
                                    filled[segment.id] != null -> onClearBlank(segment.id)
                                    chosen != null -> {
                                        onAssign(segment.id, chosen)
                                        selectedOption = null
                                    }
                                }
                            },
                        )
                }
            }
        }
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            freeOptions.forEach { option ->
                ClozeOptionChip(
                    option = option,
                    selected = selectedOption == option,
                    dragging = drag.dragging == option,
                    drag = drag,
                    register = { coordinates -> drag.optionCoords[option] = coordinates },
                    onSelect = { selectedOption = if (selectedOption == option) null else option },
                    onAssigned = { blankId, dropped ->
                        onAssign(blankId, dropped)
                        selectedOption = null
                    },
                )
            }
        }
        Text(
            text = if (tapMode) "Toca una opción y luego el hueco" else "Arrastra las palabras a los huecos",
            style = MaterialTheme.typography.labelLarge,
            color = colors.onSurfaceVariant,
        )
    }
}

/** Estados efímeros del gesto de arrastre; sin lógica de negocio. */
private class ClozeDragState {
    /** Opción en arrastre; `null` fuera del gesto (observada por la UI). */
    var dragging: String? by mutableStateOf(null)

    /** Hueco bajo el puntero (observado para el resaltado). */
    var target: String? by mutableStateOf(null)

    /** Delta acumulado, leído solo en `graphicsLayer`. */
    val dragOffset = mutableStateOf(Offset.Zero)

    /** Posición global del puntero; mutable solo en callbacks del gesto. */
    var pointerGlobal: Offset = Offset.Zero

    /** Registro de coordenadas de huecos y opciones. */
    val blankCoords = mutableMapOf<String, LayoutCoordinates>()
    val optionCoords = mutableMapOf<String, LayoutCoordinates>()

    fun reset() {
        dragging = null
        target = null
        dragOffset.value = Offset.Zero
        pointerGlobal = Offset.Zero
    }
}

/**
 * Hueco del pasaje: muestra la opción colocada o un espacio vacío. Tap en
 * un hueco lleno lo libera; tap en uno vacío coloca la opción seleccionada.
 */
@Composable
private fun ClozeBlankSlot(
    number: Int,
    blankId: String,
    option: String?,
    isTarget: Boolean,
    register: (LayoutCoordinates) -> Unit,
    onClick: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val description = if (option == null) "Hueco $number vacío" else "Hueco $number: $option"
    Box(
        modifier =
            Modifier
                .onGloballyPositioned { coordinates -> register(coordinates) }
                .widthIn(min = 56.dp)
                .heightIn(min = 48.dp)
                .clip(RoundedCornerShape(8.dp))
                .border(
                    width = 1.dp,
                    color = if (isTarget) colors.primary else colors.outline,
                    shape = RoundedCornerShape(8.dp),
                ).background(
                    when {
                        option != null -> colors.secondaryContainer
                        isTarget -> colors.primary.copy(alpha = 0.12f)
                        else -> colors.surfaceVariant
                    },
                ).clickable(enabled = true, role = Role.Button, onClick = onClick)
                .semantics { contentDescription = description }
                .padding(horizontal = 12.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = option ?: "____",
            style = MaterialTheme.typography.bodyLarge,
            color = if (option == null) colors.onSurfaceVariant else colors.onSecondaryContainer,
        )
    }
}

/** Chip arrastrable: tap lo selecciona; arrastre lo coloca en un hueco. */
@Composable
private fun ClozeOptionChip(
    option: String,
    selected: Boolean,
    dragging: Boolean,
    drag: ClozeDragState,
    register: (LayoutCoordinates) -> Unit,
    onSelect: () -> Unit,
    onAssigned: (blankId: String, option: String) -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    Text(
        text = option,
        style = MaterialTheme.typography.bodyMedium,
        color =
            when {
                dragging || selected -> colors.onPrimary
                else -> colors.onSurface
            },
        modifier =
            Modifier
                .onGloballyPositioned { coordinates -> register(coordinates) }
                .zIndex(if (dragging) 1f else 0f)
                .graphicsLayer {
                    if (dragging) {
                        translationX = drag.dragOffset.value.x
                        translationY = drag.dragOffset.value.y
                    }
                }.clip(RoundedCornerShape(12.dp))
                .background(
                    when {
                        dragging || selected -> colors.primary
                        else -> colors.surface
                    },
                ).clickable { onSelect() }
                .semantics {
                    role = Role.Button
                    this.selected = selected
                }.padding(horizontal = 12.dp, vertical = 14.dp)
                .pointerInput(option) {
                    detectDragGestures(
                        onDragStart = { start ->
                            val coordinates = drag.optionCoords[option]
                            drag.dragging = option
                            drag.dragOffset.value = Offset.Zero
                            drag.pointerGlobal = (coordinates?.positionInWindow() ?: Offset.Zero) + start
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            drag.dragOffset.value += dragAmount
                            drag.pointerGlobal += dragAmount
                            val hit =
                                drag.blankCoords.entries.firstOrNull { (_, coordinates) ->
                                    coordinates.boundsInWindow().contains(drag.pointerGlobal)
                                }
                            val targetId = hit?.key
                            if (targetId != drag.target) drag.target = targetId
                        },
                        onDragEnd = {
                            val targetId = drag.target
                            if (targetId != null && drag.dragging == option) onAssigned(targetId, option)
                            drag.reset()
                        },
                        onDragCancel = { drag.reset() },
                    )
                },
    )
}

/**
 * `true` cuando TalkBack (exploración por toque) está activo: es la señal
 * para preferir el modo de toque frente al arrastre libre.
 */
@Composable
private fun rememberTouchExplorationEnabled(): Boolean {
    val context = LocalContext.current
    val manager = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
    val enabled = remember { mutableStateOf(manager.isTouchExplorationEnabled) }
    DisposableEffect(manager) {
        val listener =
            AccessibilityManager.TouchExplorationStateChangeListener { active ->
                enabled.value = active
            }
        manager.addTouchExplorationStateChangeListener(listener)
        onDispose { manager.removeTouchExplorationStateChangeListener(listener) }
    }
    return enabled.value
}

@Preview(showBackground = true)
@Composable
private fun ClozeDragDropBoardPreview() {
    LinguaTheme {
        ClozeDragDropBoard(
            segments = ClozePassageParser.parse("Deberíamos {{hueco}} la reunión."),
            options = listOf("reschedule", "cancel"),
            filled = emptyMap(),
            onAssign = { _, _ -> },
            onClearBlank = {},
        )
    }
}

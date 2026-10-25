package dev.linguago.feature.home.path

import dev.linguago.core.ui.components.path.PathNodeState

/**
 * Posiciones del zigzag del camino (ADR-026): patrón de cuatro posiciones
 * relativas al centro. Los cofres siempre van centrados para destacar.
 */
fun zigzagBias(nodeIndex: Int): Float =
    when (nodeIndex % ZIGZAG_PATTERN.size) {
        0 -> ZIGZAG_PATTERN[0]
        1 -> ZIGZAG_PATTERN[1]
        2 -> ZIGZAG_PATTERN[2]
        else -> ZIGZAG_PATTERN[3]
    }

/** Fila visual del camino: un elemento y las posiciones de su conector. */
data class PathRow(
    val item: PathItem,
    val bias: Float,
    val nextBias: Float?,
)

/** Construye las filas visuales con el zigzag ya resuelto. */
fun buildPathRows(items: List<PathItem>): List<PathRow> {
    val rows = mutableListOf<PathRow>()
    var nodeIndex = 0
    items.forEachIndexed { index, item ->
        if (item is PathItem.Node) {
            val bias = nodeBias(item, nodeIndex)
            val nextNode = items.getOrNull(index + 1) as? PathItem.Node
            rows += PathRow(item = item, bias = bias, nextBias = nextNode?.let { nodeBias(it, nodeIndex + 1) })
            nodeIndex++
        } else {
            rows += PathRow(item = item, bias = 0f, nextBias = null)
        }
    }
    return rows
}

/** Índice del primer nodo disponible/en progreso; si no hay, el último nodo. */
fun activeItemIndex(items: List<PathItem>): Int {
    val active =
        items.indexOfFirst { item ->
            item is PathItem.Node &&
                (item.node.state == PathNodeState.AVAILABLE || item.node.state == PathNodeState.IN_PROGRESS)
        }
    return if (active >= 0) active else items.indexOfLast { it is PathItem.Node }
}

internal fun nodeBias(
    item: PathItem.Node,
    nodeIndex: Int,
): Float =
    when (item.node.state) {
        PathNodeState.CHEST, PathNodeState.CHEST_LOCKED -> 0f
        else -> zigzagBias(nodeIndex)
    }

private val ZIGZAG_PATTERN = listOf(-0.24f, 0f, 0.24f, 0f)

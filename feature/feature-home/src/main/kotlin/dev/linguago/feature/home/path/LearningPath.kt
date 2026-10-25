package dev.linguago.feature.home.path

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import dev.linguago.core.ui.components.path.PathNode
import dev.linguago.core.ui.components.path.PathNodeState
import dev.linguago.core.ui.components.path.UnitHeader
import dev.linguago.core.ui.theme.LinguaTokens

/**
 * Camino lineal en zigzag (ADR-026): cabeceras de unidad, nodos flotantes y
 * conectores curvos. Se desplaza al nodo activo al abrir Home y mantiene la
 * posición del usuario entre recomposiciones. Los cofres se delegan en
 * [onChestClick] (la hoja de recompensa llega en la Etapa 6).
 */
@Composable
fun LearningPath(
    items: List<PathItem>,
    onLessonClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    onChestClick: (String) -> Unit = {},
) {
    val rows = remember(items) { buildPathRows(items) }
    val listState = rememberLazyListState()
    val activeIndex = activeItemIndex(items)

    LaunchedEffect(activeIndex, rows.size) {
        if (activeIndex >= 0) {
            listState.scrollToItem((activeIndex - 1).coerceAtLeast(0))
        }
    }

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        items(
            items = rows,
            key = { row -> rowKey(row.item) },
        ) { row ->
            when (val item = row.item) {
                is PathItem.Header ->
                    UnitHeader(
                        title = item.title,
                        completed = item.completed,
                        total = item.total,
                        modifier = Modifier.padding(top = 20.dp, bottom = 6.dp),
                    )
                is PathItem.Node -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Spacer(Modifier.weight((0.5f + row.bias).coerceAtLeast(0.06f)))
                            PathNode(
                                node = item.node,
                                onClick = { id ->
                                    val lessonId = item.lessonId
                                    if (lessonId != null) onLessonClick(lessonId) else onChestClick(id)
                                },
                            )
                            Spacer(Modifier.weight((0.5f - row.bias).coerceAtLeast(0.06f)))
                        }
                        val nextBias = row.nextBias
                        if (nextBias != null) {
                            PathConnector(
                                fromBias = row.bias,
                                toBias = nextBias,
                                color =
                                    if (item.node.state == PathNodeState.COMPLETED) {
                                        LinguaTokens.Accent
                                    } else {
                                        MaterialTheme.colorScheme.outline
                                    },
                            )
                        }
                    }
                }
            }
        }
    }
}

/** Conector curvo entre dos nodos; el color refleja si el tramo está hecho. */
@Composable
private fun PathConnector(
    fromBias: Float,
    toBias: Float,
    color: Color,
) {
    Canvas(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(26.dp),
    ) {
        val startX = size.width * (0.5f + fromBias)
        val endX = size.width * (0.5f + toBias)
        val path =
            Path().apply {
                moveTo(startX, 0f)
                cubicTo(
                    startX,
                    size.height * 0.55f,
                    endX,
                    size.height * 0.45f,
                    endX,
                    size.height,
                )
            }
        drawPath(
            path = path,
            color = color,
            style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round),
        )
    }
}

private fun rowKey(item: PathItem): String =
    when (item) {
        is PathItem.Header -> "header:${item.unitId}"
        is PathItem.Node -> item.node.id
    }

package dev.linguago.feature.home.path

import dev.linguago.core.ui.components.path.PathNodeState
import dev.linguago.core.ui.components.path.PathNodeUi
import org.junit.Assert.assertEquals
import org.junit.Test

/** Layout del camino (ADR-026): zigzag determinista y auto-scroll al activo. */
class PathLayoutTest {
    @Test
    fun `el zigzag alterna cuatro posiciones`() {
        assertEquals(-0.24f, zigzagBias(0))
        assertEquals(0f, zigzagBias(1))
        assertEquals(0.24f, zigzagBias(2))
        assertEquals(0f, zigzagBias(3))
        assertEquals(-0.24f, zigzagBias(4))
    }

    @Test
    fun `los cofres siempre van centrados`() {
        val rows =
            buildPathRows(
                listOf(
                    node("l-1", PathNodeState.AVAILABLE),
                    node("chest:u-1", PathNodeState.CHEST_LOCKED),
                    node("l-2", PathNodeState.LOCKED),
                ),
            )

        assertEquals(0f, rows[1].bias)
    }

    @Test
    fun `las filas encadenan la posicion del siguiente nodo`() {
        val rows =
            buildPathRows(
                listOf(
                    node("l-1", PathNodeState.AVAILABLE),
                    node("chest:u-1", PathNodeState.CHEST_LOCKED),
                    node("l-2", PathNodeState.LOCKED),
                ),
            )

        assertEquals(0f, rows[0].nextBias)
        assertEquals(0.24f, rows[1].nextBias)
        assertEquals(null, rows[2].nextBias)
    }

    @Test
    fun `las cabeceras no alteran el indice de nodos`() {
        val rows =
            buildPathRows(
                listOf(
                    header(),
                    node("l-1", PathNodeState.AVAILABLE),
                    node("l-2", PathNodeState.LOCKED),
                ),
            )

        assertEquals(0f, rows[0].bias)
        assertEquals(-0.24f, rows[1].bias)
        assertEquals(0f, rows[2].bias)
    }

    @Test
    fun `el nodo activo es el primero disponible o en progreso`() {
        val items =
            listOf(
                header(),
                node("l-1", PathNodeState.COMPLETED),
                node("l-2", PathNodeState.IN_PROGRESS),
                node("l-3", PathNodeState.LOCKED),
            )

        assertEquals(2, activeItemIndex(items))
    }

    @Test
    fun `sin nodos activos se usa el ultimo nodo`() {
        val items =
            listOf(
                header(),
                node("l-1", PathNodeState.COMPLETED),
                node("l-2", PathNodeState.COMPLETED),
            )

        assertEquals(2, activeItemIndex(items))
    }

    @Test
    fun `lista vacia no tiene nodo activo`() {
        assertEquals(-1, activeItemIndex(emptyList()))
    }

    private fun header(): PathItem.Header = PathItem.Header("u-1", "Unidad 1", completed = 0, total = 2)

    private fun node(
        id: String,
        state: PathNodeState,
    ): PathItem.Node =
        PathItem.Node(
            node = PathNodeUi(id = id, label = id, state = state),
            lessonId = if (id.startsWith("chest:")) null else id,
        )
}

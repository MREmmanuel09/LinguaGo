package dev.linguago.feature.home.path

import dev.linguago.core.domain.model.Course
import dev.linguago.core.domain.model.CourseId
import dev.linguago.core.domain.model.Language
import dev.linguago.core.domain.model.Lesson
import dev.linguago.core.domain.model.LessonId
import dev.linguago.core.domain.model.LessonProgress
import dev.linguago.core.domain.model.Level
import dev.linguago.core.domain.model.LevelId
import dev.linguago.core.domain.model.Unit
import dev.linguago.core.domain.model.UnitId
import dev.linguago.core.ui.components.path.PathNodeState
import dev.linguago.core.ui.components.path.PathNodeUi
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Adaptador del camino lineal (ADR-026): estados, desbloqueo y cofres. */
class PathItemsTest {
    @Test
    fun `curso sin progreso deja la primera leccion disponible y el resto bloqueado`() {
        val items = buildPathItems(course(unit("u-1", "l-1", "l-2"), unit("u-2", "l-3")), emptyList())

        val nodes = nodes(items)
        assertEquals(listOf("l-1", "l-2", "chest:u-1", "l-3", "chest:u-2"), nodes.map { it.id })
        assertEquals(PathNodeState.AVAILABLE, nodes[0].state)
        assertEquals(PathNodeState.LOCKED, nodes[1].state)
        assertEquals(PathNodeState.CHEST_LOCKED, nodes[2].state)
        assertEquals(PathNodeState.LOCKED, nodes[3].state)
        assertEquals(PathNodeState.CHEST_LOCKED, nodes[4].state)

        val headers = headers(items)
        assertEquals(2, headers.size)
        assertEquals(0, headers[0].completed)
        assertEquals(2, headers[0].total)
    }

    @Test
    fun `completar la primera desbloquea la segunda`() {
        val items = buildPathItems(course(unit("u-1", "l-1", "l-2")), listOf(progress("l-1", completed = true)))

        val nodes = nodes(items)
        assertEquals(PathNodeState.COMPLETED, nodes[0].state)
        assertEquals(PathNodeState.AVAILABLE, nodes[1].state)
        assertEquals(PathNodeState.CHEST_LOCKED, nodes[2].state)
    }

    @Test
    fun `completar la unidad abre el cofre y la siguiente unidad`() {
        val items =
            buildPathItems(
                course(unit("u-1", "l-1", "l-2"), unit("u-2", "l-3")),
                listOf(progress("l-1", completed = true), progress("l-2", completed = true)),
            )

        val nodes = nodes(items)
        assertEquals(PathNodeState.CHEST, nodes[2].state)
        assertEquals(PathNodeState.AVAILABLE, nodes[3].state)
        assertEquals(2, headers(items)[0].completed)
        assertEquals(2, headers(items)[0].total)
    }

    @Test
    fun `completadas fuera de orden siguen clicables y los huecos se bloquean`() {
        val items =
            buildPathItems(
                course(unit("u-1", "l-1", "l-2", "l-3", "l-4")),
                listOf(progress("l-1", completed = true), progress("l-3", completed = true)),
            )

        val nodes = nodes(items)
        assertEquals(PathNodeState.COMPLETED, nodes[0].state)
        assertEquals(PathNodeState.AVAILABLE, nodes[1].state)
        assertEquals(PathNodeState.COMPLETED, nodes[2].state)
        assertEquals(PathNodeState.LOCKED, nodes[3].state)
    }

    @Test
    fun `intentos sin completar marcan la leccion en progreso`() {
        val items = buildPathItems(course(unit("u-1", "l-1", "l-2")), listOf(progress("l-1", attempts = 2)))

        val nodes = nodes(items)
        assertEquals(PathNodeState.IN_PROGRESS, nodes[0].state)
        assertEquals(PathNodeState.LOCKED, nodes[1].state)
    }

    @Test
    fun `los cofres no tienen leccion asociada`() {
        val items = buildPathItems(course(unit("u-1", "l-1")), emptyList())

        val chests = items.filterIsInstance<PathItem.Node>().filter { it.lessonId == null }
        assertEquals(1, chests.size)
        assertEquals("chest:u-1", chests.single().node.id)
        assertTrue(items.filterIsInstance<PathItem.Node>().any { it.lessonId == "l-1" })
    }

    @Test
    fun `curso vacio no produce items`() {
        assertTrue(buildPathItems(course(), emptyList()).isEmpty())
    }

    @Test
    fun `nivel visual deriva del xp total`() {
        assertEquals(XpLevel(level = 1, progressToNext = 0f), xpLevelFor(0))
        assertEquals(XpLevel(level = 1, progressToNext = 0.5f), xpLevelFor(50))
        assertEquals(XpLevel(level = 2, progressToNext = 0f), xpLevelFor(100))
        assertEquals(XpLevel(level = 3, progressToNext = 0.5f), xpLevelFor(250))
        assertEquals(XpLevel(level = 1, progressToNext = 0f), xpLevelFor(-10))
    }

    private fun lesson(id: String): Lesson = Lesson(id = LessonId(id), title = "Lección $id", exercises = emptyList())

    private fun unit(
        id: String,
        vararg lessonIds: String,
    ): Unit = Unit(id = UnitId(id), title = "Unidad $id", lessons = lessonIds.map(::lesson))

    private fun course(vararg units: Unit): Course =
        Course(
            id = CourseId("es-en"),
            fromLanguage = Language.SPANISH,
            toLanguage = Language.ENGLISH,
            levels = listOf(Level(id = LevelId("lv-1"), name = "Nivel", units = units.toList())),
        )

    private fun progress(
        lessonId: String,
        completed: Boolean = false,
        attempts: Int = 0,
    ): LessonProgress =
        LessonProgress(
            lessonId = LessonId(lessonId),
            completed = completed,
            xp = if (completed) 10 else 0,
            attempts = attempts,
        )

    private fun nodes(items: List<PathItem>): List<PathNodeUi> = items.filterIsInstance<PathItem.Node>().map { it.node }

    private fun headers(items: List<PathItem>): List<PathItem.Header> = items.filterIsInstance<PathItem.Header>()
}

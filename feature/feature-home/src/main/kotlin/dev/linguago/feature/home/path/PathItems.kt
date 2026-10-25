package dev.linguago.feature.home.path

import dev.linguago.core.domain.model.Course
import dev.linguago.core.domain.model.LessonProgress
import dev.linguago.core.ui.components.path.PathNodeState
import dev.linguago.core.ui.components.path.PathNodeUi

/**
 * Elemento del camino lineal (ADR-026): cabecera de unidad o nodo
 * interactivo. La UI de la Etapa 5 lo renderiza en un `LazyColumn`.
 */
sealed interface PathItem {
    /** Cabecera de unidad con su avance. */
    data class Header(
        val unitId: String,
        val title: String,
        val completed: Int,
        val total: Int,
    ) : PathItem

    /** Nodo del camino; [lessonId] es `null` para los cofres. */
    data class Node(
        val node: PathNodeUi,
        val lessonId: String?,
    ) : PathItem
}

/** Nivel visual derivado del XP total; no se persiste (ADR-026). */
data class XpLevel(
    val level: Int,
    val progressToNext: Float,
)

/** XP necesaria para subir de nivel visual. */
const val XP_PER_LEVEL: Int = 100

/** Calcula el nivel visual y el avance hacia el siguiente. */
fun xpLevelFor(totalXp: Int): XpLevel {
    val safe = totalXp.coerceAtLeast(0)
    return XpLevel(
        level = safe / XP_PER_LEVEL + 1,
        progressToNext = (safe % XP_PER_LEVEL) / XP_PER_LEVEL.toFloat(),
    )
}

/**
 * Convierte curso + progreso en el camino lineal (ADR-026). El orden del
 * curso es la rampa de dificultad A1→B2; el desbloqueo es secuencial sobre el
 * camino aplanado y las lecciones completadas siempre son rejugables. Tras
 * cada unidad se inserta un cofre independiente, bloqueado hasta completarla.
 */
fun buildPathItems(
    course: Course,
    lessonProgress: List<LessonProgress>,
): List<PathItem> {
    val completedIds = lessonProgress.filter { it.completed }.map { it.lessonId.value }.toSet()
    val attemptsById = lessonProgress.associate { it.lessonId.value to it.attempts }
    val items = mutableListOf<PathItem>()
    var unlocked = true
    course.levels.forEach { level ->
        level.units.forEach { unit ->
            val completedInUnit = unit.lessons.count { it.id.value in completedIds }
            items += PathItem.Header(unit.id.value, unit.title, completedInUnit, unit.lessons.size)
            unit.lessons.forEach { lesson ->
                val id = lesson.id.value
                val state =
                    when {
                        id in completedIds -> PathNodeState.COMPLETED
                        unlocked ->
                            if ((attemptsById[id] ?: 0) > 0) {
                                PathNodeState.IN_PROGRESS
                            } else {
                                PathNodeState.AVAILABLE
                            }
                        else -> PathNodeState.LOCKED
                    }
                items +=
                    PathItem.Node(
                        node = PathNodeUi(id = id, label = lesson.title, state = state),
                        lessonId = id,
                    )
                if (id !in completedIds) unlocked = false
            }
            val unitComplete = unit.lessons.isNotEmpty() && completedInUnit == unit.lessons.size
            items +=
                PathItem.Node(
                    node =
                        PathNodeUi(
                            id = "chest:${unit.id.value}",
                            label = unit.title,
                            state = if (unitComplete) PathNodeState.CHEST else PathNodeState.CHEST_LOCKED,
                        ),
                    lessonId = null,
                )
        }
    }
    return items
}

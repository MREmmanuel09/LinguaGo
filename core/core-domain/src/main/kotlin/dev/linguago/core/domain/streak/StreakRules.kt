package dev.linguago.core.domain.streak

import dev.linguago.core.domain.model.StreakState
import java.time.LocalDate

/**
 * Reglas de la racha diaria (invariante I3: sin fricción — no hay
 * penalizaciones ni vidas). Asistir mantiene la racha solo si hubo actividad
 * ayer o hoy; un hueco de más de 1 día la resetea. Lógica pura, 100% testeable.
 */
public object StreakRules {
    /**
     * Calcula el estado de racha tras completar una lección en [today],
     * partiendo de [current].
     */
    public fun computeOnLessonCompletion(
        current: StreakState,
        today: LocalDate,
    ): StreakState {
        val yesterday = today.minusDays(1)

        val streakValue =
            when (current.lastDate) {
                today -> current.current
                yesterday -> current.current + 1
                else -> 1
            }

        return current.copy(
            current = streakValue,
            longest = maxOf(current.longest, streakValue),
            lastDate = today,
        )
    }
}

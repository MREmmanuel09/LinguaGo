package dev.linguago.core.domain.streak

import dev.linguago.core.domain.model.StreakState
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class StreakRulesTest {
    private val today: LocalDate = LocalDate.of(2026, 10, 16)

    @Test
    fun nueva_racha_si_lleva_mas_de_un_dia_inactivo() {
        val state =
            StreakState(
                current = 5,
                longest = 9,
                lastDate = today.minusDays(3),
                totalXp = 100,
            )

        val result = StreakRules.computeOnLessonCompletion(state, today)

        assertEquals(1, result.current)
        assertEquals(9, result.longest)
        assertEquals(today, result.lastDate)
        assertEquals(100, result.totalXp)
    }

    @Test
    fun incrementa_si_actividad_ayer() {
        val state =
            StreakState(
                current = 4,
                longest = 4,
                lastDate = today.minusDays(1),
                totalXp = 80,
            )

        val result = StreakRules.computeOnLessonCompletion(state, today)

        assertEquals(5, result.current)
        assertEquals(5, result.longest)
    }

    @Test
    fun estable_si_ya_hubo_actividad_hoy() {
        val state =
            StreakState(
                current = 7,
                longest = 7,
                lastDate = today,
                totalXp = 140,
            )

        val result = StreakRules.computeOnLessonCompletion(state, today)

        assertEquals(7, result.current)
        assertEquals(7, result.longest)
        assertEquals(today, result.lastDate)
    }

    @Test
    fun longest_actualiza_si_rompe_record_personal() {
        val state =
            StreakState(
                current = 11,
                longest = 10,
                lastDate = today.minusDays(1),
                totalXp = 220,
            )

        val result = StreakRules.computeOnLessonCompletion(state, today)

        assertEquals(12, result.current)
        assertEquals(12, result.longest)
    }
}

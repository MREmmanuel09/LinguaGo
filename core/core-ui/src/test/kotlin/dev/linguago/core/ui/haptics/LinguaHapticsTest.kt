package dev.linguago.core.ui.haptics

import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class LinguaHapticsTest {
    private val recorded = mutableListOf<HapticFeedbackType>()

    private val haptics =
        LinguaHaptics(
            hapticFeedback =
                object : HapticFeedback {
                    override fun performHapticFeedback(hapticFeedbackType: HapticFeedbackType) {
                        recorded += hapticFeedbackType
                    }
                },
        )

    @Test
    fun `confirm emite un unico pulso de confirmacion`() {
        haptics.confirm()

        assertEquals(listOf(HapticFeedbackType.Confirm), recorded)
    }

    @Test
    fun `tick emite un pulso ligero`() {
        haptics.tick()

        assertEquals(listOf(HapticFeedbackType.TextHandleMove), recorded)
    }

    @Test
    fun `error emite dos pulsos de rechazo`() {
        runBlocking { haptics.error() }

        assertEquals(
            listOf(HapticFeedbackType.Reject, HapticFeedbackType.Reject),
            recorded,
        )
    }
}

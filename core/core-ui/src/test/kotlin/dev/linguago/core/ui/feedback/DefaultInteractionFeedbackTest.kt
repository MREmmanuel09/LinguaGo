package dev.linguago.core.ui.feedback

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DefaultInteractionFeedbackTest {
    private val playedSounds = mutableListOf<FeedbackCue>()
    private val playedHaptics = mutableListOf<FeedbackCue>()

    private val sounds =
        object : SoundEffectPlayer {
            override fun play(cue: FeedbackCue) {
                playedSounds += cue
            }

            override fun release() = Unit
        }

    private val haptics =
        object : HapticFeedbackPlayer {
            override fun play(cue: FeedbackCue) {
                playedHaptics += cue
            }
        }

    @Test
    fun `emite sonido y haptica cuando ambos ajustes estan activos`() {
        val feedback =
            DefaultInteractionFeedback(
                sounds = sounds,
                haptics = haptics,
                settings = FeedbackSettings(soundEffectsEnabled = true, hapticsEnabled = true),
            )

        feedback.play(FeedbackCue.CORRECT)

        assertEquals(listOf(FeedbackCue.CORRECT), playedSounds)
        assertEquals(listOf(FeedbackCue.CORRECT), playedHaptics)
    }

    @Test
    fun `el ajuste de sonido apagado no silencia la haptica`() {
        val feedback =
            DefaultInteractionFeedback(
                sounds = sounds,
                haptics = haptics,
                settings = FeedbackSettings(soundEffectsEnabled = false, hapticsEnabled = true),
            )

        feedback.play(FeedbackCue.INCORRECT)

        assertTrue(playedSounds.isEmpty())
        assertEquals(listOf(FeedbackCue.INCORRECT), playedHaptics)
    }

    @Test
    fun `el ajuste de haptica apagado no silencia el sonido`() {
        val feedback =
            DefaultInteractionFeedback(
                sounds = sounds,
                haptics = haptics,
                settings = FeedbackSettings(soundEffectsEnabled = true, hapticsEnabled = false),
            )

        feedback.play(FeedbackCue.SNAP)

        assertEquals(listOf(FeedbackCue.SNAP), playedSounds)
        assertTrue(playedHaptics.isEmpty())
    }

    @Test
    fun `ambos ajustes apagados no emiten nada`() {
        val feedback =
            DefaultInteractionFeedback(
                sounds = sounds,
                haptics = haptics,
                settings = FeedbackSettings(soundEffectsEnabled = false, hapticsEnabled = false),
            )

        feedback.play(FeedbackCue.COMPLETE)

        assertTrue(playedSounds.isEmpty())
        assertTrue(playedHaptics.isEmpty())
    }
}

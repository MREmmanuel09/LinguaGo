package dev.linguago.core.ui.feedback

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import dev.linguago.core.ui.R

/** Reproductor de SFX; se abstrae para poder probar el despacho de cues. */
internal interface SoundEffectPlayer {
    fun play(cue: FeedbackCue)

    fun release()
}

/**
 * Reproductor basado en `SoundPool` (ADR-027): un único pool en memoria,
 * precargado y liberado al salir de composición. Los sonidos son originales
 * de LinguaGo y viven en `res/raw`.
 */
internal class SoundPoolSoundEffectPlayer(
    context: Context,
) : SoundEffectPlayer {
    private val appContext = context.applicationContext
    private val soundPool: SoundPool =
        SoundPool
            .Builder()
            .setMaxStreams(MAX_STREAMS)
            .setAudioAttributes(
                AudioAttributes
                    .Builder()
                    .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build(),
            ).build()

    private val samples: Map<FeedbackCue, Int> =
        mapOf(
            FeedbackCue.TAP to load(R.raw.sfx_tap),
            FeedbackCue.SELECT to load(R.raw.sfx_select),
            FeedbackCue.DESELECT to load(R.raw.sfx_deselect),
            FeedbackCue.SNAP to load(R.raw.sfx_snap),
            FeedbackCue.CORRECT to load(R.raw.sfx_correct),
            FeedbackCue.INCORRECT to load(R.raw.sfx_incorrect),
            FeedbackCue.COMPLETE to load(R.raw.sfx_complete),
        )

    @Volatile
    private var released = false

    override fun play(cue: FeedbackCue) {
        if (released) return
        val sampleId = samples[cue] ?: return
        if (sampleId == 0) return
        soundPool.play(sampleId, VOLUME, VOLUME, PRIORITY, NO_LOOP, NORMAL_RATE)
    }

    override fun release() {
        if (released) return
        released = true
        soundPool.release()
    }

    private fun load(resourceId: Int): Int = soundPool.load(appContext, resourceId, 1)

    private companion object {
        const val MAX_STREAMS = 4
        const val VOLUME = 0.45f
        const val PRIORITY = 1
        const val NO_LOOP = 0
        const val NORMAL_RATE = 1f
    }
}

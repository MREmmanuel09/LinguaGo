package dev.linguago.core.domain.logic

import dev.linguago.core.domain.model.Exercise

/**
 * Reglas de reintento pedagógico y fairness de vidas/XP (ADR-030). Se
 * separan del ViewModel para poder probarlas sin Android ni UI.
 */
public object RetryPolicy {
    /** Reintentos permitidos por ejercicio (el primero es el intento normal). */
    public const val MAX_RETRIES: Int = 1

    /** Solo los ejercicios de escucha y producción admiten reintento. */
    public fun supportsRetry(exercise: Exercise): Boolean =
        when (exercise) {
            is Exercise.ListenChoose,
            is Exercise.AudioOnlyPrompt,
            is Exercise.ListenType,
            is Exercise.SpeakRepeat,
            is Exercise.FreeSpeakingPrompt,
            -> true
            else -> false
        }

    /** `true` si el feedback actual debe ofrecer "Reintentar". */
    public fun canRetry(
        exercise: Exercise,
        isCorrect: Boolean,
        retryCount: Int,
        allowRetry: Boolean = true,
    ): Boolean = allowRetry && !isCorrect && supportsRetry(exercise) && retryCount < MAX_RETRIES

    /** Una vida se solicita al primer fallo del ejercicio, no en los reintentos. */
    public fun shouldRequestHeartConsumption(
        consumeHeart: Boolean,
        alreadyConsumed: Boolean,
    ): Boolean = consumeHeart && !alreadyConsumed

    /** El XP de un ejercicio se otorga como máximo una vez, incluso al reintentar. */
    public fun shouldAwardXp(
        isCorrect: Boolean,
        alreadyAwarded: Boolean,
    ): Boolean = isCorrect && !alreadyAwarded
}

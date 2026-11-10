package dev.linguago.feature.review

/** Progreso hacia la siguiente vida recuperada en el repaso (ADR-048). */
internal data class HeartRecoveryProgress(
    val progress: Int,
    val recovered: Boolean,
)

/**
 * Reglas puras de recuperación activa: cada 5 tarjetas superadas (todo salvo
 * «Otra vez») se recupera 1 vida. El contador se reinicia al completar el ciclo.
 */
internal object ReviewHeartRules {
    internal const val REVIEWS_PER_HEART: Int = 5

    fun afterPassed(progress: Int): HeartRecoveryProgress {
        val next = progress.coerceAtLeast(0) + 1
        return if (next >= REVIEWS_PER_HEART) {
            HeartRecoveryProgress(progress = 0, recovered = true)
        } else {
            HeartRecoveryProgress(progress = next, recovered = false)
        }
    }
}

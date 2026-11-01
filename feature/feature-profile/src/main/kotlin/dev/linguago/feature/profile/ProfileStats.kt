package dev.linguago.feature.profile

/** Cálculos de nivel visual del perfil (ADR-035); no se persisten. */
internal object ProfileStats {
    const val XP_PER_LEVEL: Int = 100

    /** Nivel visual derivado del XP total (mismo criterio que el camino). */
    fun levelFor(totalXp: Int): Int = totalXp.coerceAtLeast(0) / XP_PER_LEVEL + 1

    /** Avance (0..1) hacia el siguiente nivel visual. */
    fun xpIntoLevelFor(totalXp: Int): Float {
        val safe = totalXp.coerceAtLeast(0)
        return (safe % XP_PER_LEVEL) / XP_PER_LEVEL.toFloat()
    }
}

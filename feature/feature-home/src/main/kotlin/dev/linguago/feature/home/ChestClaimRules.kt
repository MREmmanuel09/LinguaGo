package dev.linguago.feature.home

import dev.linguago.core.ui.components.path.PathNodeState

/**
 * Regla pura del bono de cofre (ADR-048): solo se otorga la primera vez que
 * se abre un cofre de una unidad completada.
 */
internal object ChestClaimRules {
    fun shouldAward(
        state: PathNodeState,
        alreadyOpened: Boolean,
        claimedThisSession: Boolean,
    ): Boolean = state == PathNodeState.CHEST && !alreadyOpened && !claimedThisSession
}

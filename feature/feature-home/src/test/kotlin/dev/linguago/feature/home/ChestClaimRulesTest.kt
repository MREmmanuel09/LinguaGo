package dev.linguago.feature.home

import dev.linguago.core.ui.components.path.PathNodeState
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** El bono de cofre se otorga una sola vez (ADR-048). */
class ChestClaimRulesTest {
    @Test
    fun `un cofre completado sin abrir otorga el bono`() {
        assertTrue(ChestClaimRules.shouldAward(PathNodeState.CHEST, alreadyOpened = false, claimedThisSession = false))
    }

    @Test
    fun `un cofre ya reclamado no vuelve a otorgar`() {
        assertFalse(ChestClaimRules.shouldAward(PathNodeState.CHEST, alreadyOpened = true, claimedThisSession = false))
        assertFalse(ChestClaimRules.shouldAward(PathNodeState.CHEST, alreadyOpened = false, claimedThisSession = true))
    }

    @Test
    fun `un cofre bloqueado no otorga nada`() {
        assertFalse(ChestClaimRules.shouldAward(PathNodeState.CHEST_LOCKED, alreadyOpened = false, claimedThisSession = false))
    }
}

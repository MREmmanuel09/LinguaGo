package dev.linguago.feature.profile

import org.junit.Assert.assertEquals
import org.junit.Test

class ProfileStatsTest {
    @Test
    fun `nivel y avance derivan del xp total`() {
        assertEquals(1, ProfileStats.levelFor(0))
        assertEquals(1, ProfileStats.levelFor(99))
        assertEquals(2, ProfileStats.levelFor(100))
        assertEquals(3, ProfileStats.levelFor(250))
        assertEquals(1, ProfileStats.levelFor(-10))

        assertEquals(0f, ProfileStats.xpIntoLevelFor(0), 0.0001f)
        assertEquals(0.5f, ProfileStats.xpIntoLevelFor(50), 0.0001f)
        assertEquals(0.5f, ProfileStats.xpIntoLevelFor(250), 0.0001f)
    }
}

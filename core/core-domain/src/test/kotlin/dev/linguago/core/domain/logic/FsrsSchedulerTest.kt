package dev.linguago.core.domain.logic

import dev.linguago.core.domain.model.WordProgress
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

/** Programador FSRS-4.5 (ADR-016): propiedades de agenda y rangos. */
public class FsrsSchedulerTest {
    private val now: Instant = Instant.parse("2026-01-01T00:00:00Z")

    @Test
    public fun `tarjeta nueva se debe de inmediato y sin repasos`() {
        val card = initial()
        assertEquals(0, card.reviewCount)
        assertEquals(now, card.nextReview)
        assertTrue(card.stability > 0f)
        assertTrue(card.difficulty in 1f..10f)
    }

    @Test
    public fun `good en primera revisión progresa y se agenda a futuro`() {
        val outcome = FsrsScheduler.grade(initial(), ReviewRating.GOOD, now)

        assertEquals(1, outcome.progress.reviewCount)
        assertEquals(now, outcome.progress.lastReview)
        assertTrue(outcome.progress.nextReview.isAfter(now))
        assertTrue(outcome.intervalDays >= 1)
    }

    @Test
    public fun `again vuelve a deberse en un día`() {
        val outcome = FsrsScheduler.grade(initial(), ReviewRating.AGAIN, now)

        assertEquals(1, outcome.intervalDays)
        assertTrue(outcome.progress.stability > 0f)
    }

    @Test
    public fun `easy agenda más lejos que good`() {
        val mature =
            WordProgress(
                word = "hermano",
                translation = "brother",
                courseId = "es-en",
                reviewCount = 3,
                lastReview = now.minusSeconds(10L * 86_400L),
                nextReview = now,
                stability = 10f,
                difficulty = 5f,
            )

        val good = FsrsScheduler.grade(mature, ReviewRating.GOOD, now)
        val easy = FsrsScheduler.grade(mature, ReviewRating.EASY, now)

        assertTrue(easy.progress.stability > good.progress.stability)
        assertTrue(easy.intervalDays >= good.intervalDays)
    }

    @Test
    public fun `la dificultad nunca sale del rango 1 a 10`() {
        var card = initial()
        repeat(20) {
            card = FsrsScheduler.grade(card, ReviewRating.AGAIN, now).progress
        }
        assertTrue(card.difficulty in 1f..10f)
        assertTrue(card.stability > 0f)
    }

    @Test
    public fun `la retención estimada está en 0 a 1`() {
        val retrievability = FsrsScheduler.retrievability(5f, 10f)
        assertTrue(retrievability in 0f..1f)
        assertEquals(1f, FsrsScheduler.retrievability(0f, 10f), 0.001f)
        assertTrue(FsrsScheduler.retrievability(30f, 10f) < retrievability)
    }

    private fun initial(): WordProgress =
        FsrsScheduler.initialState(
            word = "hermano",
            translation = "brother",
            courseId = "es-en",
            now = now,
        )
}

package dev.linguago.core.domain.logic

import dev.linguago.core.domain.model.WordProgress
import java.time.Instant
import java.time.temporal.ChronoUnit
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.roundToInt

/** Calificación del usuario en el repaso (FSRS: 1..4). */
public enum class ReviewRating {
    /** Otra vez: no recordó la palabra. */
    AGAIN,

    /** Difícil: recordó con esfuerzo. */
    HARD,

    /** Bien: recordó correctamente. */
    GOOD,

    /** Fácil: recordó sin esfuerzo. */
    EASY,
}

/** Resultado de calificar una tarjeta: progreso nuevo + intervalo aplicado. */
public data class ReviewOutcome(
    val progress: WordProgress,
    val intervalDays: Int,
)

/**
 * Programador de repaso espaciado FSRS-4.5 (ADR-016): algoritmo puro con los
 * 17 parámetros por defecto, retención objetivo del 90% y estados
 * stability/difficulty. Sin dependencias de Android: testeable en JVM (I7).
 *
 * La primera calificación usa la estabilidad inicial por rating; a partir de
 * la segunda se aplican las fórmulas de crecimiento (éxito) o re-aprendizaje
 * (fallo). "Otra vez" vuelve a deberse en 1 día.
 */
public object FsrsScheduler {
    private const val GOOD = 3
    private const val AGAIN = 1
    private const val HARD = 2
    private const val EASY = 4

    private const val DECAY = -0.5f
    private const val FACTOR = 19f / 81f
    private const val TARGET_RETENTION = 0.9f
    private const val MIN_STABILITY = 0.01f
    private const val MIN_DIFFICULTY = 1f
    private const val MAX_DIFFICULTY = 10f
    private const val AGAIN_INTERVAL_DAYS = 1
    private const val MILLIS_PER_DAY = 86_400_000.0

    // Pesos FSRS-4.5 por defecto (w0..w16).
    private val W =
        floatArrayOf(
            0.4872f,
            1.4003f,
            3.7145f,
            13.8206f,
            5.1618f,
            1.2298f,
            0.8975f,
            0.031f,
            1.6474f,
            0.1367f,
            1.0461f,
            2.1072f,
            0.0793f,
            0.3246f,
            1.587f,
            0.2272f,
            2.8755f,
        )

    /** Tarjeta nueva: due ahora, con estado inicial "Bien". */
    public fun initialState(
        word: String,
        translation: String,
        courseId: String,
        now: Instant,
    ): WordProgress =
        WordProgress(
            word = word,
            translation = translation,
            courseId = courseId,
            reviewCount = 0,
            lastReview = now,
            nextReview = now,
            stability = initialStability(GOOD),
            difficulty = initialDifficulty(GOOD),
        )

    /** Aplica una calificación y calcula la próxima fecha de repaso. */
    public fun grade(
        current: WordProgress,
        rating: ReviewRating,
        now: Instant,
    ): ReviewOutcome {
        val grade = rating.ordinal + 1
        val isFirstReview = current.reviewCount == 0
        val elapsedDays =
            max(
                0f,
                (ChronoUnit.MILLIS.between(current.lastReview, now) / MILLIS_PER_DAY).toFloat(),
            )
        val retrievability = retrievability(elapsedDays, current.stability)
        val difficulty =
            if (isFirstReview) {
                initialDifficulty(grade)
            } else {
                nextDifficulty(current.difficulty, grade)
            }
        val stability =
            when {
                isFirstReview -> initialStability(grade)
                grade == AGAIN -> failedStability(current.stability, difficulty, retrievability)
                else -> grownStability(current.stability, difficulty, retrievability, grade)
            }
        val intervalDays = intervalDays(stability, grade)
        val nextReview = now.plus(intervalDays.toLong(), ChronoUnit.DAYS)
        return ReviewOutcome(
            progress =
                current.copy(
                    reviewCount = current.reviewCount + 1,
                    lastReview = now,
                    nextReview = nextReview,
                    stability = stability,
                    difficulty = difficulty,
                ),
            intervalDays = intervalDays,
        )
    }

    /** Retención estimada tras `elapsedDays` con la estabilidad actual. */
    public fun retrievability(
        elapsedDays: Float,
        stability: Float,
    ): Float = (1f + FACTOR * (elapsedDays / stability)).pow(DECAY).coerceIn(0f, 1f)

    private fun initialStability(grade: Int): Float = W[grade - 1].coerceAtLeast(MIN_STABILITY)

    private fun initialDifficulty(grade: Int): Float = (W[4] - (grade - 3) * W[5]).coerceIn(MIN_DIFFICULTY, MAX_DIFFICULTY)

    private fun nextDifficulty(
        difficulty: Float,
        grade: Int,
    ): Float {
        val delta = difficulty - W[6] * (grade - 3)
        val reverted = W[7] * W[4] + (1f - W[7]) * delta
        return reverted.coerceIn(MIN_DIFFICULTY, MAX_DIFFICULTY)
    }

    private fun grownStability(
        stability: Float,
        difficulty: Float,
        retrievability: Float,
        grade: Int,
    ): Float {
        val hardPenalty = if (grade == HARD) W[15] else 1f
        val easyBonus = if (grade == EASY) W[16] else 1f
        val growth =
            1f +
                exp(W[8]) *
                (11f - difficulty) *
                stability.pow(-W[9]) *
                (exp((1f - retrievability) * W[10]) - 1f) *
                hardPenalty *
                easyBonus
        return (stability * growth).coerceAtLeast(MIN_STABILITY)
    }

    private fun failedStability(
        stability: Float,
        difficulty: Float,
        retrievability: Float,
    ): Float =
        (
            W[11] *
                difficulty.pow(-W[12]) *
                ((stability + 1f).pow(W[13]) - 1f) *
                exp((1f - retrievability) * W[14])
        ).coerceAtLeast(MIN_STABILITY)

    private fun intervalDays(
        stability: Float,
        grade: Int,
    ): Int {
        if (grade == AGAIN) return AGAIN_INTERVAL_DAYS
        val interval =
            stability / FACTOR *
                (TARGET_RETENTION.pow(1f / DECAY) - 1f)
        return max(1, interval.roundToInt())
    }
}

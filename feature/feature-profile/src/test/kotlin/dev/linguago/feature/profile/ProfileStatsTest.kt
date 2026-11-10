package dev.linguago.feature.profile

import dev.linguago.core.domain.model.CefrLevel
import dev.linguago.core.domain.model.Course
import dev.linguago.core.domain.model.CourseId
import dev.linguago.core.domain.model.DailyActivity
import dev.linguago.core.domain.model.Language
import dev.linguago.core.domain.model.Lesson
import dev.linguago.core.domain.model.LessonId
import dev.linguago.core.domain.model.LessonProgress
import dev.linguago.core.domain.model.Level
import dev.linguago.core.domain.model.LevelId
import dev.linguago.core.domain.model.Unit
import dev.linguago.core.domain.model.UnitId
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

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

    @Test
    fun `las metricas orales agregan intentos aprobados y mejor fluidez`() {
        val metrics =
            ProfileStats.oralMetrics(
                listOf(
                    progress("l1", speakingAttempts = 2, passed = 1, bestWpm = 80f),
                    progress("l2", speakingAttempts = 2, passed = 2, bestWpm = 118f),
                ),
            )

        assertEquals(4, metrics.attempts)
        assertEquals(3, metrics.passed)
        assertEquals(75, metrics.passRatePercent)
        assertEquals(118, metrics.bestWpm)
    }

    @Test
    fun `la precision media ignora lecciones sin nota`() {
        val average =
            ProfileStats.averageBestScore(
                listOf(
                    progress("l1", completed = true, bestScore = 80),
                    progress("l2", completed = true, bestScore = 90),
                    progress("l3", completed = false, bestScore = 50),
                ),
            )

        assertEquals(85, average)
    }

    @Test
    fun `la ventana de actividad rellena los dias sin practica`() {
        val today = LocalDate.of(2026, 11, 9)
        val window =
            ProfileStats.activityWindow(
                activity =
                    listOf(
                        DailyActivity(today.toEpochDay(), xpEarned = 40, lessonsCompleted = 1, reviewsCompleted = 0),
                        DailyActivity(today.minusDays(2).toEpochDay(), xpEarned = 0, lessonsCompleted = 0, reviewsCompleted = 3),
                    ),
                today = today,
            )

        assertEquals(28, window.size)
        assertEquals(today.toEpochDay(), window.last().epochDay)
        assertEquals(true, window.last().active)
        assertEquals(true, window[25].active)
        assertEquals(false, window.first().active)
    }

    @Test
    fun `el desglose cefr incluye las seis etapas y el curso activo`() {
        val course =
            Course(
                id = CourseId("es-en"),
                fromLanguage = Language.SPANISH,
                toLanguage = Language.ENGLISH,
                levels =
                    listOf(
                        Level(
                            id = LevelId("lv-1"),
                            name = "Nivel 1",
                            units =
                                listOf(
                                    Unit(
                                        id = UnitId("un-1"),
                                        title = "Unidad 1",
                                        lessons =
                                            listOf(
                                                lesson("a1", CefrLevel.A1),
                                                lesson("a2", CefrLevel.A2),
                                                lesson("b2", CefrLevel.B2),
                                            ),
                                    ),
                                ),
                        ),
                    ),
            )
        val progress =
            listOf(
                progress("a1", completed = true),
                progress("a2", completed = false),
            )

        val stages = ProfileStats.cefrProgress(course, progress)

        assertEquals(6, stages.size)
        assertEquals(CefrProgressUi(CefrLevel.A1, completed = 1, total = 1), stages[0])
        assertEquals(CefrProgressUi(CefrLevel.A2, completed = 0, total = 1), stages[1])
        assertEquals(CefrProgressUi(CefrLevel.B2, completed = 0, total = 1), stages[3])
        assertEquals(CefrProgressUi(CefrLevel.C2, completed = 0, total = 0), stages[5])
    }

    private fun lesson(
        id: String,
        cefr: CefrLevel,
    ): Lesson =
        Lesson(
            id = LessonId(id),
            title = "Lección $id",
            exercises = emptyList(),
            cefrLevel = cefr,
        )

    private fun progress(
        id: String,
        completed: Boolean = true,
        speakingAttempts: Int = 0,
        passed: Int = 0,
        bestWpm: Float = 0f,
        bestScore: Int = 0,
    ): LessonProgress =
        LessonProgress(
            lessonId = LessonId(id),
            completed = completed,
            xp = 0,
            attempts = 1,
            speakingAttempts = speakingAttempts,
            speakingPassedAttempts = passed,
            speakingBestWpm = bestWpm,
            bestScorePercent = bestScore,
        )
}

package dev.linguago.baselineprofile

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class BaselineProfileGenerator {
    @get:Rule
    val baselineProfileRule = BaselineProfileRule()

    private val uiDevice: UiDevice
        get() = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

    @Before
    fun seedState() {
        seedBenchmarkData(uiDevice)
    }

    @Test
    fun startupProfile() =
        baselineProfileRule.collect(
            packageName = PACKAGE_NAME,
            includeInStartupProfile = true,
        ) {
            pressHome()
            startActivityAndWait()
            awaitHome(device)
        }

    @Test
    fun criticalJourneyProfile() =
        baselineProfileRule.collect(
            packageName = PACKAGE_NAME,
        ) {
            pressHome()
            startActivityAndWait()
            awaitHome(device)
            openB2Lesson(device)
            interactWithCloze(device)
            exitLesson(device)
            openReview(device)
        }
}

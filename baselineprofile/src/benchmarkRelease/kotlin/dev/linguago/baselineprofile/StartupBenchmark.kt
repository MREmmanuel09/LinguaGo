package dev.linguago.baselineprofile

import androidx.benchmark.macro.BaselineProfileMode
import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.FrameTimingMetric
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.StartupTimingMetric
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
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
class StartupBenchmark {
    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()

    private val uiDevice: UiDevice
        get() = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

    @Before
    fun selectCourse() {
        ensureCourseSelected(uiDevice)
    }

    @Test
    fun coldStartWithBaselineProfile() =
        benchmarkRule.measureRepeated(
            packageName = PACKAGE_NAME,
            metrics = listOf(StartupTimingMetric()),
            iterations = ITERATIONS,
            startupMode = StartupMode.COLD,
            compilationMode = CompilationMode.Partial(BaselineProfileMode.Require),
        ) {
            pressHome()
            startActivityAndWait()
            awaitHome(device)
        }

    @Test
    fun coldStartWithoutBaselineProfile() =
        benchmarkRule.measureRepeated(
            packageName = PACKAGE_NAME,
            metrics = listOf(StartupTimingMetric()),
            iterations = ITERATIONS,
            startupMode = StartupMode.COLD,
            compilationMode = CompilationMode.None(),
        ) {
            pressHome()
            startActivityAndWait()
            awaitHome(device)
        }

    @Test
    fun homeScrollWithBaselineProfile() =
        benchmarkRule.measureRepeated(
            packageName = PACKAGE_NAME,
            metrics = listOf(FrameTimingMetric()),
            iterations = ITERATIONS,
            startupMode = StartupMode.COLD,
            compilationMode = CompilationMode.Partial(BaselineProfileMode.Require),
        ) {
            pressHome()
            startActivityAndWait()
            awaitHome(device)
            repeat(SCROLLS_PER_ITERATION) {
                device.swipe(
                    device.displayWidth / 2,
                    (device.displayHeight * 0.75f).toInt(),
                    device.displayWidth / 2,
                    (device.displayHeight * 0.30f).toInt(),
                    20,
                )
                device.waitForIdle()
            }
        }

    @Test
    fun homeScrollWithoutBaselineProfile() =
        benchmarkRule.measureRepeated(
            packageName = PACKAGE_NAME,
            metrics = listOf(FrameTimingMetric()),
            iterations = ITERATIONS,
            startupMode = StartupMode.COLD,
            compilationMode = CompilationMode.None(),
        ) {
            pressHome()
            startActivityAndWait()
            awaitHome(device)
            repeat(SCROLLS_PER_ITERATION) {
                device.swipe(
                    device.displayWidth / 2,
                    (device.displayHeight * 0.75f).toInt(),
                    device.displayWidth / 2,
                    (device.displayHeight * 0.30f).toInt(),
                    20,
                )
                device.waitForIdle()
            }
        }

    private companion object {
        const val ITERATIONS = 5
        const val SCROLLS_PER_ITERATION = 3
    }
}

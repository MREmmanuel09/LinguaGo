package dev.linguago.qa

import android.app.Instrumentation
import android.content.Context
import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import org.json.JSONObject
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.time.Duration
import java.time.Instant

/**
 * RELEASE GATE F4 (ROADMAP_V2.md): sesión de uso real sobre la app `enRelease`
 * minimizada (R8), con métricas térmicas/memoria/estabilidad y perturbaciones
 * exploratorias. El reporte JSON queda determinado en el logcat y en el
 * almacenamiento externo de la app para su posterior inspección.
 */
@RunWith(AndroidJUnit4::class)
class ReleaseGateF4Test {
    private lateinit var device: UiDevice
    private lateinit var probe: DeviceProbe
    private lateinit var report: ReleaseGateReport
    private lateinit var driver: LessonDriver
    private lateinit var targetContext: Context

    private var targetMinutes: Int = FULL_TARGET_MINUTES
    private var minLessons: Int = 1
    private var originalFontScale: String = "1.0"
    private var originalNightMode: String = "0"
    private var originalLocale: String? = null
    private var lessonsCompleted: Int = 0

    @Before
    fun setUp() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        device = UiDevice.getInstance(instrumentation)
        probe = DeviceProbe(device)
        targetContext = instrumentation.targetContext
        report = ReleaseGateReport(probe.deviceInfo())
        driver = LessonDriver(device, probe, report, targetContext)

        targetMinutes = readIntArg(instrumentation, "linguago.releasegate.durationMinutes", FULL_TARGET_MINUTES)
        minLessons = readIntArg(instrumentation, "linguago.releasegate.minLessons", 1)
        val smoke = targetMinutes < FULL_TARGET_MINUTES

        clearLogBuffers()
        originalFontScale = probe.setting("system", "font_scale")
        originalNightMode = probe.setting("secure", "ui_night_mode")
        originalLocale = probe.setting("system", "system_locales").takeIf { it != "null" }

        report.startSession(nowIso(), targetMinutes.toLong(), smoke)
        Log.i(LOG_TAG, "Gate F4: target=${targetMinutes}min smoke=$smoke minLessons=$minLessons")

        grantPermissions(instrumentation)
        seedCourseSelection()
        captureBaselines()
    }

    @Test
    fun releaseGate20MinuteSession() {
        val wallStart = Instant.now()
        val targetMs = targetMinutes * 60_000L
        val wallCapMs = targetMs + (BACKGROUND_MINUTES + OVERHEAD_MINUTES) * 60_000L
        var foregroundMs = 0L
        var lessonIndex = 0
        var consecutiveFailures = 0
        var didBackground = false

        try {
            while (foregroundMs < targetMs || lessonsCompleted < minLessons) {
                if (Duration.between(wallStart, Instant.now()).toMillis() > wallCapMs) {
                    report.addScenario("hard_cap", "NOTE", "se alcanzó el límite de tiempo en pared")
                    return
                }
                val backgroundDue = targetMs >= BACKGROUND_MINUTES * 60_000L * 3L
                if (!didBackground && backgroundDue && foregroundMs >= targetMs / 2) {
                    didBackground = true
                    runBackgroundScenario()
                } else {
                    val lessonStart = Instant.now()
                    val ok = runLessonWithScenario(LessonPlan.scenarioFor(lessonIndex))
                    foregroundMs += Duration.between(lessonStart, Instant.now()).toMillis()
                    if (ok) {
                        lessonsCompleted += 1
                        consecutiveFailures = 0
                    } else {
                        consecutiveFailures += 1
                        if (consecutiveFailures >= MAX_CONSECUTIVE_FAILURES) {
                            report.addScenario("abort", "FAIL", "3 lecciones consecutivas fallidas")
                            return
                        }
                    }
                    lessonIndex += 1
                }
            }
            runSpecialScenarios()
        } finally {
            restoreSettings()
            collectCrashEvidence()
            finalizeSession(wallStart, foregroundMs)
        }
    }

    /** Reserva flags de la lección (fontScale/oscuro/locale) y conduce la lección. */
    private fun runLessonWithScenario(scenario: LessonScenario): Boolean {
        if (scenario.fontScale2x) {
            probe.putSetting("system", "font_scale", "2.0")
            report.addManualCheck("fontScale=2.0 aplicado en la lección ${scenario.lessonIndex}")
        }
        if (scenario.darkMode) {
            probe.putSetting("secure", "ui_night_mode", "2")
            report.addManualCheck("modo oscuro aplicado en la lección ${scenario.lessonIndex}")
        }
        if (scenario.localeChange) {
            probe.putSetting("system", "system_locales", "en-US")
            report.addManualCheck("locale en-US aplicado en la lección ${scenario.lessonIndex}")
        }
        val ok = driver.runLesson(scenario)
        if (scenario.fontScale2x) probe.putSetting("system", "font_scale", originalFontScale)
        if (scenario.darkMode) probe.putSetting("secure", "ui_night_mode", originalNightMode)
        if (scenario.localeChange) restoreLocale()
        return ok
    }

    /**
     * Escenario "5 min en background → volver → progreso persiste": la app sale
     * a Home durante BACKGROUND_MINUTES, se reabre y se verifica que comunica
     * con Home y que el proceso sigue vivo (o que sobrevivió a su muerte).
     */
    private fun runBackgroundScenario() {
        val beforePid = probe.pid()
        probe.pressHome()
        Log.i(LOG_TAG, "Background ${BACKGROUND_MINUTES}minLord: pid=$beforePid")
        Thread.sleep(BACKGROUND_MINUTES * 60_000L)
        probe.launchApp()
        val backHome = driver.waitForHome()
        val afterPid = probe.pid()
        val survive = beforePid != null && afterPid != null && beforePid == afterPid
        report.addScenario(
            "background_5min_persistence",
            if (backHome && afterPid != null) "PASS" else "FAIL",
            "home=$backHome pid $beforePid -> $afterPid (mismo_pid=$survive)",
        )
        report.addManualCheck("persistencia visual del progreso tras background: verificación subjetiva")
        if (!backHome) report.incrementRestarts()
    }

    private fun runSpecialScenarios() {
        val home = driver.waitForHome()
        report.addScenario("special:home_after_session", if (home) "PASS" else "FAIL", "Home visible al final")
        report.addManualCheck("TalkBack concurrente con la sesión: verificación subjetiva humana")
        report.addManualCheck("haptics percibidos en aciertos/errores: verificación subjetiva humana")
        report.addManualCheck("latencia 'sentida' del STT y del TTS: verificación subjetiva humana")
    }

    private fun finalizeSession(
        wallStart: Instant,
        foregroundMs: Long,
    ) {
        val thermal = ThermalStatus.fromCode(probe.thermalRaw())
        val ram = probe.totalPssMb()
        val nativeHeap = probe.nativeHeapMb()
        val fd = probe.fdCount()
        report.finish(nowIso(), foregroundMs, thermal, ram, nativeHeap, fd)
        val json = report.toJson()
        writeReport(json)
        Log.i(LOG_TAG, "Veredicto=${json.optString("verdict")} failed=${json.optJSONArray("failed_criteria")}")
    }

    private fun captureBaselines() {
        val thermal = ThermalStatus.fromCode(probe.thermalRaw())
        val ram = probe.totalPssMb()
        val nativeHeap = probe.nativeHeapMb()
        val fd = probe.fdCount()
        report.setBaselines(thermal, ram, nativeHeap, fd)
        Log.i(LOG_TAG, "Baseline: thermal=${thermal.label} ram=${ram}Mb native=${nativeHeap}Mb fd=$fd")
    }

    private fun clearLogBuffers() {
        runCatching { device.executeShellCommand("logcat -b crash -c") }
        runCatching { device.executeShellCommand("logcat -b events -c") }
        runCatching { device.executeShellCommand("logcat -c") }
    }

    private fun collectCrashEvidence() {
        val crashDump = runCatching { device.executeShellCommand("logcat -b crash -d") }.getOrDefault("")
        crashDump
            .split("FATAL EXCEPTION")
            .drop(1)
            .filter { block -> block.lineSequence().take(CRASH_LOOKAHEAD_LINES).any { TARGET_PACKAGE in it } }
            .forEach { block -> report.addCrash(block.trim().take(MAX_LOG_LINE_CHARS)) }
        val eventsDump = runCatching { device.executeShellCommand("logcat -b events -d") }.getOrDefault("")
        eventsDump
            .lineSequence()
            .filter { it.contains("am_anr") && it.contains(TARGET_PACKAGE) }
            .forEach { report.addAnr(it.trim().take(MAX_LOG_LINE_CHARS)) }
    }

    private fun grantPermissions(instrumentation: Instrumentation) {
        listOf(
            "android.permission.RECORD_AUDIO",
            "android.permission.POST_NOTIFICATIONS",
        ).forEach { permission ->
            runCatching { instrumentation.uiAutomation.grantRuntimePermission(TARGET_PACKAGE, permission) }
        }
    }

    private fun seedCourseSelection() {
        probe.shell("am force-stop $TARGET_PACKAGE")
        probe.launchApp()
        val alreadyHome = device.wait(Until.hasObject(By.text(HOME_LABEL)), DEFAULT_TIMEOUT_MILLIS)
        if (alreadyHome) {
            probe.shell("am force-stop $TARGET_PACKAGE")
            return
        }
        device.findObject(By.text(COURSE_TILE_LABEL))?.click()
        device.waitForIdle()
        device.findObject(By.text(START_COURSE_LABEL))?.click()
        device.waitForIdle()
        device.wait(Until.hasObject(By.text(HOME_LABEL)), DEFAULT_TIMEOUT_MILLIS)
        // Deja la app viva en Home: el baseline debe medir un proceso real.
        probe.launchApp()
        device.waitForIdle()
    }

    private fun restoreSettings() {
        runCatching { probe.putSetting("system", "font_scale", originalFontScale) }
        runCatching { probe.putSetting("secure", "ui_night_mode", originalNightMode) }
        runCatching { probe.putSetting("system", "system_locales", originalLocale ?: "es-ES") }
        runCatching { probe.putSetting("global", "always_finish_activities", "0") }
    }

    private fun restoreLocale() {
        val value = originalLocale ?: return
        probe.putSetting("system", "system_locales", value)
    }

    private fun writeReport(json: JSONObject) {
        val outputDir =
            targetContext.getExternalFilesDir(null)
                ?: File("/sdcard/Android/data/$TARGET_PACKAGE/files")
        val file = File(outputDir, REPORT_FILE_NAME)
        file.writeText(json.toString(2))
        Log.i(LOG_TAG, "Reporte JSON: ${file.absolutePath}")
        Log.i(LOG_TAG, "Veredicto final: ${json.optString("verdict")}")
        // La desinstalación tras la prueba borra /Android/data/<pkg>; el
        // reporte sobrevive en /data/local/tmp para pullearlo después.
        probe.shell("cp ${file.absolutePath} /data/local/tmp/$REPORT_FILE_NAME")
    }

    private fun readIntArg(
        instrumentation: Instrumentation,
        key: String,
        default: Int,
    ): Int = InstrumentationRegistry.getArguments().getString(key)?.toIntOrNull() ?: default

    private fun nowIso(): String = Instant.now().toString()
}

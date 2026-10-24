package dev.linguago.qa

import org.json.JSONArray
import org.json.JSONObject

/** Nivel térmico Android (power HAL); orden creciente = más calor. */
internal enum class ThermalStatus(
    val code: Int,
    val label: String,
    val blocksGate: Boolean,
) {
    NONE(0, "NONE", false),
    LIGHT(1, "LIGHT", false),
    MODERATE(2, "MODERATE", true),
    SEVERE(3, "SEVERE", true),
    CRITICAL(4, "CRITICAL", true),
    EMERGENCY(5, "EMERGENCY", true),
    SHUTDOWN(6, "SHUTDOWN", true),
    UNKNOWN(-1, "UNKNOWN", true),
    ;

    companion object {
        fun fromCode(code: Int?): ThermalStatus = entries.firstOrNull { it.code == code } ?: UNKNOWN
    }
}

/** Resultado de una perturbación exploratoria. */
internal data class ScenarioResult(
    val name: String,
    val status: String,
    val detail: String,
)

/** Métricas de una iteración (un ejercicio). */
internal data class IterationRecord(
    val lessonIndex: Int,
    val lessonTitle: String,
    val step: String,
    val durationMs: Long,
    val ramMb: Int?,
    val nativeHeapMb: Int?,
    val javaHeapMb: Int?,
    val graphicsMb: Int?,
    val privateOtherMb: Int?,
    val fdCount: Int?,
    val thermalStatus: String,
    val outcome: String,
    val timestamp: String,
)

/** Modelo del reporte JSON del gate. */
internal class ReleaseGateReport(
    private val deviceInfo: Map<String, String>,
) {
    private val iterations = mutableListOf<IterationRecord>()
    private val scenarios = mutableListOf<ScenarioResult>()
    private val raceSuspects = mutableListOf<String>()
    private val crashes = mutableListOf<String>()
    private val anrs = mutableListOf<String>()
    private val manualChecks = mutableListOf<String>()
    private val failedCriteria = mutableListOf<String>()

    private var startedAt: String? = null
    private var endedAt: String? = null
    private var durationMs: Long = 0
    private var targetDurationMs: Long = 0
    private var smokeOnly: Boolean = false
    private var thermalStart: ThermalStatus = ThermalStatus.UNKNOWN
    private var thermalEnd: ThermalStatus = ThermalStatus.UNKNOWN
    private var thermalPeak: ThermalStatus = ThermalStatus.NONE
    private var ramStart: Int? = null
    private var nativeHeapStart: Int? = null
    private var fdStart: Int? = null
    private var fdEnd: Int? = null
    private var nativeHeapEnd: Int? = null
    private var processRestarts: Int = 0
    private var lessonsCompleted: Int = 0

    fun startSession(
        isoNow: String,
        targetMinutes: Long,
        smoke: Boolean,
    ) {
        startedAt = isoNow
        targetDurationMs = targetMinutes * 60_000L
        smokeOnly = smoke
    }

    fun setBaselines(
        thermal: ThermalStatus,
        ramMb: Int?,
        nativeHeapMb: Int?,
        fdCount: Int?,
    ) {
        thermalStart = thermal
        thermalPeak = thermal
        ramStart = ramMb
        nativeHeapStart = nativeHeapMb
        fdStart = fdCount
    }

    fun noteThermal(thermal: ThermalStatus) {
        if (thermal.code > thermalPeak.code) thermalPeak = thermal
    }

    fun addIteration(record: IterationRecord) {
        iterations += record
    }

    fun addScenario(
        name: String,
        status: String,
        detail: String,
    ) {
        scenarios += ScenarioResult(name, status, detail)
    }

    fun addRaceSuspect(detail: String) {
        raceSuspects += detail
    }

    fun addCrash(detail: String) {
        crashes += detail
    }

    fun addAnr(detail: String) {
        anrs += detail
    }

    fun addManualCheck(detail: String) {
        manualChecks += detail
    }

    fun incrementRestarts() {
        processRestarts++
    }

    fun incrementLessons() {
        lessonsCompleted++
    }

    fun finish(
        isoNow: String,
        elapsedMs: Long,
        thermal: ThermalStatus,
        ramMb: Int?,
        nativeHeapMb: Int?,
        fdCount: Int?,
    ) {
        endedAt = isoNow
        durationMs = elapsedMs
        thermalEnd = thermal
        noteThermal(thermal)
        evaluate(ramMb, nativeHeapMb, fdCount)
    }

    private fun evaluate(
        ramEnd: Int?,
        nativeHeapEndMb: Int?,
        fdEndMb: Int?,
    ) {
        fdEnd = fdEndMb
        nativeHeapEnd = nativeHeapEndMb
        val peakRam = listOfNotNull(ramStart, ramEnd, iterations.mapNotNull { it.ramMb }.maxOrNull()).maxOrNull()
        if (targetDurationMs > 0 && durationMs < targetDurationMs && !smokeOnly) {
            failedCriteria += "duration: ${durationMs / 1000}s < target ${targetDurationMs / 1000}s"
        }
        if (crashes.isNotEmpty()) failedCriteria += "crashes: ${crashes.size}"
        if (anrs.isNotEmpty()) failedCriteria += "anrs: ${anrs.size}"
        if (thermalEnd.blocksGate) {
            failedCriteria += "thermal: ${thermalStart.label} -> ${thermalEnd.label}"
        }
        if (peakRam != null && peakRam > MAX_RAM_MB) failedCriteria += "ram: peak ${peakRam}MB > ${MAX_RAM_MB}MB"
        if (raceSuspects.isNotEmpty()) failedCriteria += "race_suspects: ${raceSuspects.size}"
        val failedScenarios = scenarios.filter { it.status == "FAIL" || it.status == "ERROR" }
        if (failedScenarios.isNotEmpty()) {
            failedCriteria += "scenarios: ${failedScenarios.map { it.name }}"
        }
        val fdStartLocal = fdStart
        if (fdStartLocal != null && fdEndMb != null && fdEndMb - fdStartLocal > MAX_FD_GROWTH) {
            failedCriteria += "fd_leak: $fdStartLocal -> $fdEndMb"
        }
        // Fuga nativa: pendiente por lección sobre el pico de la primera
        // (con modelos ya cargados). Comparar extremos del segmento evita
        // confundir el salto inicial de carga con una fuga sostenida.
        val nativeByLesson =
            iterations
                .groupBy { it.lessonIndex }
                .mapNotNull { (_, group) -> group.mapNotNull { it.nativeHeapMb }.maxOrNull() }
        if (nativeByLesson.size >= 2) {
            val drift = nativeByLesson.last() - nativeByLesson.first()
            val budget = MAX_NATIVE_GROWTH_PER_LESSON_MB * (nativeByLesson.size - 1)
            if (drift > budget) {
                failedCriteria +=
                    "native_drift: ${nativeByLesson.first()}->${nativeByLesson.last()}MB " +
                    "(${drift}MB > ${budget}MB en ${nativeByLesson.size} lecciones)"
            }
        }
        if (iterations.isEmpty()) failedCriteria += "no_iterations"
    }

    fun verdict(): String = if (failedCriteria.isEmpty()) "PASS" else "FAIL"

    fun toJson(): JSONObject {
        val peakRam =
            listOfNotNull(ramStart, iterations.mapNotNull { it.ramMb }.maxOrNull()).maxOrNull()
        val peakThermal = thermalPeak
        return JSONObject().apply {
            put(
                "session",
                JSONObject().apply {
                    put("started_at", startedAt ?: "")
                    put("ended_at", endedAt ?: "")
                    put("duration_ms", durationMs)
                    put("target_duration_ms", targetDurationMs)
                    put("smoke_only", smokeOnly)
                    put("device", JSONObject(deviceInfo))
                    put("package", TARGET_PACKAGE)
                    put("variant", "enRelease (minified, R8)")
                    put("lessons_completed", lessonsCompleted)
                    put("process_restarts", processRestarts)
                },
            )
            put(
                "iterations",
                JSONArray().apply {
                    iterations.forEach { record ->
                        put(
                            JSONObject().apply {
                                put("lesson_index", record.lessonIndex)
                                put("lesson_title", record.lessonTitle)
                                put("step", record.step)
                                put("duration_ms", record.durationMs)
                                put("ram_mb", record.ramMb ?: JSONObject.NULL)
                                put("native_heap_mb", record.nativeHeapMb ?: JSONObject.NULL)
                                put("java_heap_mb", record.javaHeapMb ?: JSONObject.NULL)
                                put("graphics_mb", record.graphicsMb ?: JSONObject.NULL)
                                put("private_other_mb", record.privateOtherMb ?: JSONObject.NULL)
                                put("fd_count", record.fdCount ?: JSONObject.NULL)
                                put("thermal_status", record.thermalStatus)
                                put("outcome", record.outcome)
                                put("timestamp", record.timestamp)
                            },
                        )
                    }
                },
            )
            put(
                "peaks",
                JSONObject().apply {
                    put("ram_mb", peakRam ?: JSONObject.NULL)
                    put("thermal_status", peakThermal.label)
                    put("thermal_start", thermalStart.label)
                    put("thermal_end", thermalEnd.label)
                    put("native_heap_start_mb", nativeHeapStart ?: JSONObject.NULL)
                    put("native_heap_end_mb", nativeHeapEnd ?: JSONObject.NULL)
                    put("fd_start", fdStart ?: JSONObject.NULL)
                    put("fd_end", fdEnd ?: JSONObject.NULL)
                },
            )
            put(
                "scenarios",
                JSONArray().apply {
                    scenarios.forEach { scenario ->
                        put(
                            JSONObject().apply {
                                put("name", scenario.name)
                                put("status", scenario.status)
                                put("detail", scenario.detail)
                            },
                        )
                    }
                },
            )
            put("race_suspects", JSONArray(raceSuspects))
            put("crashes", JSONArray(crashes))
            put("anrs", JSONArray(anrs))
            put("manual_checks", JSONArray(manualChecks))
            put("failed_criteria", JSONArray(failedCriteria))
            put("verdict", verdict())
        }
    }
}

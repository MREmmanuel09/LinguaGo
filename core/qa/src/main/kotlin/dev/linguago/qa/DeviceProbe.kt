package dev.linguago.qa

import android.util.Log
import androidx.test.uiautomator.UiDevice

/**
 * Sonda de métricas del dispositivo vía `dumpsys`/`/proc`. Todo se ejecuta con
 * el uid `shell` de UiAutomator, igual que `AppJourneys` del baseline profile.
 */
internal class DeviceProbe(
    private val device: UiDevice,
) {
    private var lastPid: String? = null

    /** PID del proceso de la app (null si no está viva). */
    fun pid(): String? = shell("pidof $TARGET_PACKAGE").trim().takeIf { it.isNotEmpty() }

    /** Detecta si el proceso murió y reapareció entre llamadas. */
    fun detectRestart(): Boolean {
        val current = pid()
        val previous = lastPid
        lastPid = current
        return previous != null && current != null && previous != current
    }

    /** Thermal status crudo de `dumpsys thermalservice` (0 = NONE). */
    fun thermalRaw(): Int? {
        val output = shell("dumpsys thermalservice")
        return Regex("Thermal Status:\\s*(\\d+)")
            .find(output)
            ?.groupValues
            ?.get(1)
            ?.toIntOrNull()
    }

    /**
     * Desglose de memoria (MB) de un único `dumpsys meminfo`. Permite separar
     * el crecimiento Java (churn de Compose/estado) del nativo (modelos JNI)
     * y de gráficos, para diagnosticar fugas en la corrida larga.
     */
    fun memSnapshot(): MemSnapshot {
        val output = shell("dumpsys meminfo $TARGET_PACKAGE")

        fun section(name: String): Int? =
            Regex("$name:?\\s+(\\d+)")
                .find(output)
                ?.groupValues
                ?.get(1)
                ?.toIntOrNull()
                ?.let { (it + 512) / 1024 }
        return MemSnapshot(
            totalPssMb = section("TOTAL(?: PSS)?"),
            javaHeapMb = section("Java Heap"),
            nativeHeapMb = section("Native Heap"),
            codeMb = section("Code"),
            graphicsMb = section("Graphics"),
            privateOtherMb = section("Private Other"),
            systemMb = section("System"),
        )
    }

    /** Cantidad de fds abiertos; null si Android impide leer `/proc/<pid>/fd`. */
    fun fdCount(): Int? {
        val pid = pid() ?: return null
        val output = shell("ls /proc/$pid/fd 2>/dev/null | wc -l").trim()
        return output.toIntOrNull()
    }

    /** Información estática del dispositivo/build. */
    fun deviceInfo(): Map<String, String> =
        mapOf(
            "model" to shell("getprop ro.product.model").trim(),
            "device" to shell("getprop ro.product.device").trim(),
            "sdk" to shell("getprop ro.build.version.sdk").trim(),
            "release" to shell("getprop ro.build.version.release").trim(),
            "abi" to shell("getprop ro.product.cpu.abi").trim(),
            "fingerprint" to shell("getprop ro.build.fingerprint").trim(),
        )

    /** Flags de instalación de la app (comprueba que no sea debuggable en release). */
    fun packageFlags(): String {
        val output = shell("dumpsys package $TARGET_PACKAGE")
        return Regex("flags=\\[([^]]*)]")
            .find(output)
            ?.groupValues
            ?.get(1)
            ?.trim() ?: ""
    }

    /** Cambios de configuración del sistema (para restaurar en el `finally`). */
    fun setting(
        namespace: String,
        key: String,
    ): String = shell("settings get $namespace $key").trim()

    fun putSetting(
        namespace: String,
        key: String,
        value: String,
    ) {
        shell("settings put $namespace $key $value")
    }

    fun launchApp() {
        shell("am start -W -n $TARGET_PACKAGE/$MAIN_ACTIVITY")
    }

    fun pressHome() {
        device.pressHome()
        device.waitForIdle()
    }

    fun shell(command: String): String =
        runCatching { device.executeShellCommand(command) }
            .onFailure { Log.w("ReleaseGate", "shell falló: $command: ${it.message}") }
            .getOrDefault("")
}

/** Desglose de memoria (MB) leído de un `dumpsys meminfo`. */
internal data class MemSnapshot(
    val totalPssMb: Int?,
    val javaHeapMb: Int?,
    val nativeHeapMb: Int?,
    val codeMb: Int?,
    val graphicsMb: Int?,
    val privateOtherMb: Int?,
    val systemMb: Int?,
)

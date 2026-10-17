package dev.linguago.core.audio

import java.io.File
import java.security.MessageDigest
import java.util.Locale

/** Entrada de modelo en `models/manifest.json` (ver docs/MODELS.md). */
public data class ModelEntry(
    val id: String,
    val path: String,
    val sha256: String,
)

/**
 * Integridad de modelos on-device (ADR-006): cada archivo de modelo se
 * verifica por SHA-256 antes de usarse; sin modelos o con hash inválido la
 * app degrada (voz del sistema) en lugar de fallar.
 */
public object ModelIntegrity {
    /** Hex SHA-256 del archivo dado en minúsculas. */
    public fun sha256Hex(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { input ->
            val buffer = ByteArray(BUFFER_SIZE)
            while (true) {
                val read = input.read(buffer)
                if (read < 0) break
                digest.update(buffer, 0, read)
            }
        }
        return digest.digest().joinToString(separator = "") { byte -> byte.toHexByte() }
    }

    /** Devuelve las entradas cuyo archivo existe y cuyo hash coincide. */
    public fun validFiles(
        modelsDir: File,
        entries: List<ModelEntry>,
    ): List<File> =
        entries.mapNotNull { entry ->
            val file = File(modelsDir, entry.path)
            if (!file.isFile) {
                null
            } else if (sha256Hex(file).equals(entry.sha256, ignoreCase = true)) {
                file
            } else {
                null
            }
        }

    /** Un solo hex byte en minúsculas. */
    private fun Byte.toHexByte(): String = String.format(Locale.ROOT, "%02x", this)
}

private const val BUFFER_SIZE: Int = 8192

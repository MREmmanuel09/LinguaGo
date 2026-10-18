package dev.linguago.core.audio

import java.io.File

/**
 * Publicación atómica de un paquete de assets (ADR-020): el contenido se
 * copia a [stagingDir], se verifica y solo entonces se hace `renameTo` a
 * [targetDir] (mismo volumen), de modo que un proceso que muere a mitad de
 * copia deja a lo sumo un staging obsoleto — nunca un destino corrupto.
 * Sin dependencias Android: testeable en JVM.
 */
internal object AtomicInstall {
    /**
     * Copia con [copy], verifica con [verify] y publica en [targetDir].
     * Devuelve el directorio publicado o `null` si algo falló; en el fallo,
     * el staging queda limpio y el destino previo intacto.
     */
    fun install(
        stagingDir: File,
        targetDir: File,
        copy: (File) -> Unit,
        verify: (File) -> Boolean,
    ): File? {
        stagingDir.deleteRecursively()
        stagingDir.parentFile?.mkdirs()
        return try {
            copy(stagingDir)
            if (!verify(stagingDir)) {
                stagingDir.deleteRecursively()
                null
            } else {
                targetDir.deleteRecursively()
                if (stagingDir.renameTo(targetDir)) {
                    targetDir
                } else {
                    stagingDir.deleteRecursively()
                    null
                }
            }
        } catch (error: Exception) {
            stagingDir.deleteRecursively()
            null
        }
    }
}

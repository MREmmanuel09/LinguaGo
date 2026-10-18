package dev.linguago.core.audio

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class AtomicInstallTest {
    @get:Rule
    val temporaryFolder: TemporaryFolder = TemporaryFolder()

    @Test
    fun `install publica el contenido verificado en el destino`() {
        val result =
            AtomicInstall.install(
                stagingDir = staging(),
                targetDir = target(),
                copy = { dir -> write(dir, "voz.onnx", "datos") },
                verify = { dir -> File(dir, "voz.onnx").isFile },
            )

        assertEquals(target(), result)
        assertEquals("datos", File(target(), "voz.onnx").readText())
        assertFalse(staging().exists())
    }

    @Test
    fun `staging que no verifica no se publica ni deja restos`() {
        val result =
            AtomicInstall.install(
                stagingDir = staging(),
                targetDir = target(),
                copy = { dir -> write(dir, "voz.onnx", "corrupto") },
                verify = { false },
            )

        assertNull(result)
        assertFalse(target().exists())
        assertFalse(staging().exists())
    }

    @Test
    fun `fallo de copia limpia staging y deja intacto el destino previo`() {
        write(target(), "voz.onnx", "previo")

        val result =
            AtomicInstall.install(
                stagingDir = staging(),
                targetDir = target(),
                copy = { error("disco lleno") },
                verify = { true },
            )

        assertNull(result)
        assertEquals("previo", File(target(), "voz.onnx").readText())
        assertFalse(staging().exists())
    }

    @Test
    fun `install reemplaza un destino previo inválido`() {
        write(target(), "viejo.onnx", "corrupto")

        val result =
            AtomicInstall.install(
                stagingDir = staging(),
                targetDir = target(),
                copy = { dir -> write(dir, "voz.onnx", "nuevo") },
                verify = { dir -> File(dir, "voz.onnx").isFile },
            )

        assertEquals(target(), result)
        assertFalse(File(target(), "viejo.onnx").exists())
        assertEquals("nuevo", File(target(), "voz.onnx").readText())
    }

    @Test
    fun `install limpia staging obsoleto antes de copiar`() {
        write(staging(), "huerfano.bin", "basura")

        val result =
            AtomicInstall.install(
                stagingDir = staging(),
                targetDir = target(),
                copy = { dir ->
                    assertFalse(File(dir, "huerfano.bin").exists())
                    write(dir, "voz.onnx", "ok")
                },
                verify = { true },
            )

        assertEquals(target(), result)
        assertTrue(File(target(), "voz.onnx").isFile)
    }

    @Test
    fun `install crea el directorio padre del staging`() {
        val parent = File(temporaryFolder.root, "tts")
        val result =
            AtomicInstall.install(
                stagingDir = File(parent, "voz.staging"),
                targetDir = File(parent, "voz"),
                copy = { dir -> write(dir, "voz.onnx", "ok") },
                verify = { true },
            )

        assertEquals(File(parent, "voz"), result)
    }

    private fun staging(): File = File(temporaryFolder.root, "modelo.staging")

    private fun target(): File = File(temporaryFolder.root, "modelo")

    private fun write(
        dir: File,
        name: String,
        content: String,
    ) {
        dir.mkdirs()
        File(dir, name).writeText(content)
    }
}

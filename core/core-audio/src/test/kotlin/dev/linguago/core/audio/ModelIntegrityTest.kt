package dev.linguago.core.audio

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class ModelIntegrityTest {
    @get:Rule
    val temporaryFolder: TemporaryFolder = TemporaryFolder()

    @Test
    fun `sha256 es estable y determinista por contenido`() {
        val file = File(temporaryFolder.root, "model.bin")
        file.writeText("linguago")

        val first = ModelIntegrity.sha256Hex(file)
        val second = ModelIntegrity.sha256Hex(file)

        assertEquals(first, second)
    }

    @Test
    fun `sha256 cambia con el contenido`() {
        val file = File(temporaryFolder.root, "model.bin")
        file.writeText("linguago")
        val original = ModelIntegrity.sha256Hex(file)
        file.writeText("otra")

        val modified = ModelIntegrity.sha256Hex(file)

        assertTrue(original != modified)
    }

    @Test
    fun `validFiles excluye hash distinto y archivo faltante`() {
        val modelsDir = temporaryFolder.root
        val good = File(modelsDir, "voces.bin")
        val bad = File(modelsDir, "otra.bin")
        good.writeText("linguago")
        bad.writeText("otro contenido")
        val goodHash = ModelIntegrity.sha256Hex(good)
        val badHash = ModelIntegrity.sha256Hex(bad)

        val entries =
            listOf(
                ModelEntry(id = "voces", path = good.name, sha256 = goodHash.replaceFirstChar { it.uppercaseChar() }),
                ModelEntry(id = "otra", path = bad.name, sha256 = goodHash),
                ModelEntry(id = "inexistente", path = "faltante.bin", sha256 = badHash),
            )

        val valid = ModelIntegrity.validFiles(modelsDir, entries)

        assertEquals(listOf(good), valid)
    }
}

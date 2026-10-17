// Composition root de plugins: se declaran aquí con `apply false` para fijar
// versiones en el classpath; los módulos los aplican via convention plugins
// de `build-logic` (ver ADR-001).
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import java.security.MessageDigest

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.room) apply false
    alias(libs.plugins.ktlint) apply false
    alias(libs.plugins.detekt) apply false
}

// Descarga manual de las voces Piper usadas por sherpa-onnx (ADR-003/006/010,
// Fase 2.2): cada paquete `vits-piper-<voz>.tar.bz2` del tag `tts-models`
// incluye `<voz>.onnx` + tokens.txt + espeak-ng-data/. Al terminar rellena
// el SHA-256 del `.onnx` en models/manifest.json.
//
// Tarea MANUAL: no está connected al build ni al CI (evita descargar ~150 MB
// en cada build; sin voces la app degrada al TTS del sistema, I3).
tasks.register("fetchVoiceModels") {
    group = "linguago"
    description =
        "Descarga las voces Piper de el tag tts-models de sherpa-onnx y " +
        "llena el sha256 en models/manifest.json."
    doLast {
        val modelsDir = File(rootDir, "models")
        val manifestFile = File(modelsDir, "manifest.json")
        val downloadsDir = File(modelsDir, "downloads")
        val ttsDir = File(modelsDir, "tts")
        for (id in listOf("en_US-amy-medium", "pt_BR-faber-medium", "fr_FR-siwis-medium")) {
            val asset = "vits-piper-$id.tar.bz2"
            val url = "https://github.com/k2-fsa/sherpa-onnx/releases/download/tts-models/$asset"
            val tarball = File(downloadsDir, asset)
            if (!tarball.isFile) {
                downloadsDir.mkdirs()
                println("Fetch: $url")
                tarball.parentFile.mkdirs()
                java.net.URI(url).toURL().openStream().use { input ->
                    tarball.outputStream().use { output -> input.copyTo(output) }
                }
            }
            val dir = File(ttsDir, id)
            if (!File(dir, "$id.onnx").isFile) {
                ttsDir.mkdirs()
                println("Extract: $asset -> ${dir.path}")
                val process = ProcessBuilder("tar", "-xjf", tarball.absolutePath)
                    .directory(ttsDir)
                    .redirectErrorStream(true)
                    .start()
                val out = process.inputStream.bufferedReader().readText()
                if (process.waitFor() != 0) {
                    throw IllegalStateException("tar falló: $out")
                }
                val srcDir = File(ttsDir, "vits-piper-$id")
                if (!srcDir.renameTo(dir)) {
                    throw Exception("no se puede renombrar ${srcDir.path} -> ${dir.path}")
                }
            }
            val onnx = File(dir, "$id.onnx")
            require(onnx.isFile && File(dir, "tokens.txt").isFile) {
                "paquete $id incompleto: faltan tokens/espeak"
            }
            val digest = MessageDigest.getInstance("SHA-256")
            onnx.inputStream().use { input ->
                val buffer = ByteArray(1 shl 16)
                var count = input.read(buffer)
                while (count >= 0) {
                    digest.update(buffer, 0, count)
                    count = input.read(buffer)
                }
            }
            val sha256 = digest.digest().joinToString("") { "%02x".format(it) }
            println("SHA-256: $id = $sha256")
            val manifest = JsonSlurper().parse(manifestFile) as Map<*, *>
            val entries = (manifest["tts"] as List<*>).map { entry ->
                val voice = entry as MutableMap<Any?, Any?>
                if (voice["id"] == id) voice["sha256"] = sha256
                voice
            }
            val output = LinkedHashMap(manifest)
            output["tts"] = entries
            manifestFile.writeText(
                JsonOutput.prettyPrint(JsonOutput.toJson(output)) + "\n",
            )
        }
        println("OK: models/manifest.json verificado; instalando voces en assets del app.")
    }
}


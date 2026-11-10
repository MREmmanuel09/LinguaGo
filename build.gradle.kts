// Composition root de plugins: se declaran aquí con `apply false` para fijar
// versiones en el classpath; los módulos los aplican via convention plugins
// de `build-logic` (ver ADR-001).
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.networknt.schema.JsonSchemaFactory
import com.networknt.schema.SpecVersion
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import java.security.MessageDigest
import kotlin.math.roundToInt

buildscript {
    dependencies {
        // Validador JSON Schema para la tarea validateContent (ADR-012).
        classpath("com.networknt:json-schema-validator:1.5.6")
    }
}

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.android.test) apply false
    alias(libs.plugins.androidx.baselineprofile) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.room) apply false
    alias(libs.plugins.ktlint) apply false
    alias(libs.plugins.detekt) apply false
}

// Descarga manual de los modelos de voz (ADR-003/006/010, Fase 2.2/2.3):
// TTS Piper (tag `tts-models` de sherpa-onnx, tar.bz2) y STT Vosk Small
// (alphacephei.com, zip). Recorre `models/manifest.json` y rellena los
// SHA-256: TTS = hash del `<voz>.onnx`; STT = `files[]` con hash por
// archivo. Tarea MANUAL: no esta wired al build ni al CI (no descarga
// ~300 MB por build; sin modelos la app degrada al TTS del sistema, I3).
tasks.register("fetchVoiceModels") {
    group = "linguago"
    description = "Descarga los modelos de voz y llena models/manifest.json."
    val modelsRoot = rootDir.absolutePath
    doLast {
        val modelsDir = File(modelsRoot, "models")
        val manifestFile = File(modelsDir, "manifest.json")
        val downloadsDir = File(modelsRoot, ".voice-downloads")
        val baseUrl = "https://alphacephei.com/vosk/models/"
        downloadsDir.mkdirs()
        val manifest = JsonSlurper().parse(manifestFile) as MutableMap<Any?, Any?>

        fun fetch(url: String, target: File) {
            if (target.isFile && target.length() > 0) return
            println("Fetch: $url")
            target.parentFile.mkdirs()
            java.net.URI(url).toURL().openStream().use { input ->
                target.outputStream().use { output -> input.copyTo(output) }
            }
        }

        fun extract(archive: File, workDir: File): Process =
            ProcessBuilder("tar", "-xf", archive.absolutePath)
                .directory(workDir)
                .redirectErrorStream(true)
                .start()

        fun sha256Hex(file: File): String {
            val digest = MessageDigest.getInstance("SHA-256")
            file.inputStream().use { input ->
                val buffer = ByteArray(1 shl 16)
                var count = input.read(buffer)
                while (count >= 0) {
                    digest.update(buffer, 0, count)
                    count = input.read(buffer)
                }
            }
            return digest.digest().joinToString("") { "%02x".format(it) }
        }

        /** Layouts de vosk: estándar (am/final.mdl) y legacy plano (final.mdl). */
        fun hasRecognizer(dir: File): Boolean =
            dir.walkTopDown().any { file -> file.name.equals("final.mdl", ignoreCase = true) }

        val ttsDirs = mutableListOf<File>()
        for (entry in manifest["tts"] as List<*>) {
            val tts = entry as MutableMap<Any?, Any?>
            val id = tts["id"] as String
            val asset = "vits-piper-$id.tar.bz2"
            val tarball = File(downloadsDir, asset)
            fetch("https://github.com/k2-fsa/sherpa-onnx/releases/download/tts-models/$asset", tarball)
            val ttsDir = File(modelsDir, "tts")
            val dir = File(ttsDir, id)
            if (!File(dir, "$id.onnx").isFile) {
                ttsDir.mkdirs()
                println("Extract: $asset -> ${dir.path}")
                val process = extract(tarball, ttsDir)
                val out = process.inputStream.bufferedReader().readText()
                if (process.waitFor() != 0) throw IllegalStateException("tar falló: $out")
                val srcDir = File(ttsDir, "vits-piper-$id")
                if (!srcDir.renameTo(dir)) throw IllegalStateException("no se pudo mover a ${dir.path}")
            }
            require(File(dir, "$id.onnx").isFile && File(dir, "tokens.txt").isFile) {
                "paquete $id incompleto"
            }
            ttsDirs += dir
            val hash = sha256Hex(File(dir, "$id.onnx"))
            println("SHA-256: tts/$id = $hash")
            tts["sha256"] = hash
        }

        // Los paquetes Piper repiten el mismo árbol espeak-ng-data (~17 MB)
        // en cada voz. Se conserva una sola copia bajo models/tts/espeak-ng-data
        // y el empaquetado la inyecta una vez (ADR-051).
        val sharedEspeak = File(modelsDir, "tts/espeak-ng-data")
        if (ttsDirs.any { dir -> File(dir, "espeak-ng-data").isDirectory }) {
            val source = ttsDirs.first { dir -> File(dir, "espeak-ng-data").isDirectory }
            if (!sharedEspeak.isDirectory) {
                println("Dedupe: ${source.name}/espeak-ng-data -> tts/espeak-ng-data")
                File(source, "espeak-ng-data").copyRecursively(sharedEspeak)
            }
            ttsDirs.forEach { dir -> File(dir, "espeak-ng-data").deleteRecursively() }
        }
        // Solo hablan las voces EN/PT/FR; los diccionarios de espeak del
        // resto de idiomas no se cargan y añaden peso (ADR-051).
        val requiredDictionaries = setOf("en_dict", "pt_dict", "fr_dict", "es_dict")
        sharedEspeak.listFiles()
            ?.filter { file -> file.isFile && file.name.endsWith("_dict") && file.name !in requiredDictionaries }
            ?.forEach { file -> file.delete() }

        for (entry in manifest["stt"] as List<*>) {
            val stt = entry as MutableMap<Any?, Any?>
            val path = stt["path"] as String
            val name = path.substringAfterLast('/')
            val zip = File(downloadsDir, "$name.zip")
            fetch(baseUrl + "$name.zip", zip)
            val sttDir = File(modelsDir, "stt")
            val dir = File(sttDir, name)
            if (!hasRecognizer(dir)) {
                sttDir.mkdirs()
                println("Extract: zip $name -> ${dir.path}")
                val process = extract(zip, sttDir)
                val out = process.inputStream.bufferedReader().readText()
                if (process.waitFor() != 0) {
                    println("tar no soportó el zip: $out")
                    val unzip = ProcessBuilder("unzip", "-o", zip.name).directory(sttDir).start()
                    if (unzip.waitFor() != 0) throw IllegalStateException("extract falló para $name")
                }
            }
            require(hasRecognizer(dir)) { "paquete $name incompleto" }
            val files = mutableListOf<MutableMap<String, String>>()
            dir.walkTopDown().filter { it.isFile }.sorted().forEach { file ->
                val rel = file.relativeTo(modelsDir).invariantSeparatorsPath
                val hash = sha256Hex(file)
                println("SHA-256: $rel = $hash")
                files.add(
                    linkedMapOf(
                        "path" to rel,
                        "sha256" to hash,
                    ),
                )
            }
            stt["files"] = files
        }

        manifestFile.writeText(
            JsonOutput.prettyPrint(JsonOutput.toJson(manifest)) + "\n",
        )
        println("OK: models/manifest.json actualizado; `./gradlew :app:assembleFullDebug` empaqueta models/ como assets.")
    }
}

// Validación del contenido contra el JSON Schema formal (ADR-012/ADR-018).
// Corre en CI antes de los tests: cada course.json y cada fixture se enruta
// por su `schemaVersion` (v1 o v2) y se valida contra el contrato que declara.
tasks.register("validateContent") {
    group = "verification"
    description = "Valida course.json y fixtures contra course-v1.json o course-v2.json según schemaVersion."
    val root = rootDir.absolutePath
    val schemaV1File = File(root, "docs/schemas/course-v1.json")
    val schemaV2File = File(root, "docs/schemas/course-v2.json")
    val contentFiles: List<File> =
        fileTree(rootDir)
            .matching {
                include("content/*/src/main/assets/courses/*/course.json")
                include("content/*/src/main/assets/courses/*/fixtures/*.json")
            }
            .files
            .toList()
    inputs.files(schemaV1File, schemaV2File)
    inputs.files(contentFiles)
    doLast {
        // Informe didáctico por curso (ADR-042, paso 7.6): la misma métrica
        // que protege ContentRampTest, pero visible en el log. Funs locales
        // (el config cache no serializa referencias del script, ver trampa #1).
        fun frasesDe(text: String): List<String> =
            text.replace(Regex("\\{\\{[^}]+\\}\\}"), "x")
                .split(Regex("[.?!。！？]+"))
                .filter { frase -> frase.isNotBlank() }

        fun longestTokensDe(exercise: JsonNode): Int {
            var longest =
                maxOf(
                    exercise.path("correctTokens").size(),
                    exercise.path("answerTokens").size(),
                )
            for (field in listOf("transcription", "targetPhrase", "modelAnswer", "audioText")) {
                val node = exercise.path(field)
                if (node.isTextual) {
                    for (frase in frasesDe(node.asText())) {
                        longest = maxOf(longest, frase.trim().split(Regex("\\s+")).count { palabra -> palabra.isNotBlank() })
                    }
                }
            }
            val passage = exercise.path("passage")
            if (passage.isTextual) {
                longest = maxOf(
                    longest,
                    passage.asText().replace(Regex("\\{\\{[^}]+\\}\\}"), "x").trim().split(Regex("\\s+")).count { palabra -> palabra.isNotBlank() },
                )
            }
            return longest
        }

        fun summarizeCourse(tree: JsonNode): String? {
            val lessons =
                tree.path("levels")
                    .flatMap { level -> level.path("units").toList() }
                    .flatMap { unit -> unit.path("lessons").toList() }
            if (lessons.isEmpty()) return null

            val buckets =
                listOf("A1", "A2", "B1", "B2", "C1", "C2")
                    .map { cefr ->
                        cefr to lessons.count { lesson -> lesson.path("cefrLevel").asText("n/d") == cefr }
                    }
                    .filter { (_, count) -> count > 0 }

            var exercises = 0
            var freeSpeaking = 0
            var withFeedbackOnError = 0
            var longest = 0
            for (lesson in lessons) {
                for (exercise in lesson.path("exercises")) {
                    exercises++
                    if (exercise.path("type").asText() == "FreeSpeakingPrompt") freeSpeaking++
                    if (exercise.path("feedbackOnError").isObject) withFeedbackOnError++
                    longest = maxOf(longest, longestTokensDe(exercise))
                }
            }
            val coverage = if (exercises == 0) 0 else (withFeedbackOnError * 100.0 / exercises).roundToInt()

            return buildString {
                append("  · ")
                append(tree.path("id").asText())
                append(": ")
                append(lessons.size)
                append(" lecciones")
                if (buckets.isNotEmpty()) {
                    append(" (")
                    append(buckets.joinToString(" ") { (cefr, count) -> "$cefr:$count" })
                    append(")")
                }
                append(" · ")
                append(exercises)
                append(" ejercicios · FreeSpeaking:")
                append(freeSpeaking)
                append(" · feedbackOnError:")
                append(coverage)
                append("% · frase más larga: ")
                append(longest)
                append(" tokens")
            }
        }

        val mapper = ObjectMapper()
        val factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7)
        val schemas =
            mapOf(
                1 to factory.getSchema(schemaV1File.readText()),
                2 to factory.getSchema(schemaV2File.readText()),
            )
        val failures = mutableListOf<String>()
        for (contentFile in contentFiles.sorted()) {
            val relative = contentFile.relativeTo(File(root)).path
            val tree = mapper.readTree(contentFile.readText())
            val version = tree.get("schemaVersion")?.takeIf { node -> node.isInt }?.asInt()
            val schema = version?.let { schemas[it] }
            if (schema == null) {
                println("INVALID: $relative")
                println("  - schemaVersion ausente o no soportado (se esperaba 1 o 2)")
                failures.add(relative)
                continue
            }
            val messages = schema.validate(tree)
            if (messages.isEmpty()) {
                println("VALID (v$version): $relative")
                summarizeCourse(tree)?.let { summary -> println(summary) }
            } else {
                println("INVALID (v$version): $relative")
                messages.forEach { println("  - $it") }
                failures.add(relative)
            }
        }
        if (failures.isNotEmpty()) {
            throw org.gradle.api.GradleException(
                "validateContent: ${failures.size} archivos de contenido no cumplen su schema (ADR-012/ADR-018)",
            )
        }
    }
}



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

// Descarga manual de los modelos de voz (ADR-003/006/010/057).
// TTS: delega en tools/tts/prepare_voices.py, que descarga las voces fijadas
// en models/voices.lock.json, precalcula fonemas con espeak-ng 1.52.0 (solo
// build time) y actualiza la sección `tts` de models/manifest.json.
// STT (Vosk Small): descarga y rellena `files[]` con SHA-256 por archivo.
// Tarea MANUAL: no está conectada al build ni al CI; sin modelos la app
// degrada al TTS/STT del sistema (I3).
tasks.register("fetchVoiceModels") {
    group = "linguago"
    description = "Descarga/prepara los modelos de voz y llena models/manifest.json."
    val modelsRoot = rootDir.absolutePath
    doLast {
        val modelsDir = File(modelsRoot, "models")
        val manifestFile = File(modelsDir, "manifest.json")
        val downloadsDir = File(modelsRoot, ".voice-downloads")
        val baseUrl = "https://alphacephei.com/vosk/models/"
        downloadsDir.mkdirs()

        // --- TTS: fonemas precalculados + voces limpias (ADR-057) ---
        val pythonCandidates =
            listOf("python", "python3", "py").firstOrNull { cmd ->
                runCatching {
                    ProcessBuilder(cmd, "--version")
                        .redirectErrorStream(true)
                        .start()
                        .waitFor() == 0
                }.getOrDefault(false)
            } ?: throw IllegalStateException(
                "Se requiere Python para preparar voces TTS (tools/tts/prepare_voices.py). " +
                    "Instala Python y `pip install -r tools/tts/requirements.txt`.",
            )
        val prepare =
            ProcessBuilder(
                pythonCandidates,
                File(modelsRoot, "tools/tts/prepare_voices.py").absolutePath,
                "--download",
            ).directory(File(modelsRoot))
                .redirectErrorStream(true)
                .start()
        val prepareOut = prepare.inputStream.bufferedReader().readText()
        if (prepare.waitFor() != 0) {
            throw IllegalStateException("prepare_voices.py falló:\n$prepareOut")
        }
        println(prepareOut.trimEnd())

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

// Verificación de licencias de voces TTS (ADR-057). Valida metadatos y
// atribuciones sin requerir que los modelos estén descargados (CI).
tasks.register("validateVoiceLicenses") {
    group = "verification"
    description = "Valida models/voices.lock.json y la coherencia legal de las voces TTS."
    val root = rootDir.absolutePath
    val lockFile = File(root, "models/voices.lock.json")
    val manifestFile = File(root, "models/manifest.json")
    val noticesFile = File(root, "THIRD_PARTY_NOTICES.md")
    inputs.files(lockFile, manifestFile, noticesFile)
    doLast {
        if (!lockFile.isFile) {
            throw org.gradle.api.GradleException("validateVoiceLicenses: falta models/voices.lock.json")
        }
        val mapper = ObjectMapper()
        val lock = mapper.readTree(lockFile)
        val voices = lock.path("voices")
        if (!voices.isArray || voices.size() == 0) {
            throw org.gradle.api.GradleException("validateVoiceLicenses: voices.lock.json sin voces")
        }
        val allowed = setOf("public domain", "cc by 4.0", "cc0", "cc0 1.0")
        val notices = noticesFile.readText()
        for (voice in voices) {
            val id = voice.path("id").asText()
            val license = voice.path("datasetLicense").asText().lowercase()
            require(license.isNotBlank()) { "validateVoiceLicenses: $id sin datasetLicense" }
            require(allowed.any { license.contains(it) }) {
                "validateVoiceLicenses: licencia no permitida para uso comercial en $id: $license"
            }
            require(voice.path("rawSha256").asText().isNotBlank()) { "validateVoiceLicenses: $id sin rawSha256" }
            require(voice.path("attribution").asText().isNotBlank()) { "validateVoiceLicenses: $id sin attribution" }
            require(notices.contains(id)) { "validateVoiceLicenses: $id no aparece en THIRD_PARTY_NOTICES.md" }
        }
        val manifest = mapper.readTree(manifestFile)
        val tts = manifest.path("tts")
        if (tts.isArray) {
            for (entry in tts) {
                val sha = entry.path("sha256").asText()
                if (sha.isNotBlank()) {
                    val files = entry.path("files")
                    if (files.isArray) {
                        for (f in files) {
                            require(f.path("sha256").asText().isNotBlank()) {
                                "validateVoiceLicenses: hash vacío en files[] de ${entry.path("id").asText()}"
                            }
                        }
                    }
                }
            }
        }
        println("VALID: licencias de voces TTS (${voices.size()} voces)")
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

// Informe de avance de la expansión a 1.020 lecciones por idioma (ADR-052).
// No falla el build durante F0: sirve de semáforo para planificar lotes y
// detectar niveles que se quedan atrás. A partir de F1 se puede convertir en
// gate estricto sin cambiar el contrato de contenido.
tasks.register("contentScaleReport") {
    group = "verification"
    description = "Muestra lecciones actuales y hueco restante por curso y nivel CEFR."
    val targetPerLevel = 170
    val contentFiles: List<File> =
        fileTree(rootDir)
            .matching {
                include("content/*/src/main/assets/courses/*/course.json")
            }.files
            .toList()
            .sortedBy { file -> file.path }
    inputs.files(contentFiles)
    doLast {
        val levels = listOf("A1", "A2", "B1", "B2", "C1", "C2")
        val mapper = ObjectMapper()
        var grandTotal = 0
        for (file in contentFiles) {
            val tree = mapper.readTree(file)
            val courseId = tree.path("id").asText(file.parentFile.name)
            val counts = levels.associateWith { 0 }.toMutableMap()
            var exercises = 0
            tree.path("levels").forEach { level ->
                level.path("units").forEach { unit ->
                    unit.path("lessons").forEach { lesson ->
                        val cefr = lesson.path("cefrLevel").asText("A1")
                        counts[cefr] = (counts[cefr] ?: 0) + 1
                        exercises += lesson.path("exercises").size()
                    }
                }
            }
            val total = counts.values.sum()
            grandTotal += total
            println("Curso $courseId: $total lecciones / $exercises ejercicios")
            for (level in levels) {
                val current = counts[level] ?: 0
                val missing = (targetPerLevel - current).coerceAtLeast(0)
                println(
                    "  $level: $current/$targetPerLevel" +
                        if (missing > 0) "  faltan $missing" else "  completo",
                )
            }
            println("  Total: $total/${targetPerLevel * levels.size}")
        }
        println(
            "TOTAL PROGRAMA: $grandTotal/${targetPerLevel * levels.size * contentFiles.size} lecciones " +
                "(${contentFiles.size} cursos)",
        )
    }
}



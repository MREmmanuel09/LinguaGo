package dev.linguago.buildlogic

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.PathSensitivity
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.jvm.toolchain.JavaToolchainService
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.configureEach
import org.gradle.kotlin.dsl.register
import org.jlleitschuh.gradle.ktlint.KtlintExtension

/**
 * Calidad de código para todo módulo Java/Kotlin (I6/DoD):
 *
 * - ktlint (verificación de estilo, usa el .editorconfig del repo)
 * - detekt (análisis estático), ejecutado por CLI como proceso separado.
 *
 * ¿Por qué CLI separado? La versión estable de detekt (1.23.8) carga un
 * compilador Kotlin incrustado incapaz de parsear versiones de JDK ≥24
 * (https://github.com/detekt/detekt/issues/8714); en un proceso aparte sobre
 * un toolchain 21 funciona siempre, sin importar el JDK del daemon.
 */
class QualityConventionPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        project.pluginManager.apply("org.jlleitschuh.gradle.ktlint")

        project.extensions.configure(KtlintExtension::class.java) {
            // ktlint usa el .editorconfig del proyecto.
        }

        registerDetekt(project)
    }

    /** Registra la tarea `detekt` (CLI) y la conecta al ciclo de vida `check`. */
    private fun registerDetekt(project: Project) {
        val detektVersion = project.linguagoCatalog.stringOf("detekt")
        val detektCli = project.configurations.detachedConfiguration(
            project.dependencies
                .create("io.gitlab.arturbosch.detekt:detekt-cli:$detektVersion"),
        )
        val detektConfig = project.rootProject.file("config/detekt/detekt.yml")
        val srcRoot = project.file("src")
        val reportTxt = project.layout.buildDirectory.file("reports/detekt/detekt.txt")
        val reportHtml = project.layout.buildDirectory.file("reports/detekt/detekt.html")

        val detektTask = project.tasks.register("detekt", JavaExec::class.java) {
            group = "verification"
            description = "Análisis estático detekt sobre un toolchain JDK 21."

            onlyIf { srcRoot.exists() }

            inputs.file(detektConfig).withPathSensitivity(PathSensitivity.NONE)
            if (srcRoot.exists()) {
                inputs.files(srcRoot).withPathSensitivity(PathSensitivity.RELATIVE)
                outputs.file(reportTxt)
                outputs.file(reportHtml)
                outputs.cacheIf { true }
            }

            classpath = detektCli
            mainClass.set("io.gitlab.arturbosch.detekt.cli.Main")
            javaLauncher.set(
                project.extensions.getByType(JavaToolchainService::class.java)
                    .launcherFor { languageVersion.set(JavaLanguageVersion.of(DETEKT_JDK)) },
            )
            jvmArgs("-Xmx2g")

            val srcDir = srcRoot.absolutePath
            val txtPath = reportTxt.get().asFile.absolutePath
            val htmlPath = reportHtml.get().asFile.absolutePath
            args(
                "--config", detektConfig.absolutePath,
                "--build-upon-default-config",
                "--input", srcDir,
                "--report", "txt:$txtPath",
                "--report", "html:$htmlPath",
                "--jvm-target", DETEKT_TARGET,
            )
        }

        // Añade `detekt` a `check` sin asumir qué plugin lo crea (java o AGP).
        project.tasks.matching { it.name == "check" }.configureEach {
            dependsOn(detektTask)
        }
    }
}

/** JDK que el detekt estable (≤1.23.x) exige para ejecutar su CLI. */
private const val DETEKT_JDK = 21

/** Target de análisis: compatible con la lista admitida por detekt 1.23.x. */
private const val DETEKT_TARGET = "21"

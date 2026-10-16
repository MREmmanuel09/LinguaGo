package dev.linguago.buildlogic

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension

/**
 * Stack completo para un módulo feature (ADR-001 y ADR-002):
 * Compose + Hilt + Lifecycle + core-ui.
 *
 * Nota: cada módulo feature declara el BOM de Compose a nivel de build file
 * (`implementation(platform(libs.compose.bom))`), ya que la función
 * `platform()` del DSL de Kotlin solo está disponible en scripts de build
 * (DependencyHandlerScope) y no en código fuente de plugins.
 */
class AndroidFeatureConventionPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        val catalog = project.extensions
            .getByType(VersionCatalogsExtension::class.java)
            .named("libs")
        val lifecycleVersion = catalog.stringOf("lifecycle")
        val junitVersion = catalog.stringOf("junit")

        project.pluginManager.apply("linguago.android.library.compose")
        project.pluginManager.apply("linguago.hilt")

        project.dependencies.apply {
            add("implementation", "androidx.compose.ui:ui")
            add("implementation", "androidx.compose.ui:ui-tooling-preview")
            add("implementation", "androidx.compose.material3:material3")
            add("implementation", "androidx.compose.foundation:foundation")
            add("implementation", "androidx.lifecycle:lifecycle-runtime-compose:$lifecycleVersion")
            add("implementation", "androidx.lifecycle:lifecycle-viewmodel-compose:$lifecycleVersion")
            add("implementation", project(":core:core-ui"))
            add("debugApi", "androidx.compose.ui:ui-tooling")
            add("testImplementation", "junit:junit:$junitVersion")
        }
    }
}

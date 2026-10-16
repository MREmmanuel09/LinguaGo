package dev.linguago.buildlogic

import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

/**
 * Habilita Jetpack Compose sobre una librería Android: aplica el plugin de
 * compilador de Compose (pinned en el catálogo a la versión de Kotlin) y
 * activa el build feature.
 */
class AndroidLibraryComposeConventionPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        project.pluginManager.apply("linguago.android.library")
        project.pluginManager.apply("org.jetbrains.kotlin.plugin.compose")

        project.configure<LibraryExtension> {
            buildFeatures {
                compose = true
            }
        }
    }
}

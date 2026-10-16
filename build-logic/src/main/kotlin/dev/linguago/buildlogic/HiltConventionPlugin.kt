package dev.linguago.buildlogic

import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Aplica Hilt + KSP y registra las dependencias de compilación desde el
 * catálogo (evita repetir versiones en cada módulo).
 */
class HiltConventionPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        val hiltVersion = project.linguagoCatalog.stringOf("hilt")
        project.pluginManager.apply("com.google.devtools.ksp")
        project.pluginManager.apply("com.google.dagger.hilt.android")

        project.dependencies.apply {
            add("implementation", "com.google.dagger:hilt-android:$hiltVersion")
            add("ksp", "com.google.dagger:hilt-compiler:$hiltVersion")
        }
    }
}

package dev.linguago.buildlogic

import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

/**
 * Config base para todos los módulos de librería Android (ADR-001):
 * compileSdk 37 / minSdk 26 / JVM 17.
 */
class AndroidLibraryConventionPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        project.pluginManager.apply("com.android.library")
        project.pluginManager.apply("linguago.quality")

        project.configure<LibraryExtension> {
            compileSdk = ANDROID_COMPILE_SDK
            defaultConfig {
                minSdk = ANDROID_MIN_SDK
            }
            compileOptions {
                sourceCompatibility = JavaVersion.VERSION_17
                targetCompatibility = JavaVersion.VERSION_17
            }
        }
    }

    private companion object {
        const val ANDROID_COMPILE_SDK = 37
        const val ANDROID_MIN_SDK = 26
    }
}

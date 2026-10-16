package dev.linguago.buildlogic

import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

/**
 * Config base del módulo :app (ADR-001) — incluye Compose ya que la raíz
 * de UI de la app es Compose de punta a punta.
 */
class AndroidApplicationConventionPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        project.pluginManager.apply("com.android.application")
        project.pluginManager.apply("linguago.quality")
        project.pluginManager.apply("org.jetbrains.kotlin.plugin.compose")

        project.configure<ApplicationExtension> {
            compileSdk = ANDROID_COMPILE_SDK
            defaultConfig {
                minSdk = ANDROID_MIN_SDK
                targetSdk = ANDROID_TARGET_SDK
            }
            buildFeatures {
                compose = true
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
        const val ANDROID_TARGET_SDK = 37
    }
}

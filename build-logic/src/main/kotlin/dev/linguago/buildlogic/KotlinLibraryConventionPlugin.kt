package dev.linguago.buildlogic

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

/**
 * Módulos Kotlin puros (core-domain, core-common): sin dependencias Android.
 */
class KotlinLibraryConventionPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        project.pluginManager.apply("org.jetbrains.kotlin.jvm")
        project.pluginManager.apply("linguago.quality")

        project.configure<KotlinJvmProjectExtension> {
            compilerOptions {
                jvmTarget.set(JvmTarget.JVM_17)
            }
        }

        // Alinea el release de Java con el target de Kotlin para evitar la
        // validación de inconsistencia JVM-target (aunque JDK local sea más
        // moderno, javac compila con --release 17).
        project.tasks.withType(JavaCompile::class.java).configureEach {
            options.release.set(17)
        }
    }
}

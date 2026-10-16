package dev.linguago.buildlogic

import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

/**
 * Módulos content-*: assets puros (JSON del contenido pedagógico) sin lógica,
 * según la regla de dependencia "content no depende de nada" (ADR-001).
 */
class ContentModuleConventionPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        project.pluginManager.apply("linguago.android.library")

        project.configure<LibraryExtension> {
            defaultConfig {
                // El contenido debe poder ser consumido por cualquier app sin
                // requerir reglas ProGuard extra.
                consumerProguardFiles()
            }
        }
    }
}

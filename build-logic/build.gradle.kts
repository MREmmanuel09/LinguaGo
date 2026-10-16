import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `kotlin-dsl`
}

group = "dev.linguago.buildlogic"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

dependencies {
    compileOnly(libs.gradlePlugin.android)
    compileOnly(libs.gradlePlugin.kotlin)
    compileOnly(libs.gradlePlugin.composeCompiler)
    compileOnly(libs.gradlePlugin.ksp)
    compileOnly(libs.gradlePlugin.hilt)
    compileOnly(libs.gradlePlugin.ktlint)
    compileOnly(libs.gradlePlugin.detekt)
}

gradlePlugin {
    plugins {
        register("linguago-android-library") {
            id = "linguago.android.library"
            implementationClass = "dev.linguago.buildlogic.AndroidLibraryConventionPlugin"
        }
        register("linguago-android-library-compose") {
            id = "linguago.android.library.compose"
            implementationClass = "dev.linguago.buildlogic.AndroidLibraryComposeConventionPlugin"
        }
        register("linguago-android-application") {
            id = "linguago.android.application"
            implementationClass = "dev.linguago.buildlogic.AndroidApplicationConventionPlugin"
        }
        register("linguago-android-feature") {
            id = "linguago.android.feature"
            implementationClass = "dev.linguago.buildlogic.AndroidFeatureConventionPlugin"
        }
        register("linguago-kotlin-library") {
            id = "linguago.kotlin.library"
            implementationClass = "dev.linguago.buildlogic.KotlinLibraryConventionPlugin"
        }
        register("linguago-hilt") {
            id = "linguago.hilt"
            implementationClass = "dev.linguago.buildlogic.HiltConventionPlugin"
        }
        register("linguago-quality") {
            id = "linguago.quality"
            implementationClass = "dev.linguago.buildlogic.QualityConventionPlugin"
        }
        register("linguago-content-module") {
            id = "linguago.content.module"
            implementationClass = "dev.linguago.buildlogic.ContentModuleConventionPlugin"
        }
    }
}

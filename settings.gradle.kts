pluginManagement {
    includeBuild("build-logic")

    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    // Descarga automática de toolchains (p.ej. JDK 21 para detekt) cuando
    // el JDK local no calza con lo que exige una herramienta.
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // sherpa-onnx Android AAR se distribuye vía JitPack (ADR-003).
        maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = "LinguaGo"

include(":app")
include(":baselineprofile")

include(
    ":core:core-domain",
    ":core:core-ui",
    ":core:core-data",
    ":core:core-content",
    ":core:core-audio",
    ":core:core-sync",
    ":core:core-common",
    ":core:core-auth",
    // QA de release (F4.3): sesión de 20 min + exploración de puntos débiles.
    ":core:qa",
)

include(
    ":feature:feature-onboarding",
    ":feature:feature-auth",
    ":feature:feature-home",
    ":feature:feature-lesson",
    ":feature:feature-profile",
    ":feature:feature-sync",
    ":feature:feature-settings",
    ":feature:feature-review",
)

include(
    ":content:content-es-en",
    ":content:content-es-pt",
    ":content:content-es-fr",
    ":content:content-es-ja",
)

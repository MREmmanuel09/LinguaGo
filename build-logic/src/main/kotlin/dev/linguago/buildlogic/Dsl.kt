package dev.linguago.buildlogic

import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension

/**
 * Catálogo compartido `libs` disponible en cada proyecto (ver
 * build-logic/settings.gradle.kts). Los base de datos de versiones se usan
 * por los convention plugins para cualquier dato de forma dinámica sin
 * duplicar el artefacto.
 */
internal val Project.linguagoCatalog: VersionCatalog
    get() = extensions.getByType(VersionCatalogsExtension::class.java).named("libs")

internal fun VersionCatalog.stringOf(name: String): String =
    findVersion(name).orElseThrow { IllegalArgumentException("Version $name no encontrada en/libs") }
        .requiredVersion

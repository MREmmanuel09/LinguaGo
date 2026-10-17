package dev.linguago.core.sync

import dagger.MapKey

/**
 * Clave del mapa de proveedores cloud (Strategy, ADR-011): varias fuentes
 * registran implementaciones (`google_drive`, `dev_local`) y el selector
 * elige la primera vinculada.
 */
@MapKey
public annotation class CloudProviderKey(
    val value: String,
)

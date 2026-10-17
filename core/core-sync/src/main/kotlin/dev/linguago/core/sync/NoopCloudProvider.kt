package dev.linguago.core.sync

import dev.linguago.core.common.LinguaError
import dev.linguago.core.common.Result
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Proveedor cloud placeholder (Fase 3): ninguna cuenta vinculada todavía.
 * La integración real de Google Drive/OneDrive llega en Fase 4 (ADR-011);
 * mientras tanto, el app completa funciona 100% offline (I1, I2) y la UI
 * muestra el respaldo como desactivado (I3).
 */
@Singleton
public class NoopCloudProvider
    @Inject
    constructor() : CloudProvider {
        override val id: String = "none"

        override fun isLinked(): Boolean = false

        override suspend fun fetchSnapshot(): Result<ByteArray> = Result.error(LinguaError.NotLinked)

        override suspend fun pushSnapshot(payload: ByteArray): Result<Unit> = Result.error(LinguaError.NotLinked)
    }

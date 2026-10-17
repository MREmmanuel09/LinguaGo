package dev.linguago.core.sync

import dev.linguago.core.common.Result
import dev.linguago.core.domain.model.StreakState
import dev.linguago.core.domain.repository.ProgressRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Motor de sync (ADR-013): respaldo = snapshot local → nube; restauración =
 * nube → decode → merge monótono con lo local. No conoce proveedores
 * concretos (Strategy, CloudProvider) ni persistencia (ProgressRepository).
 */
@Singleton
public class SyncEngine
    @Inject
    constructor(
        private val progressRepository: ProgressRepository,
        private val codec: SnapshotCodec,
        private val cloudProvider: CloudProvider,
    ) {
        /** Construye el snapshot del progreso local y lo sube al proveedor. */
        public suspend fun backup(): Result<Unit> =
            when (val snapshot = progressRepository.snapshot()) {
                is Result.Error -> snapshot
                is Result.Success ->
                    when (val encoded = codec.encode(snapshot.value)) {
                        is Result.Error -> encoded
                        is Result.Success -> cloudProvider.pushSnapshot(encoded.value)
                    }
            }

        /** Baja el snapshot remoto y lo restaura con merge monótono (I4). */
        public suspend fun restore(): Result<StreakState> =
            when (val remote = cloudProvider.fetchSnapshot()) {
                is Result.Error -> remote
                is Result.Success ->
                    when (val decoded = codec.decode(remote.value)) {
                        is Result.Error -> decoded
                        is Result.Success -> progressRepository.restore(decoded.value)
                    }
            }
    }

package dev.linguago.core.sync

import dev.linguago.core.common.CryptoService
import dev.linguago.core.common.LinguaError
import dev.linguago.core.common.Result
import dev.linguago.core.domain.model.StreakState
import dev.linguago.core.domain.repository.ProgressRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Motor de sync (ADR-013): respaldo = snapshot local → cifrado → nube;
 * restauración = nube → descifrado → merge monótono con lo local. No conoce
 * proveedores concretos (Strategy, CloudProvider) ni persistencia
 * (ProgressRepository). El cifrado es un paso de transporte (ADR-014): el
 * payload cifrado es opaco fuera del dispositivo.
 */
@Singleton
public class SyncEngine
    @Inject
    constructor(
        private val progressRepository: ProgressRepository,
        private val codec: SnapshotCodec,
        private val crypto: CryptoService,
        private val cloudProvider: CloudProvider,
    ) {
        /** Construye el snapshot del progreso local, lo cifra y lo sube. */
        public suspend fun backup(): Result<Unit> =
            when (val snapshot = progressRepository.snapshot()) {
                is Result.Error -> snapshot
                is Result.Success ->
                    when (val encoded = codec.encode(snapshot.value)) {
                        is Result.Error -> encoded
                        is Result.Success ->
                            when (val encrypted = crypto.encrypt(encoded.value)) {
                                null -> Result.error(LinguaError.InvalidContent)
                                else -> cloudProvider.pushSnapshot(encrypted)
                            }
                    }
            }

        /** Baja el snapshot remoto, lo descifra y lo restaura (merge monótono, I4). */
        public suspend fun restore(): Result<StreakState> =
            when (val remote = cloudProvider.fetchSnapshot()) {
                is Result.Error -> remote
                is Result.Success ->
                    when (val decrypted = crypto.decrypt(remote.value)) {
                        null -> Result.error(LinguaError.InvalidContent)
                        else ->
                            when (val decoded = codec.decode(decrypted)) {
                                is Result.Error -> decoded
                                is Result.Success -> progressRepository.restore(decoded.value)
                            }
                    }
            }
    }

package dev.linguago.core.sync

import dev.linguago.core.common.CryptoService
import dev.linguago.core.common.LinguaError
import dev.linguago.core.common.Result
import dev.linguago.core.common.flatMap
import dev.linguago.core.domain.model.StreakState
import dev.linguago.core.domain.repository.ProgressRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Motor de sync (ADR-013/ADR-050): respaldo = snapshot local → cifrado → nube;
 * restauración = nube → descifrado → merge monótono con lo local. No conoce
 * proveedores concretos (Strategy, CloudProvider) ni persistencia
 * (ProgressRepository). El cifrado es un paso de transporte (ADR-014): el
 * payload cifrado es opaco fuera del dispositivo.
 *
 * La exportación/importación local reutiliza el mismo codec y merge, pero
 * entrega JSON legible para SAF (sin cuenta ni red).
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
            when (val encoded = exportSnapshot()) {
                is Result.Error -> encoded
                is Result.Success -> cloudProvider.pushSnapshot(crypto.encrypt(encoded.value))
            }

        /** Baja el snapshot remoto, lo descifra y lo restaura (merge monótono, I4). */
        public suspend fun restore(): Result<StreakState> =
            when (val remote = cloudProvider.fetchSnapshot()) {
                is Result.Error -> remote
                is Result.Success ->
                    when (val decrypted = crypto.decrypt(remote.value)) {
                        null -> Result.error(LinguaError.InvalidContent)
                        else -> importSnapshot(decrypted)
                    }
            }

        /** Serializa el snapshot local a JSON UTF-8 para exportación SAF. */
        public suspend fun exportSnapshot(): Result<ByteArray> =
            progressRepository
                .snapshot()
                .flatMap { snapshot -> codec.encode(snapshot) }

        /** Decodifica un JSON local y lo aplica con merge monótono (I4). */
        public suspend fun importSnapshot(payload: ByteArray): Result<StreakState> =
            codec.decode(payload).flatMap { snapshot -> progressRepository.restore(snapshot) }
    }

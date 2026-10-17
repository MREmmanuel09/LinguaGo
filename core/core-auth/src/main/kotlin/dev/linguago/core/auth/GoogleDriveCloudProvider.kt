package dev.linguago.core.auth

import dev.linguago.core.common.LinguaError
import dev.linguago.core.common.Result
import dev.linguago.core.sync.CloudProvider
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Proveedor real de respaldo (ADR-011): Google Drive `appDataFolder` vía la
 * REST API con tokens OAuth gestionados por `OAuthEngine`. Sustituible
 * (Strategy, ADR-003): SyncEngine no sabe nada de Google.
 */
@Singleton
public class GoogleDriveCloudProvider
    @Inject
    constructor(
        private val restClient: GoogleDriveRestClient,
        private val tokenStore: TokenReader,
    ) : CloudProvider {
        override val id: String get() = PROVIDER_ID

        override fun isLinked(): Boolean = tokenStore.hasTokens()

        override suspend fun fetchSnapshot(): Result<ByteArray> =
            when (val found = restClient.findSnapshotId()) {
                is Result.Error -> found
                is Result.Success ->
                    when (found.value) {
                        null -> Result.error(LinguaError.NotFound)
                        else -> restClient.downloadSnapshot(found.value!!)
                    }
            }

        override suspend fun pushSnapshot(payload: ByteArray): Result<Unit> =
            when (tokenStore.hasTokens()) {
                false -> Result.error(LinguaError.NotLinked)
                true -> restClient.uploadSnapshot(payload)
            }

        private companion object {
            const val PROVIDER_ID = "google_drive"
        }
    }

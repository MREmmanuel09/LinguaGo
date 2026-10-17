package dev.linguago.core.auth

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.linguago.core.common.DispatcherProvider
import dev.linguago.core.common.LinguaError
import dev.linguago.core.common.Result
import dev.linguago.core.sync.CloudProvider
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Proveedor de laboratorio (ADR-011): mismo contrato `CloudProvider`, pero
 * respaldando en el propio dispositivo. Existe para probar el flujo
 * completo auth → sync en el emulador sin necesidad de credenciales reales;
 * los binds DI lo registran solo en builds debug.
 */
@Singleton
public class DevCloudProvider
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
        private val dispatchers: DispatcherProvider,
    ) : CloudProvider {
        override val id: String get() = PROVIDER_ID

        override fun isLinked(): Boolean = markerFile.exists()

        override suspend fun fetchSnapshot(): Result<ByteArray> =
            withContext(dispatchers.io) {
                when {
                    !isLinked() -> Result.error(LinguaError.NotLinked)
                    snapshotFile.exists() -> Result.success(snapshotFile.readBytes())
                    else -> Result.error(LinguaError.NotFound)
                }
            }

        override suspend fun pushSnapshot(payload: ByteArray): Result<Unit> =
            withContext(dispatchers.io) {
                when {
                    !isLinked() -> Result.error(LinguaError.NotLinked)
                    else -> {
                        snapshotFile.writeBytes(payload)
                        Result.success(Unit)
                    }
                }
            }

        /** Vincula/desvincula el proveedor de lab (operación instantánea). */
        public suspend fun link(enabled: Boolean) {
            withContext(dispatchers.io) {
                when (enabled) {
                    true -> markerFile.createNewFile()
                    false -> markerFile.delete()
                }
                Unit
            }
        }

        private val markerFile: File = File(context.filesDir, "dev-cloud.linked")

        private val snapshotFile: File = File(context.filesDir, "dev-cloud-snapshot.json")

        private companion object {
            const val PROVIDER_ID = "dev_local"
        }
    }

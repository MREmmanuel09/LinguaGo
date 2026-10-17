package dev.linguago.core.sync

import dev.linguago.core.common.Result
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Selector de proveedor (ADR-011): toma el primer proveedor real con sesión
 * vinculada del mapa y delega la contrato en él; si no hay ninguno, degrada
 * a NoopCloudProvider (la app sigue 100% offline, I3). El orden preferido
 * es `dev_local` (laboratorio, solo debug) antes que `google_drive`.
 */
@Singleton
public class DelegatingCloudProvider
    @Inject
    constructor(
        providers: Map<String, @JvmSuppressWildcards CloudProvider>,
    ) : CloudProvider {
        private val providers: Map<String, CloudProvider> = providers

        override val id: String
            get() = active().id

        override fun isLinked(): Boolean = active().isLinked()

        override suspend fun fetchSnapshot(): Result<ByteArray> = active().fetchSnapshot()

        override suspend fun pushSnapshot(payload: ByteArray): Result<Unit> = active().pushSnapshot(payload)

        /** Re-evalúa en cada acceso para reflejar link/unlink al vuelo. */
        private fun active(): CloudProvider =
            providers[PRIORITY_DEV]
                ?.takeIf(CloudProvider::isLinked)
                ?: providers[PRIORITY_GOOGLE]
                    ?.takeIf(CloudProvider::isLinked)
                ?: providers[PRIORITY_GOOGLE]
                ?: providers[PRIORITY_DEV]
                ?: NoopCloudProvider()

        private companion object {
            const val PRIORITY_DEV = "dev_local"
            const val PRIORITY_GOOGLE = "google_drive"
        }
    }

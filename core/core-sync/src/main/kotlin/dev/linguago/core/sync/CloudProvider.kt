package dev.linguago.core.sync

/**
 * Contrato de proveedor cloud (RFC §6.1): Google Drive (`appDataFolder`) y
 * OneDrive (`appfolder`) son implementaciones intercambiables vía Strategy.
 * La app funciona 100% offline incluso si ninguno está conectado (I1, I2).
 */
interface CloudProvider {
    /** Identificador humano del proveedor (`google_drive`, `onedrive`). */
    val id: String

    /** Indica si existe una sesión vinculada con este proveedor. */
    fun isLinked(): Boolean

    /** Descarga el snapshot cifrado remoto si existe. */
    suspend fun fetchSnapshot(): Result<ByteArray>

    /** Sube el snapshot cifrado local. */
    suspend fun pushSnapshot(payload: ByteArray): Result<Unit>
}

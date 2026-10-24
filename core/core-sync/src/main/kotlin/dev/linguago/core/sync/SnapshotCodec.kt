package dev.linguago.core.sync

import dev.linguago.core.common.LinguaError
import dev.linguago.core.common.Result
import dev.linguago.core.domain.model.SyncSnapshot
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Codec JSON del snapshot (ADR-013): parseo estricto (sin claves extra) y
 * verificación de versión del formato. Se aceptan versiones anteriores para
 * no perder respaldos existentes (v1 → 10 vidas por defecto, ADR-025); una
 * versión futura se rechaza como InvalidContent (I4: nunca aplica mutaciones
 * de otro formato).
 */
@Singleton
public class SnapshotCodec
    @Inject
    constructor() {
        private val json =
            Json {
                encodeDefaults = true
                ignoreUnknownKeys = false
            }

        /** Serializa el snapshot a bytes UTF-8. */
        public fun encode(snapshot: SyncSnapshot): Result<ByteArray> =
            try {
                Result.success(
                    json
                        .encodeToString(SyncSnapshot.serializer(), snapshot)
                        .toByteArray(Charsets.UTF_8),
                )
            } catch (error: SerializationException) {
                Result.error(LinguaError.InvalidContent)
            }

        /** Decodifica y valida la versión; error si es otro formato. */
        public fun decode(payload: ByteArray): Result<SyncSnapshot> =
            try {
                val decoded =
                    json.decodeFromString(
                        SyncSnapshot.serializer(),
                        payload.toString(Charsets.UTF_8),
                    )
                val supported = decoded.schemaVersion in LEGACY_MIN_VERSION..SyncSnapshot.SCHEMA_VERSION
                if (supported) {
                    Result.success(decoded)
                } else {
                    Result.error(LinguaError.InvalidContent)
                }
            } catch (error: SerializationException) {
                Result.error(LinguaError.InvalidContent)
            } catch (error: IllegalArgumentException) {
                Result.error(LinguaError.InvalidContent)
            }

        private companion object {
            /** Versión mínima aceptada (v1 sin vidas, ADR-013). */
            const val LEGACY_MIN_VERSION = 1
        }
    }

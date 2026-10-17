package dev.linguago.core.auth

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.linguago.core.common.CryptoService
import dev.linguago.core.common.DispatcherProvider
import dev.linguago.core.common.LinguaError
import dev.linguago.core.common.Result
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Almacén de tokens OAuth (ADR-011): DataStore privado de la app con una
 * única entrada serializada. Mantiene una copia en memoria para que las
 * consultas sincronas (`isLinked()`) no tengan que entrar al disco.
 */
@Singleton
public class TokenStore
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
        private val json: Json,
        private val crypto: CryptoService,
        private val dispatchers: DispatcherProvider,
    ) : TokenReader {
        private val scope = CoroutineScope(SupervisorJob() + dispatchers.io)
        private val dataStore: DataStore<Preferences> =
            PreferenceDataStoreFactory.create(
                scope = scope,
                produceFile = { preferencesFile() },
            )

        private val _state = MutableStateFlow<StoredTokens?>(null)

        /** Estado de sesión observable por la UI (link/unlink por sesión). */
        public val state: StateFlow<StoredTokens?> = _state.asStateFlow()

        init {
            scope.launch { reload() }
        }

        /** Copia sincrónica (solo lectura rápida, nunca hace I/O). */
        public fun cached(): StoredTokens? = _state.value

        /** Sesión persistida actual para los motores OAuth. */
        override suspend fun tokens(): StoredTokens? = cached()

        /** Consulta sincrónica de vinculación (I3: no hace I/O ni red). */
        override fun hasTokens(): Boolean = cached() != null

        /** Persiste una sesión cifrada; emite el cambio inmediatamente. */
        public suspend fun save(tokens: StoredTokens): Result<Unit> =
            withContext(dispatchers.io) {
                try {
                    val plaintext = json.encodeToString(StoredTokens.serializer(), tokens)
                    val ciphertext = crypto.encrypt(plaintext.toByteArray(Charsets.UTF_8))
                    dataStore.edit { preferences ->
                        preferences[TOKEN_KEY] = ciphertext.toHex()
                    }
                    _state.value = tokens
                    Result.success(Unit)
                } catch (error: Exception) {
                    Result.error(LinguaError.Unknown)
                }
            }

        /** Borra la sesión (desvinculación); no borra datos de progreso. */
        public suspend fun clear(): Result<Unit> =
            withContext(dispatchers.io) {
                try {
                    dataStore.edit { preferences ->
                        preferences.remove(TOKEN_KEY)
                    }
                    _state.value = null
                    Result.success(Unit)
                } catch (error: Exception) {
                    Result.error(LinguaError.Unknown)
                }
            }

        private suspend fun reload() {
            val tokens =
                try {
                    val preferences = dataStore.data.first()
                    preferences[TOKEN_KEY]?.let { text ->
                        val ciphertext = text.hexToByteArray()
                        val plaintext = crypto.decrypt(ciphertext)?.toString(Charsets.UTF_8)
                        plaintext?.let { json.decodeFromString(StoredTokens.serializer(), it) }
                            ?: migrateLegacy(text)
                    }
                } catch (error: Exception) {
                    null
                }
            _state.value = tokens
        }

        /** Tokens en claro (pre-ADR-014) se re-cifran en el primer acceso. */
        private suspend fun migrateLegacy(text: String): StoredTokens? =
            try {
                val tokens = json.decodeFromString(StoredTokens.serializer(), text)
                save(tokens)
                tokens
            } catch (error: Exception) {
                null
            }

        private fun ByteArray.toHex(): String = joinToString("") { byte -> "%02x".format(byte) }

        private fun String.hexToByteArray(): ByteArray = chunked(2).map { it.toInt(16).toByte() }.toByteArray()

        private fun preferencesFile(): File =
            File(context.filesDir, "datastore")
                .also(File::mkdirs)
                .resolve("linguago_auth.preferences_pb")

        private companion object {
            val TOKEN_KEY = stringPreferencesKey("tokens_json")
        }
    }

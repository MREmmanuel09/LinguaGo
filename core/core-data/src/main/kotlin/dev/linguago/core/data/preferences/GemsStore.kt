package dev.linguago.core.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/** DataStore dedicado a las gemas (ADR-055). */
private val Context.gemsDataStore by preferencesDataStore(name = "linguago_gems")

/**
 * Gemas del usuario (ADR-055): divisa del prototipo persistida en DataStore,
 * **no en Room** (I4: sin cambio de schema). El snapshot de sincronización la
 * fusiona con máximo, así que `applyMax` nunca puede reducir el saldo.
 */
@Singleton
public class GemsStore
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) {
        private val key = intPreferencesKey("gems_current")

        /** Flujo reactivo del saldo de gemas. */
        public fun observe(): Flow<Int> =
            context.gemsDataStore.data
                .catch { e -> emit(recoveryPreferences(e)) }
                .map { preferences -> preferences[key] ?: 0 }

        /** Saldo actual (lectura puntual para snapshots). */
        public suspend fun current(): Int = observe().first()

        /** Suma gemas (premio de lección). */
        public suspend fun add(amount: Int) {
            if (amount <= 0) return
            context.gemsDataStore.edit { preferences ->
                preferences[key] = (preferences[key] ?: 0) + amount
            }
        }

        /** Aplica el máximo entre el saldo local y uno externo (merge I4). */
        public suspend fun applyMax(value: Int) {
            context.gemsDataStore.edit { preferences ->
                preferences[key] = maxOf(preferences[key] ?: 0, value)
            }
        }

        private fun recoveryPreferences(throwable: Throwable): Preferences =
            if (throwable is IOException) emptyPreferences() else throw throwable
    }

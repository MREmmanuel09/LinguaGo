package dev.linguago.core.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.linguago.core.domain.model.ThemeMode
import dev.linguago.core.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/** Instancia única de DataStore para todas las preferencias del usuario. */
private val Context.userPreferences by preferencesDataStore(name = "linguago_user_preferences")

/**
 * Preferencias del usuario sobre DataStore (ADR-005). Son transitorias en
 * alcance: NO almacenan progreso (eso es Room, I4).
 */
@Singleton
public class SettingsRepositoryImpl
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) : SettingsRepository {
        override val selectedCourseId: Flow<String?> =
            context.userPreferences.data
                .catch { e -> emit(recoveryPreferences(e)) }
                .map { preferences -> preferences[PrefKeys.SELECTED_COURSE_ID] }

        override val themeMode: Flow<ThemeMode> =
            context.userPreferences.data
                .catch { e -> emit(recoveryPreferences(e)) }
                .map { preferences -> decodeThemeMode(preferences[PrefKeys.THEME_MODE]) }

        override suspend fun updateSelectedCourse(courseId: String?) {
            context.userPreferences.edit { preferences ->
                if (courseId == null) {
                    preferences.remove(PrefKeys.SELECTED_COURSE_ID)
                } else {
                    preferences[PrefKeys.SELECTED_COURSE_ID] = courseId
                }
            }
        }

        override suspend fun updateThemeMode(mode: ThemeMode) {
            context.userPreferences.edit { preferences ->
                preferences[PrefKeys.THEME_MODE] = mode.name
            }
        }

        private fun recoveryPreferences(throwable: Throwable): Preferences =
            if (throwable is IOException) emptyPreferences() else throw throwable

        private fun decodeThemeMode(value: String?): ThemeMode =
            value?.let { stored -> ThemeMode.entries.firstOrNull { it.name == stored } } ?: ThemeMode.SYSTEM

        private object PrefKeys {
            val SELECTED_COURSE_ID = stringPreferencesKey("selected_course_id")
            val THEME_MODE = stringPreferencesKey("theme_mode")
        }
    }

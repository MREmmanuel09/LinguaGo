package dev.linguago.core.domain.repository

import dev.linguago.core.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow

/**
 * Contrato de preferencias del usuario (RFC §3.1, DataStore — ADR-005).
 * La implementación vive en core-data; el dominio solo expone lenguaje
 * de negocio sobre el estado almacenado.
 */
public interface SettingsRepository {
    /** Id del curso activo del usuario, o null si aún no selecciona ninguno. */
    public val selectedCourseId: Flow<String?>

    /** Modo de tema visual preferido. */
    public val themeMode: Flow<ThemeMode>

    /** Guarda el curso activo (null deselecciona, regresando al onboarding). */
    public suspend fun updateSelectedCourse(courseId: String?)

    /** Guarda el modo de tema visual. */
    public suspend fun updateThemeMode(mode: ThemeMode)
}

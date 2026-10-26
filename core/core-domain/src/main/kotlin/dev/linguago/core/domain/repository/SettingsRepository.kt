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

    /** `true` si el usuario quiere efectos de sonido (ADR-027). */
    public val soundEffectsEnabled: Flow<Boolean>

    /** `true` si el usuario quiere feedback háptico (ADR-027). */
    public val hapticsEnabled: Flow<Boolean>

    /** Guarda el curso activo (null deselecciona, regresando al onboarding). */
    public suspend fun updateSelectedCourse(courseId: String?)

    /** Guarda el modo de tema visual. */
    public suspend fun updateThemeMode(mode: ThemeMode)

    /** Activa o desactiva los efectos de sonido. */
    public suspend fun updateSoundEffectsEnabled(enabled: Boolean)

    /** Activa o desactiva el feedback háptico. */
    public suspend fun updateHapticsEnabled(enabled: Boolean)
}

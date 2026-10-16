package dev.linguago.core.data

/**
 * Contratos de nombres y constantes de la capa de datos (ADR-005).
 * La implementación de Room/DataStore llega en la Fase 1; este módulo fija
 * el vocabulario compartido para no romper consumers.
 */
object LinguaDataContract {
    /** Nombre del archivo de base de datos Room. */
    const val DATABASE_NAME = "linguago.db"

    /** Nombre del archivo DataStore de preferencias. */
    const val PREFERENCES_NAME = "linguago_preferences"

    /** Versión del esquema de datos con el que se generó este build. */
    const val SCHEMA_VERSION = 0
}

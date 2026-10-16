package dev.linguago.core.common

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/**
 * Puente de dispatchers para inyectabilidad (invariante I7): los UseCases
 * cooperan sobre estas abstracciones, nunca sobre `Dispatchers.*`
 * directamente, de modo que los tests puedan correr bajo
 * `StandardTestDispatcher`.
 */
public interface DispatcherProvider {
    public val io: CoroutineDispatcher
    public val default: CoroutineDispatcher
    public val main: CoroutineDispatcher
}

/** Implementación por defecto sobre los dispatchers del runtime coroutines. */
public class DefaultDispatcherProvider : DispatcherProvider {
    override val io: CoroutineDispatcher
        get() = Dispatchers.IO
    override val default: CoroutineDispatcher
        get() = Dispatchers.Default
    override val main: CoroutineDispatcher
        get() = Dispatchers.Main
}

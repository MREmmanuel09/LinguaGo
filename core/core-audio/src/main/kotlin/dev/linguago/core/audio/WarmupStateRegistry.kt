package dev.linguago.core.audio

import dev.linguago.core.domain.model.Language
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.ConcurrentHashMap

/**
 * Estado de warmup por idioma compartido por los motores audio (ADR-020):
 * un `StateFlow<AudioEngineState>` observable desde Compose y un Mutex por
 * idioma que serializa la inicialización nativa. [warmUpOnce] ejecuta el
 * bloque una sola vez por racha: quien llega segundo espera el lock y sale
 * al ver el idioma ya en [AudioEngineState.Ready].
 */
internal class WarmupStateRegistry {
    private val states = ConcurrentHashMap<Language, MutableStateFlow<AudioEngineState>>()
    private val locks = ConcurrentHashMap<Language, Mutex>()

    /** Estado del idioma; creado (Idle) la primera vez que se consulta. */
    fun stateOf(language: Language): MutableStateFlow<AudioEngineState> =
        states.computeIfAbsent(language) { MutableStateFlow(AudioEngineState.Idle) }

    /** `true` si el idioma ya quedó listo tras un warmup previo. */
    fun isReady(language: Language): Boolean = stateOf(language).value is AudioEngineState.Ready

    /** Vuelve a [AudioEngineState.Idle] tras liberar el recurso nativo. */
    fun reset(language: Language) {
        stateOf(language).value = AudioEngineState.Idle
    }

    /** Ejecuta [block] bajo el lock del idioma salvo que ya esté listo. */
    suspend fun warmUpOnce(
        language: Language,
        block: suspend () -> Unit,
    ) {
        if (isReady(language)) return
        locks.computeIfAbsent(language) { Mutex() }.withLock {
            if (isReady(language)) return@withLock
            block()
        }
    }
}

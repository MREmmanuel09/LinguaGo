package dev.linguago.core.audio

import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/**
 * Carril serializado por idioma para un recurso nativo compartido
 * (ADR-022). [use] mantiene el lock mientras el recurso está en uso, de modo
 * que una cancelación libera el carril en cuanto termina el bloque y
 * [close] espera a que no haya operaciones activas antes de liberar el
 * puntero JNA/JNI. Un recurso instalado tras [close] se cierra al instante.
 */
internal class NativeSerialLane<T>(
    private val closer: (T) -> Unit,
) {
    private val lock = Mutex()
    private var resource: T? = null
    private var closed: Boolean = false

    /** Instala [value]; `false` si el carril ya fue cerrado. */
    suspend fun install(value: T): Boolean =
        lock.withLock {
            if (closed) {
                closeSafely(value)
                return@withLock false
            }
            val previous = resource
            resource = value
            if (previous != null && previous !== value) closeSafely(previous)
            true
        }

    /** Ejecuta [block] con el recurso bajo el lock; `null` si no está listo. */
    suspend fun <R> use(block: suspend (T) -> R): R? =
        lock.withLock {
            val current = resource
            if (closed || current == null) return@withLock null
            block(current)
        }

    /** Cierra el recurso cuando la operación activa (si existe) termina. */
    suspend fun close() {
        withContext(NonCancellable) {
            lock.withLock {
                closed = true
                resource?.let { closeSafely(it) }
                resource = null
            }
        }
    }

    private fun closeSafely(value: T) {
        runCatching { closer(value) }
    }
}

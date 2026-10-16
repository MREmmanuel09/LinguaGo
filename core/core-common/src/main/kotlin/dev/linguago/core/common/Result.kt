package dev.linguago.core.common

/**
 * Resultado tipado del sistema (invariante I7). Comunica entre capas con
 * estado explícito (éxito/error), sin excepciones a través de la frontera.
 */
public sealed interface Result<out T> {
    public data class Success<T>(
        val value: T,
    ) : Result<T>

    public data class Error(
        val error: LinguaError,
    ) : Result<Nothing>

    public companion object {
        /** Crea un resultado de éxito. */
        public fun <T> success(value: T): Result<T> = Success(value)

        /** Crea un resultado de error. */
        public fun error(error: LinguaError): Result<Nothing> = Error(error)
    }
}

/** Transforma el valor en caso de éxito, propagando el error tal cual. */
public inline fun <T, R> Result<T>.map(transform: (T) -> R): Result<R> =
    when (this) {
        is Result.Success -> Result.Success(transform(value))
        is Result.Error -> this
    }

/** Devuelve el valor o `null` si es un error. */
public fun <T> Result<T>.getOrNull(): T? =
    when (this) {
        is Result.Success -> value
        is Result.Error -> null
    }

/** Plegado explícito: exactamente uno de los dos handlers se ejecuta. */
public inline fun <T, R> Result<T>.fold(
    onSuccess: (T) -> R,
    onError: (LinguaError) -> R,
): R =
    when (this) {
        is Result.Success -> onSuccess(value)
        is Result.Error -> onError(error)
    }

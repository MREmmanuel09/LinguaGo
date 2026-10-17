package dev.linguago.core.auth

import dev.linguago.core.common.DispatcherProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/** Config falsa compartida por los tests de auth (apunta a MockWebServer). */
internal class FakeConfigSource(
    override val clientId: String?,
    override val tokenUrl: String,
    override val driveApiBase: String = "https://www.googleapis.com",
) : OAuthConfigSource

/** TokenReader falsa configurable por los tests. */
internal class FakeTokenReader : TokenReader {
    public var token: StoredTokens? = null

    override suspend fun tokens(): StoredTokens? = token

    override fun hasTokens(): Boolean = token != null
}

/** Dispatchers inline: el I/O corre en el hilo del test (sin bloqueo). */
internal class TestDispatchers : DispatcherProvider {
    override val io: CoroutineDispatcher = Dispatchers.Unconfined
    override val default: CoroutineDispatcher = Dispatchers.Unconfined
    override val main: CoroutineDispatcher = Dispatchers.Unconfined
}

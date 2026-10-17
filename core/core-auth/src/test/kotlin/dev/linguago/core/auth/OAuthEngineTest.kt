package dev.linguago.core.auth

import dev.linguago.core.common.LinguaError
import dev.linguago.core.common.Result
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/** Motor OAuth contra un servidor de memoria (ADR-011). */
public class OAuthEngineTest {
    private lateinit var server: MockWebServer
    private lateinit var config: FakeConfigSource
    private lateinit var reader: FakeTokenReader

    @Before
    public fun setUp() {
        server = MockWebServer()
        server.start()
        config =
            FakeConfigSource(
                clientId = "test-client-id.apps.googleusercontent.com",
                tokenUrl = server.url("/token").toString(),
            )
        reader = FakeTokenReader()
    }

    @After
    public fun tearDown() {
        server.shutdown()
    }

    @Test
    public fun `createLaunch incluye pkce state y redirect`() =
        runTest {
            val engine = engine()
            val launch = (engine.createLaunch() as Result.Success).value
            assertTrue(launch.url.contains("code_challenge_method=S256"))
            assertTrue(launch.url.contains("response_type=code"))
            assertTrue(launch.url.contains("drive.appdata"))
            assertTrue(launch.codeVerifier.length >= 64)
        }

    @Test
    public fun `createLaunch sin client id es NotConfigured`() =
        runTest {
            config = FakeConfigSource(clientId = null, tokenUrl = server.url("/token").toString())
            val engine = engine()
            val error = engine.createLaunch() as Result.Error
            assertEquals(LinguaError.NotConfigured, error.error)
        }

    @Test
    public fun `exchange guarda refresh y expiración`() =
        runTest {
            server.enqueue(
                MockResponse()
                    .setResponseCode(200)
                    .setBody(
                        """{"access_token":"at-1","refresh_token":"rt-1","expires_in":3600}""",
                    ),
            )
            val engine = engine()
            val tokens =
                (engine.exchange("code-1", "verifier-1") as Result.Success).value
            assertEquals("at-1", tokens.accessToken)
            assertEquals("rt-1", tokens.refreshToken)
            assertTrue(tokens.expiresAtMillis > System.currentTimeMillis())
        }

    @Test
    public fun `refresh conserva el refresh token si el IdP no lo rota`() =
        runTest {
            server.enqueue(
                MockResponse().setBody("""{"access_token":"at-2","expires_in":3600}"""),
            )
            reader.token =
                StoredTokens(
                    accessToken = "at-0",
                    refreshToken = "rt-0",
                    expiresAtMillis = 0L,
                )
            val engine = engine()
            val refreshed =
                (engine.refreshToken(reader.token!!) as Result.Success).value
            assertEquals("at-2", refreshed.accessToken)
            assertEquals("rt-0", refreshed.refreshToken)
        }

    @Test
    public fun `accessToken refresca si está por vencer`() =
        runTest {
            server.enqueue(
                MockResponse().setBody("""{"access_token":"at-2","expires_in":3600}"""),
            )
            reader.token =
                StoredTokens(
                    accessToken = "at-1",
                    refreshToken = "rt-1",
                    expiresAtMillis = System.currentTimeMillis() - 1000,
                )
            val engine = engine()
            val token = (engine.accessToken() as Result.Success).value
            assertEquals("at-2", token)
        }

    @Test
    public fun `accessToken sin sesión es NotLinked`() =
        runTest {
            val engine = engine()
            assertEquals(LinguaError.NotLinked, (engine.accessToken() as Result.Error).error)
        }

    private fun engine(): OAuthEngine =
        OAuthEngine(
            okHttp = OkHttpClient(),
            config = config,
            pkce = PkceGenerator(java.security.SecureRandom(), TestDispatchers()),
            tokens = reader,
            json = Json { ignoreUnknownKeys = true },
            dispatchers = TestDispatchers(),
        )
}

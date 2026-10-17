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
import org.junit.Before
import org.junit.Test

/** Cliente Drive contra un servidor de memoria (ADR-011). */
public class GoogleDriveRestClientTest {
    private lateinit var server: MockWebServer
    private lateinit var config: FakeConfigSource

    @Before
    public fun setUp() {
        server = MockWebServer()
        server.start()
        config =
            FakeConfigSource(
                clientId = "test-client-id",
                tokenUrl = server.url("/token").toString(),
                driveApiBase = server.url("/").toString().trimEnd('/'),
            )
    }

    @After
    public fun tearDown() {
        server.shutdown()
    }

    @Test
    public fun `findSnapshotId devuelve el id del archivo existente`() =
        runTest {
            server.enqueue(
                MockResponse()
                    .setBody("""{"files":[{"id":"file-1","name":"linguago-snapshot.json"}]}"""),
            )
            val client = client()
            val result = client.findSnapshotId() as Result.Success
            assertEquals("file-1", result.value)
        }

    @Test
    public fun `findSnapshotId vacio devuelve null`() =
        runTest {
            server.enqueue(MockResponse().setBody("""{"files":[]}"""))
            val client = client()
            val result = client.findSnapshotId() as Result.Success
            assertEquals(null, result.value)
        }

    @Test
    public fun `downloadSnapshot trae los bytes`() =
        runTest {
            server.enqueue(
                MockResponse().setBody("CONTENIDO"),
            )
            val client = client()
            val result = client.downloadSnapshot("file-1") as Result.Success
            assertEquals("CONTENIDO", String(result.value))
        }

    @Test
    public fun `uploadSnapshot sin conectividad result Network`() =
        runTest {
            server.enqueue(
                MockResponse()
                    .setResponseCode(401)
                    .setBody("""{"error":"invalid_client"}"""),
            )
            val client = client()
            val result = client.uploadSnapshot("PAYLOAD".toByteArray())
            assertEquals(LinguaError.Network, (result as? Result.Error)?.error)
        }

    private fun client(): GoogleDriveRestClient =
        GoogleDriveRestClient(
            okHttp = OkHttpClient(),
            engine =
                OAuthEngine(
                    okHttp = OkHttpClient(),
                    config = config,
                    pkce = PkceGenerator(java.security.SecureRandom(), TestDispatchers()),
                    tokens =
                        FakeTokenReader().also {
                            it.token =
                                StoredTokens(
                                    accessToken = "token-test",
                                    refreshToken = "refresh-test",
                                    expiresAtMillis = System.currentTimeMillis() + 3_600_000,
                                )
                        },
                    json = Json { ignoreUnknownKeys = true },
                    dispatchers = TestDispatchers(),
                ),
            config = config,
            json = Json { ignoreUnknownKeys = true },
            dispatchers = TestDispatchers(),
        )
}

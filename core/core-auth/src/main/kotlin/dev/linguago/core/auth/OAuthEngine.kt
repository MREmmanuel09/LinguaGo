package dev.linguago.core.auth

import dev.linguago.core.common.DispatcherProvider
import dev.linguago.core.common.LinguaError
import dev.linguago.core.common.Result
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URLEncoder
import javax.inject.Inject
import javax.inject.Singleton

/** Respuesta JSON del endpoint de token (RFC 6749 §5.1). */
@Serializable
private data class TokenResponse(
    @SerialName("access_token") public val accessToken: String,
    @SerialName("refresh_token") public val refreshToken: String? = null,
    @SerialName("expires_in") public val expiresInSeconds: Long = 3600L,
)

/**
 * Motor OAuth 2.0 con PKCE (ADR-011): construye la URL de autorización,
 * canjea el `code` por tokens y refresca cuando el access token está por
 * vencer. Sin dependencia de Play Services: HTTP puro con OkHttp.
 */
@Singleton
public class OAuthEngine
    @Inject
    constructor(
        private val okHttp: OkHttpClient,
        private val config: OAuthConfigSource,
        private val pkce: PkceGenerator,
        private val tokens: TokenReader,
        private val json: Json,
        private val dispatchers: DispatcherProvider,
    ) {
        /** Access token vigente, refrescando bajo demanda (sin bloqueo). */
        public suspend fun accessToken(): Result<String> =
            when (val current = tokens.tokens()) {
                null -> Result.error(LinguaError.NotLinked)
                else ->
                    when {
                        current.isExpiredAt(System.currentTimeMillis()) ->
                            when (val refreshed = refreshToken(current)) {
                                is Result.Error -> refreshed
                                is Result.Success -> Result.success(refreshed.value.accessToken)
                            }
                        else -> Result.success(current.accessToken)
                    }
            }

        /** URL lista para abrir en el navegador (Custom Tab o ACTION_VIEW). */
        public suspend fun createLaunch(): Result<OAuthLaunch> {
            val clientId = config.clientId
            if (clientId.isNullOrBlank()) return Result.error(LinguaError.NotConfigured)
            val (verifier, codeChallenge) = pkce.generate()
            val url =
                config.authorizeUrl
                    .plus("?response_type=code")
                    .plus("&client_id=${encode(clientId)}")
                    .plus("&redirect_uri=${encode(redirectUri())}")
                    .plus("&scope=${encode(config.scope)}")
                    .plus("&state=${encode(stateFor(verifier))}")
                    .plus("&code_challenge=$codeChallenge")
                    .plus("&code_challenge_method=S256")
                    .plus("&access_type=offline")
                    .plus("&prompt=consent")
            return Result.success(OAuthLaunch(url = url, codeVerifier = verifier))
        }

        /** Canjea el código del redirect por una sesión nueva. */
        public suspend fun exchange(
            code: String,
            codeVerifier: String,
        ): Result<StoredTokens> =
            requestTokens(
                previous = tokens.tokens(),
                form =
                    FormBody
                        .Builder()
                        .add("grant_type", "authorization_code")
                        .add("code", code)
                        .add("code_verifier", codeVerifier)
                        .add("client_id", config.clientId ?: "")
                        .add("redirect_uri", redirectUri())
                        .build(),
            )

        /** Refresca el access token con el refresh token actual. */
        public suspend fun refreshToken(current: StoredTokens): Result<StoredTokens> =
            requestTokens(
                previous = current,
                form =
                    FormBody
                        .Builder()
                        .add("grant_type", "refresh_token")
                        .add("refresh_token", current.refreshToken)
                        .add("client_id", config.clientId ?: "")
                        .build(),
            )

        // ---- HTTP ----

        private suspend fun requestTokens(
            previous: StoredTokens?,
            form: FormBody,
        ): Result<StoredTokens> =
            withContext(dispatchers.io) {
                try {
                    val request =
                        Request
                            .Builder()
                            .url(config.tokenUrl)
                            .post(form)
                            .build()
                    okHttp.newCall(request).execute().use { response ->
                        when (response.body) {
                            null -> Result.error(LinguaError.Network)
                            else ->
                                when (response.isSuccessful) {
                                    false ->
                                        Result.error(
                                            if (response.code in 400..499) {
                                                LinguaError.NotConfigured
                                            } else {
                                                LinguaError.Unknown
                                            },
                                        )
                                    true -> {
                                        val tokensResponse =
                                            json.decodeFromString(
                                                TokenResponse.serializer(),
                                                response.body!!.string(),
                                            )
                                        Result.success(
                                            mergeTokens(previous, tokensResponse),
                                        )
                                    }
                                }
                        }
                    }
                } catch (error: Exception) {
                    Result.error(LinguaError.Network)
                }
            }

        /** El refresh token nuevo gana; si el IdP no lo rotó, se conserva. */
        private fun mergeTokens(
            previous: StoredTokens?,
            response: TokenResponse,
        ): StoredTokens =
            StoredTokens(
                accessToken = response.accessToken,
                refreshToken = response.refreshToken ?: previous?.refreshToken ?: "",
                expiresAtMillis =
                    System.currentTimeMillis() + response.expiresInSeconds * 1000,
                accountLabel = previous?.accountLabel,
            )

        // ---- helpers ----

        private fun redirectUri(): String = "${config.redirectScheme}://${config.redirectHost}"

        /** Estado derivado del verifier (reversible sin una tabla adicional). */
        private fun stateFor(verifier: String): String = verifier.takeLast(16)

        private fun encode(value: String): String =
            URLEncoder
                .encode(value, "UTF-8")
                .replace("+", "%20")
    }

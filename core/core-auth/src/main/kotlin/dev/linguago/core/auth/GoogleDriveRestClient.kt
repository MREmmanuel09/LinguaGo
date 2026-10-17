package dev.linguago.core.auth

import dev.linguago.core.common.DispatcherProvider
import dev.linguago.core.common.LinguaError
import dev.linguago.core.common.Result
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.net.URLEncoder
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Cliente REST de Google Drive (ADR-011) con `appDataFolder`: el snapshot
 * vive en un archivo único dentro del sandbox privado de la app en Drive;
 * por diseño el contenido no es accesible ni para Google ni otras apps
 * (scope `drive.appdata`).
 */
@Singleton
public class GoogleDriveRestClient
    @Inject
    constructor(
        private val okHttp: OkHttpClient,
        private val engine: OAuthEngine,
        private val config: OAuthConfigSource,
        private val json: Json,
        private val dispatchers: DispatcherProvider,
    ) {
        /** Busca el id del snapshot remoto (`null` si aún no existe). */
        public suspend fun findSnapshotId(): Result<String?> =
            when (val token = engine.accessToken()) {
                is Result.Error -> Result.error(token.error)
                is Result.Success -> listFiles(token.value)
            }

        /** Baja el contenido bruto del snapshot remoto. */
        public suspend fun downloadSnapshot(id: String): Result<ByteArray> =
            when (val token = engine.accessToken()) {
                is Result.Error -> Result.error(token.error)
                is Result.Success ->
                    withContext(dispatchers.io) {
                        runCatching {
                            val request =
                                authorized(token.value)
                                    .url(mediaUrl(id))
                                    .get()
                                    .build()
                            okHttp.newCall(request).execute().use { response ->
                                val body = response.body?.bytes()
                                when {
                                    !response.isSuccessful -> Result.error(LinguaError.Network)
                                    body == null -> Result.error(LinguaError.Network)
                                    else -> Result.success(body)
                                }
                            }
                        }.getOrElse { Result.error(LinguaError.Network) }
                    }
            }

        /** Crea o actualiza el snapshot remoto con el payload dado. */
        public suspend fun uploadSnapshot(payload: ByteArray): Result<Unit> =
            when (val token = engine.accessToken()) {
                is Result.Error -> Result.error(token.error)
                is Result.Success -> upsertSnapshot(token.value, payload)
            }

        // ---- interno ----

        private suspend fun upsertSnapshot(
            token: String,
            payload: ByteArray,
        ): Result<Unit> =
            when (val existing = findSnapshotId()) {
                is Result.Error -> Result.error(existing.error)
                is Result.Success ->
                    when (val id = existing.value) {
                        null -> createSnapshot(token, payload)
                        else -> setSnapshotContent(token, id, payload)
                    }
            }

        private suspend fun listFiles(token: String): Result<String?> =
            withContext(dispatchers.io) {
                runCatching {
                    val query =
                        "name = '${config.snapshotFileName}' and trashed = false"
                    val url =
                        "${config.driveApiBase}/drive/v3/files" +
                            "?spaces=appDataFolder" +
                            "&q=${URLEncoder.encode(query, "UTF-8")}" +
                            "&fields=files(id,name)"
                    val request =
                        authorized(token)
                            .url(url)
                            .get()
                            .build()
                    okHttp.newCall(request).execute().use { response ->
                        val raw = response.body?.string()
                        when {
                            !response.isSuccessful -> Result.error(LinguaError.Network)
                            raw == null -> Result.error(LinguaError.Network)
                            else -> {
                                val id =
                                    json
                                        .parseToJsonElement(raw)
                                        .jsonObject["files"]
                                        ?.jsonArray
                                        ?.firstOrNull()
                                        ?.jsonObject
                                        ?.get("id")
                                        ?.jsonPrimitive
                                        ?.content
                                Result.success(id)
                            }
                        }
                    }
                }.getOrElse { Result.error(LinguaError.Network) }
            }

        /** Crea el archivo de metadatos (vacío) dentro de appDataFolder. */
        private suspend fun createSnapshot(
            token: String,
            payload: ByteArray,
        ): Result<Unit> =
            withContext(dispatchers.io) {
                runCatching {
                    val metadata =
                        buildJsonObject {
                            put("name", config.snapshotFileName)
                            put(
                                "parents",
                                buildJsonArray { add(JsonPrimitive("appDataFolder")) },
                            )
                        }
                    val body =
                        json
                            .encodeToString(JsonObject.serializer(), metadata)
                            .toRequestBody(JSON_MEDIA_TYPE.toMediaType())
                    val createRequest =
                        authorized(token)
                            .url("${config.driveApiBase}/drive/v3/files")
                            .post(body)
                            .build()
                    val createdId =
                        okHttp.newCall(createRequest).execute().use { response ->
                            val raw = response.body?.string()
                            when {
                                !response.isSuccessful -> null
                                raw == null -> null
                                else ->
                                    json
                                        .parseToJsonElement(raw)
                                        .jsonObject["id"]
                                        ?.jsonPrimitive
                                        ?.content
                            }
                        }
                    when (createdId) {
                        null -> Result.error(LinguaError.Network)
                        else -> setSnapshotContent(token, createdId, payload)
                    }
                }.getOrElse { Result.error(LinguaError.Network) }
            }

        /** PATCH media: reemplaza el contenido del archivo ya creado. */
        private suspend fun setSnapshotContent(
            token: String,
            id: String,
            payload: ByteArray,
        ): Result<Unit> =
            withContext(dispatchers.io) {
                runCatching {
                    val url =
                        "${config.driveApiBase}/upload/drive/v3/files/$id" +
                            "?uploadType=media"
                    val request =
                        authorized(token)
                            .url(url)
                            .patch(payload.toRequestBody(BINARY_MEDIA_TYPE.toMediaType()))
                            .build()
                    okHttp.newCall(request).execute().use { response ->
                        when {
                            response.isSuccessful -> Result.success(Unit)
                            else -> Result.error(LinguaError.Network)
                        }
                    }
                }.getOrElse { Result.error(LinguaError.Network) }
            }

        private fun authorized(token: String): Request.Builder = Request.Builder().addHeader("Authorization", "Bearer $token")

        private fun mediaUrl(id: String): String = "${config.driveApiBase}/drive/v3/files/$id?alt=media"

        private companion object {
            const val JSON_MEDIA_TYPE = "application/json"
            const val BINARY_MEDIA_TYPE = "application/octet-stream"
        }
    }

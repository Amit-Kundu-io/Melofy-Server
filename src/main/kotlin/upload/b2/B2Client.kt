package com.plugins.upload.b2

/**
 * B2Client.kt
 *
 * Thin wrapper around the Backblaze B2 native API v3 for large-file
 * (multipart) uploads. This is the ONLY file in the server that knows the
 * B2 wire format — everything above it (repository, routes) talks in
 * plain Kotlin types.
 *
 * Architecture note: with the "semi-direct" upload flow, this client never
 * receives or forwards part BYTES. It only issues short-lived, scoped
 * upload URLs (getUploadPartUrl) that the Android client then PUTs
 * directly to B2. That keeps multi-GB payloads off this server entirely —
 * the server's job is session bookkeeping, not data relay.
 *
 * Fixes vs. the previous version:
 *  - Auth token is cached WITH an expiry. B2 tokens are valid ~24h; the
 *    old code cached forever, so every call after ~24h uptime would 401
 *    until process restart. Cached for 12h here (half B2's window) as a
 *    safety margin — re-authorizes proactively rather than waiting to be
 *    told the token is dead.
 *  - Every call checks response.status.isSuccess() before parsing the
 *    body. Previously a 400 from B2 (e.g. bad bucketId, expired part URL)
 *    would surface as a confusing kotlinx-serialization exception instead
 *    of a clear B2ApiException with the actual B2 error code/message.
 */


import com.plugins.storage.upload.b2.B2ApiException
import com.plugins.storage.upload.b2.B2AuthResponse
import com.plugins.storage.upload.b2.B2StartLargeFileResponse
import com.plugins.storage.upload.b2.B2UploadPartUrlResponse
import com.plugins.storage.upload.b2.FinishLargeFileRequest
import com.plugins.storage.upload.b2.GetUploadPartUrlRequest
import com.plugins.storage.upload.b2.ListPartsRequest
import com.plugins.storage.upload.b2.StartLargeFileRequest
import com.plugins.upload.models.FinishLargeFileResponse.FinishLargeFileResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.util.Base64

class B2Client(
    private val keyId: String,
    private val appKey: String,
    private val bucketId: String,
    private val httpClient: HttpClient
) {
    private companion object {
        // B2 authorization tokens are valid ~24h. Re-authorize after half
        // that so we're never caught by surprise mid-request.
        const val AUTH_TTL_MILLIS = 12 * 3600_000L
    }

    private data class CachedAuth(val auth: B2AuthResponse, val obtainedAtMillis: Long)

    private var cached: CachedAuth? = null
    private val authMutex = Mutex()

    private suspend fun authorize(): B2AuthResponse {
        cached?.let { if (System.currentTimeMillis() - it.obtainedAtMillis < AUTH_TTL_MILLIS) return it.auth }

        // Mutex avoids a stampede of concurrent re-authorize calls when
        // many uploads hit the expiry boundary at once.
        return authMutex.withLock {
            cached?.let { if (System.currentTimeMillis() - it.obtainedAtMillis < AUTH_TTL_MILLIS) return it.auth }

            val credentials = Base64.getEncoder().encodeToString("$keyId:$appKey".toByteArray())
            val response = httpClient.get("https://api.backblazeb2.com/b2api/v3/b2_authorize_account") {
                header(HttpHeaders.Authorization, "Basic $credentials")
            }
            val auth = response.bodyOrThrow<B2AuthResponse>()
            cached = CachedAuth(auth, System.currentTimeMillis())
            auth
        }
    }

    suspend fun startLargeFile(fileName: String, contentType: String): B2StartLargeFileResponse {
        val auth = authorize()
        val response = httpClient.post("${auth.apiUrl}/b2api/v3/b2_start_large_file") {
            header(HttpHeaders.Authorization, auth.authorizationToken)
            contentType(ContentType.Application.Json)
            setBody(StartLargeFileRequest(bucketId, fileName, contentType))
        }
        return response.bodyOrThrow()
    }

    suspend fun getUploadPartUrl(fileId: String): B2UploadPartUrlResponse {
        val auth = authorize()
        val response = httpClient.post("${auth.apiUrl}/b2api/v3/b2_get_upload_part_url") {
            header(HttpHeaders.Authorization, auth.authorizationToken)
            contentType(ContentType.Application.Json)
            setBody(GetUploadPartUrlRequest(fileId))
        }
        return response.bodyOrThrow()
    }

    suspend fun finishLargeFile(
        fileId: String,
        partSha1Array: List<String>
    ): FinishLargeFileResponse {

        val auth = authorize()

        val response = httpClient.post(
            "${auth.apiUrl}/b2api/v3/b2_finish_large_file"
        ) {
            header(HttpHeaders.Authorization, auth.authorizationToken)
            contentType(ContentType.Application.Json)

            setBody(
                FinishLargeFileRequest(
                    fileId = fileId,
                    partSha1Array = partSha1Array
                )
            )
        }

        response.throwIfNotSuccess()

        return response.body()
    }

    suspend fun listPartsWithSha1(fileId: String): Map<Int, String> {
        val auth = authorize()
        val response = httpClient.post("${auth.apiUrl}/b2api/v3/b2_list_parts") {
            header(HttpHeaders.Authorization, auth.authorizationToken)
            contentType(ContentType.Application.Json)
            setBody(ListPartsRequest(fileId = fileId, maxPartCount = 1000))
        }
        response.throwIfNotSuccess()
        val json = Json.parseToJsonElement(response.bodyAsText())
        return json.jsonObject["parts"]!!.jsonArray.associate {
            it.jsonObject["partNumber"]!!.jsonPrimitive.int to
                it.jsonObject["contentSha1"]!!.jsonPrimitive.content
        }
    }

    // ---- response helpers -------------------------------------------------

    private suspend inline fun <reified T> HttpResponse.bodyOrThrow(): T {
        throwIfNotSuccess()
        return body()
    }
    suspend fun getDownloadUrl(fileName: String): String {
        val auth = authorize()
        return "${auth.downloadUrl}/file/Video-Ak/$fileName"
    }


    private suspend fun HttpResponse.throwIfNotSuccess() {
        if (status.isSuccess()) return
        val raw = bodyAsText()
        val (code, message) = runCatching {
            val obj = Json.parseToJsonElement(raw).jsonObject
            obj["code"]?.jsonPrimitive?.content to obj["message"]?.jsonPrimitive?.content
        }.getOrDefault(null to null)
        throw B2ApiException(status.value, code, message, raw)
    }
}

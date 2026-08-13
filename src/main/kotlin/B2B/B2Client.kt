package B2B

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.util.Base64

/**
 * B2 native API v3 auth response. The v2 API returned apiUrl/downloadUrl
 * as top-level fields; v3 nests them under apiInfo.storageApi. The
 * computed properties below preserve the old auth.apiUrl / auth.downloadUrl
 * call sites so nothing else in this file needs to change.
 *
 * https://www.backblaze.com/apidocs/b2-authorize-account (v3 response shape)
 */
@Serializable
data class B2StorageApiInfo(
    val apiUrl: String,
    val downloadUrl: String,
    val bucketId: String? = null,
    val bucketName: String? = null,
    val recommendedPartSize: Long? = null,
    val absoluteMinimumPartSize: Long? = null,
    val s3ApiUrl: String? = null
)

@Serializable
data class B2ApiInfo(
    val storageApi: B2StorageApiInfo
)

@Serializable
data class B2AuthResponse(
    val authorizationToken: String,
    val apiInfo: B2ApiInfo,
    val accountId: String? = null
) {
    val apiUrl: String get() = apiInfo.storageApi.apiUrl
    val downloadUrl: String get() = apiInfo.storageApi.downloadUrl
}

@Serializable
data class B2StartLargeFileResponse(val fileId: String, val fileName: String)

@Serializable
data class B2UploadPartUrlResponse(val uploadUrl: String, val authorizationToken: String)

@Serializable
data class B2UploadPartResponse(val partNumber: Int, val contentSha1: String, val contentLength: Long)

/**
 * Request bodies below are typed @Serializable classes rather than
 * mapOf(...) literals. mapOf(...) with mixed-type values (e.g. a String
 * and an Int in the same map) forces Kotlin to widen the map's value
 * type to the nearest common supertype, which isn't a concrete
 * serializable type — Json.encodeToString(...) then fails at runtime
 * with "Serializer for class 'Any' is not found." A typed data class
 * always has a generated serializer, so this can't happen.
 */
@Serializable
private data class StartLargeFileRequest(val bucketId: String, val fileName: String, val contentType: String)

@Serializable
private data class GetUploadPartUrlRequest(val fileId: String)

@Serializable
private data class FinishLargeFileRequest(val fileId: String, val partSha1Array: List<String>)

@Serializable
private data class ListPartsRequest(val fileId: String, val maxPartCount: Int)

class B2Client(
    private val keyId: String,
    private val appKey: String,
    private val bucketId: String,
    private val httpClient: HttpClient
) {
    private var cachedAuth: B2AuthResponse? = null

    /**
     * NOTE: B2 authorization tokens expire (typically after 24h). Caching
     * forever means every call after expiry will start failing with 401s
     * until the process restarts. If you see 401s after the app has been
     * running a while, that's why — consider caching with a timestamp and
     * re-authorizing once it's stale, e.g.:
     *
     * private var cachedAuth: B2AuthResponse? = null
     * private var cachedAt: Long = 0
     * ... if (cachedAuth != null && System.currentTimeMillis() - cachedAt < 12 * 3600_000) return cachedAuth!!
     */
    private suspend fun authorize(): B2AuthResponse {
        cachedAuth?.let { return it }
        val credentials = Base64.getEncoder().encodeToString("$keyId:$appKey".toByteArray())
        val response = httpClient.get("https://api.backblazeb2.com/b2api/v3/b2_authorize_account") {
            header(HttpHeaders.Authorization, "Basic $credentials")
        }
        val auth = response.body<B2AuthResponse>()
        cachedAuth = auth
        return auth
    }

    suspend fun startLargeFile(fileName: String, contentType: String): B2StartLargeFileResponse {
        val auth = authorize()
        val response = httpClient.post("${auth.apiUrl}/b2api/v3/b2_start_large_file") {
            header(HttpHeaders.Authorization, auth.authorizationToken)
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(StartLargeFileRequest(bucketId, fileName, contentType)))
        }
        return response.body()
    }

    suspend fun getUploadPartUrl(fileId: String): B2UploadPartUrlResponse {
        val auth = authorize()
        val response = httpClient.post("${auth.apiUrl}/b2api/v3/b2_get_upload_part_url") {
            header(HttpHeaders.Authorization, auth.authorizationToken)
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(GetUploadPartUrlRequest(fileId)))
        }
        return response.body()
    }

    suspend fun uploadPart(
        uploadUrl: String,
        uploadAuthToken: String,
        partNumber: Int,
        sha1: String,
        contentLength: Long,
        bytes: ByteArray
    ): B2UploadPartResponse {
        val response = httpClient.post(uploadUrl) {
            header(HttpHeaders.Authorization, uploadAuthToken)
            header("X-Bz-Part-Number", partNumber.toString())
            header("X-Bz-Content-Sha1", sha1)
            header(HttpHeaders.ContentLength, contentLength.toString())
            setBody(bytes)
        }
        return response.body()
    }

    suspend fun finishLargeFile(fileId: String, partSha1Array: List<String>): Boolean {
        val auth = authorize()
        val response = httpClient.post("${auth.apiUrl}/b2api/v3/b2_finish_large_file") {
            header(HttpHeaders.Authorization, auth.authorizationToken)
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(FinishLargeFileRequest(fileId, partSha1Array)))
        }
        return response.status.isSuccess()
    }

    suspend fun listPartsWithSha1(fileId: String): Map<Int, String> {
        val auth = authorize()
        val response = httpClient.post("${auth.apiUrl}/b2api/v3/b2_list_parts") {
            header(HttpHeaders.Authorization, auth.authorizationToken)
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(ListPartsRequest(fileId, maxPartCount = 1000)))
        }
        val json = Json.parseToJsonElement(response.bodyAsText())
        return json.jsonObject["parts"]!!.jsonArray.associate {
            it.jsonObject["partNumber"]!!.jsonPrimitive.int to
                    it.jsonObject["contentSha1"]!!.jsonPrimitive.content
        }
    }
}
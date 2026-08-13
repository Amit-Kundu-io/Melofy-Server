package com.plugins.Backblaze_B2.apis

import com.plugins.Backblaze_B2.Models.GetDownloadAuthorizationRequest
import com.plugins.Backblaze_B2.Models.GetDownloadAuthorizationResponse
import com.plugins.Backblaze_B2.di.BackblazeConfig
import com.plugins.Backblaze_B2.storage.Chunk
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.basicAuth
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.content.OutgoingContent
import io.ktor.http.contentType
import io.ktor.utils.io.ByteWriteChannel
import io.ktor.utils.io.writeFully
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class BackblazeApiImpl(
    private val client: HttpClient,
    private val config: BackblazeConfig
) : BackblazeApi {

    private companion object {
        const val AUTHORIZE = "https://api.backblazeb2.com/b2api/v4/b2_authorize_account"
        const val START_LARGE_FILE = "/b2api/v4/b2_start_large_file"
        const val GET_UPLOAD_PART_URL = "/b2api/v4/b2_get_upload_part_url"
        const val FINISH_LARGE_FILE = "/b2api/v4/b2_finish_large_file"
        const val CANCEL_LARGE_FILE = "/b2api/v4/b2_cancel_large_file"
        const val LIST_PARTS = "/b2api/v4/b2_list_parts"
        const val GET_DOWNLOAD_AUTHORIZATION = "/b2api/v4/b2_get_download_authorization"
        const val HEADER_PART_NUMBER = "X-Bz-Part-Number"
        const val HEADER_CONTENT_SHA1 = "X-Bz-Content-Sha1"
    }

    private var cachedAuthorization: AuthorizeAccountResponse? = null
    private val authMutex = Mutex()

    override suspend fun authorize(): AuthorizeAccountResponse =
        client.get(AUTHORIZE) {
            basicAuth(username = config.keyId, password = config.applicationKey)
        }.body()

    private suspend fun getAuthorization(forceRefresh: Boolean = false): AuthorizeAccountResponse {
        if (!forceRefresh) {
            cachedAuthorization?.let { return it }
        }

        return authMutex.withLock {
            if (!forceRefresh) {
                cachedAuthorization?.let { return@withLock it }
            }
            val authorization = authorize()
            cachedAuthorization = authorization
            authorization
        }
    }

    override suspend fun startLargeFile(request: StartLargeFileRequest): StartLargeFileResponse {
        val auth = getAuthorization()
        val requestBody = request.copy(bucketId = config.bucketId)

        return client.post("${auth.apiInfo.storageApi.apiUrl}$START_LARGE_FILE") {
            header(HttpHeaders.Authorization, auth.authorizationToken)
            contentType(ContentType.Application.Json)
            setBody(requestBody)
        }.body()
    }

    override suspend fun getUploadPartUrl(request: GetUploadPartUrlRequest): GetUploadPartUrlResponse {
        val auth = getAuthorization()

        return client.post("${auth.apiInfo.storageApi.apiUrl}$GET_UPLOAD_PART_URL") {
            header(HttpHeaders.Authorization, auth.authorizationToken)
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    override suspend fun uploadPart(
        uploadUrl: String,
        authorizationToken: String,
        partNumber: Int,
        chunk: Chunk,
        sha1Hex: String
    ): UploadPartResponse {

        require(partNumber > 0) { "Part number must be greater than 0." }
        require(chunk.length > 0) { "Part content length must be greater than 0." }

        return client.post(uploadUrl) {
            header(HttpHeaders.Authorization, authorizationToken)
            header(HEADER_PART_NUMBER, partNumber)
            header(HEADER_CONTENT_SHA1, sha1Hex)
            header(HttpHeaders.ContentLength, chunk.length)

            setBody(
                object : OutgoingContent.WriteChannelContent() {
                    override val contentLength: Long = chunk.length.toLong()
                    // Writes only the meaningful chunk.length prefix of the
                    // reused buffer -- no copy of the backing array, so this
                    // never doubles the ~CHUNK_SIZE memory footprint.
                    override suspend fun writeTo(channel: ByteWriteChannel) {
                        channel.writeFully(chunk.bytes, 0, chunk.length)
                    }
                }
            )
        }.body()
    }

    override suspend fun finishLargeFile(request: FinishLargeFileRequest): FinishLargeFileResponse {
        val auth = getAuthorization()

        return client.post("${auth.apiInfo.storageApi.apiUrl}$FINISH_LARGE_FILE") {
            header(HttpHeaders.Authorization, auth.authorizationToken)
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    override suspend fun cancelLargeFile(request: CancelLargeFileRequest): CancelLargeFileResponse {
        val auth = getAuthorization()

        return client.post("${auth.apiInfo.storageApi.apiUrl}$CANCEL_LARGE_FILE") {
            header(HttpHeaders.Authorization, auth.authorizationToken)
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    override suspend fun listParts(request: ListPartsRequest): ListPartsResponse {
        val auth = getAuthorization()

        return client.post("${auth.apiInfo.storageApi.apiUrl}$LIST_PARTS") {
            header(HttpHeaders.Authorization, auth.authorizationToken)
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    override suspend fun generateWatchUrl(fileName: String, expiresInSeconds: Int): String {
        val auth = getAuthorization()

        val tokenResponse = client.post("${auth.apiInfo.storageApi.apiUrl}$GET_DOWNLOAD_AUTHORIZATION") {
            header(HttpHeaders.Authorization, auth.authorizationToken)
            contentType(ContentType.Application.Json)
            setBody(
                GetDownloadAuthorizationRequest(
                    bucketId = config.bucketId,
                    fileNamePrefix = fileName,
                    validDurationInSeconds = expiresInSeconds
                )
            )
        }.body<GetDownloadAuthorizationResponse>()

        return buildString {
            append(auth.apiInfo.storageApi.downloadUrl)
            append("/file/")
            append(config.bucketName)
            append("/")
            append(fileName)
            append("?Authorization=")
            append(tokenResponse.authorizationToken)
        }
    }
}

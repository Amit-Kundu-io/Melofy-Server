/**
 * B2Models.kt
 *
 * Data models for the Backblaze B2 native API v3, plus the exception type
 * B2Client throws for any non-2xx response. Kept separate from B2Client so
 * the wire-format types don't clutter the client logic.
 *
 * https://www.backblaze.com/apidocs/b2-authorize-account
 */

package com.plugins.storage.upload.b2

import kotlinx.serialization.Serializable

/**
 * v3 nests apiUrl/downloadUrl under apiInfo.storageApi (v2 had them as
 * top-level fields). The computed properties preserve the old
 * auth.apiUrl / auth.downloadUrl call sites.
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
data class B2ApiInfo(val storageApi: B2StorageApiInfo)

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

/**
 * Request bodies are typed @Serializable classes, never mapOf(...) with
 * mixed-type values. A map like mapOf("fileId" to "x", "parts" to listOf(1))
 * widens to Map<String, Any>, and kotlinx serialization has no serializer
 * for Any — that fails at runtime, not at compile time. A typed data class
 * always has a generated serializer, so this class of bug can't happen.
 */
@Serializable
internal data class StartLargeFileRequest(val bucketId: String, val fileName: String, val contentType: String)

@Serializable
internal data class GetUploadPartUrlRequest(val fileId: String)

@Serializable
internal data class FinishLargeFileRequest(val fileId: String, val partSha1Array: List<String>)

@Serializable
internal data class ListPartsRequest(val fileId: String, val startPartNumber: Int? = null, val maxPartCount: Int)

/** Thrown for any non-2xx response from B2, carrying enough to log/report cleanly upstream. */
class B2ApiException(
    val httpStatus: Int,
    val b2Code: String?,
    val b2Message: String?,
    rawBody: String
) : Exception("B2 error $httpStatus${b2Code?.let { " [$it]" } ?: ""}: ${b2Message ?: rawBody}")

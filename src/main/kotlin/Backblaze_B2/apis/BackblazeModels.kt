package com.plugins.Backblaze_B2.apis

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * ============================================
 * Authorize Account
 * ============================================
 */

@Serializable
data class AuthorizeAccountResponse(

    @SerialName("accountId")
    val accountId: String,

    @SerialName("authorizationToken")
    val authorizationToken: String,

    @SerialName("apiInfo")
    val apiInfo: ApiInfo
)

@Serializable
data class ApiInfo(

    @SerialName("storageApi")
    val storageApi: StorageApiInfo
)

@Serializable
data class StorageApiInfo(

    @SerialName("apiUrl")
    val apiUrl: String,

    @SerialName("downloadUrl")
    val downloadUrl: String,

    @SerialName("recommendedPartSize")
    val recommendedPartSize: Long,

    @SerialName("absoluteMinimumPartSize")
    val absoluteMinimumPartSize: Long
)

/**
 * ============================================
 * Start Large File
 * ============================================
 */

@Serializable
data class StartLargeFileRequest(

    @SerialName("bucketId")
    val bucketId: String,

    @SerialName("fileName")
    val fileName: String,

    @SerialName("contentType")
    val contentType: String
)

@Serializable
data class StartLargeFileResponse(

    @SerialName("fileId")
    val fileId: String,

    @SerialName("fileName")
    val fileName: String
)

/**
 * ============================================
 * Get Upload Part URL
 * ============================================
 */

@Serializable
data class GetUploadPartUrlRequest(

    @SerialName("fileId")
    val fileId: String
)

@Serializable
data class GetUploadPartUrlResponse(

    @SerialName("fileId")
    val fileId: String,

    @SerialName("uploadUrl")
    val uploadUrl: String,

    @SerialName("authorizationToken")
    val authorizationToken: String
)

/**
 * ============================================
 * Upload Part Response
 * ============================================
 */

@Serializable
data class UploadPartResponse(

    @SerialName("fileId")
    val fileId: String,

    @SerialName("partNumber")
    val partNumber: Int,

    @SerialName("contentLength")
    val contentLength: Long,

    @SerialName("contentSha1")
    val contentSha1: String
)

/**
 * ============================================
 * Finish Large File
 * ============================================
 */

@Serializable
data class FinishLargeFileRequest(

    @SerialName("fileId")
    val fileId: String,

    @SerialName("partSha1Array")
    val partSha1Array: List<String>
)

@Serializable
data class FinishLargeFileResponse(

    @SerialName("fileId")
    val fileId: String,

    @SerialName("fileName")
    val fileName: String
)

/**
 * ============================================
 * Cancel Large File
 * ============================================
 */

@Serializable
data class CancelLargeFileRequest(

    @SerialName("fileId")
    val fileId: String
)

@Serializable
data class CancelLargeFileResponse(

    @SerialName("fileId")
    val fileId: String
)

/**
 * UploadRepository.kt
 *
 * Domain-facing contract for the upload feature. Routes depend on this
 * interface, never on B2Client directly — that's what lets the storage
 * backend (B2 today) be swapped later without touching route code, and
 * lets routes be tested against a fake repository without any network.
 */

package com.plugins.storage.upload.repository

data class StartUploadResult(val fileId: String, val fileName: String)

data class UploadPartUrlResult(val uploadUrl: String, val authorizationToken: String)

/** Thrown by the repository for any failure; routes translate this to an HTTP response. */
class UploadOperationException(message: String, cause: Throwable? = null) : Exception(message, cause)

interface UploadRepository {

    /** Starts a new large-file upload session with the storage backend. Idempotent per (fileName, contentType) is NOT assumed — callers own session/idempotency at their layer. */
    suspend fun startUpload(fileName: String, contentType: String): StartUploadResult

    /**
     * Returns a short-lived, scoped URL + token the CLIENT uses to PUT one
     * part's bytes directly to the storage backend. The server never sees
     * the bytes — this is the "semi-direct" upload architecture: the
     * server issues credentials, the device does the transfer.
     */
    suspend fun getUploadPartUrl(fileId: String): UploadPartUrlResult

    /** Finalizes the large file once all parts are confirmed uploaded, in part-number order. */
    suspend fun finishUpload(fileId: String, partSha1InOrder: List<String>)

    /** partNumber -> sha1 for every part the backend already has. Resume source of truth. */
    suspend fun listCompletedParts(fileId: String): Map<Int, String>
}

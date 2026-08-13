package com.plugins.Backblaze_B2.apis

import com.plugins.Backblaze_B2.storage.Chunk

interface BackblazeApi {

    /**
     * Authenticate with Backblaze. Called once and cached; refreshed on
     * demand if a request comes back unauthorized.
     */
    suspend fun authorize(): AuthorizeAccountResponse

    /**
     * Starts a new large file upload. Returns a fileId.
     */
    suspend fun startLargeFile(
        request: StartLargeFileRequest
    ): StartLargeFileResponse

    /**
     * Returns a temporary, effectively single-use upload URL + auth token
     * for uploading one part. Request a fresh one for every part -- and
     * every retry of a part -- rather than reusing one.
     */
    suspend fun getUploadPartUrl(
        request: GetUploadPartUrlRequest
    ): GetUploadPartUrlResponse

    /**
     * Upload a single part. The chunk is already fully resident in memory
     * (see [Chunk]), so the SHA1 is computed once up front and sent as a
     * normal header -- no streaming-hash trick needed.
     *
     * @param uploadUrl Upload URL returned by [getUploadPartUrl].
     * @param authorizationToken Temporary upload authorization token.
     * @param partNumber Part number (starts from 1).
     * @param chunk The part's bytes (chunk.length may be less than
     * chunk.bytes.size for the final part).
     * @param sha1Hex Precomputed SHA1 hex digest of chunk.bytes[0 until chunk.length].
     */
    suspend fun uploadPart(
        uploadUrl: String,
        authorizationToken: String,
        partNumber: Int,
        chunk: Chunk,
        sha1Hex: String
    ): UploadPartResponse

    /**
     * Completes the large file upload.
     */
    suspend fun finishLargeFile(
        request: FinishLargeFileRequest
    ): FinishLargeFileResponse

    /**
     * Cancels an unfinished large upload.
     */
    suspend fun cancelLargeFile(
        request: CancelLargeFileRequest
    ): CancelLargeFileResponse

    /**
     * Lists parts B2 already has for an in-progress large file. This is
     * the resume source of truth: a client (e.g. UploadWorker) calls this
     * on restart to find out which parts it can skip re-uploading.
     */
    suspend fun listParts(
        request: ListPartsRequest
    ): ListPartsResponse

    suspend fun generateWatchUrl(
        fileName: String,
        expiresInSeconds: Int = 3600
    ): String
}

package com.plugins.Backblaze_B2.storage

import com.plugins.Backblaze_B2.Models.UploadResult
import io.ktor.utils.io.ByteReadChannel

interface BackblazeStorage {

    /**
     * Uploads a large video to Backblaze B2 using the Large File API,
     * streaming it in fixed-size chunks as they arrive on [channel].
     *
     * Workflow:
     * 1. Start large file
     * 2. For each chunk: read it fully into memory, hash it, upload it as
     *    one B2 "part" (retrying that part in place on failure)
     * 3. Finish large file
     *
     * @param fileName Name of the file in Backblaze.
     * @param contentType MIME type (e.g. "video/mp4").
     * @param channel Raw request body, read chunk by chunk as bytes arrive
     * -- no full-body wait.
     * @param contentLength Total file size in bytes (from the request's
     * Content-Length header).
     * @param onPartUploaded Called with the number of bytes just forwarded
     * to Backblaze after each part succeeds -- wire this to progress
     * tracking (e.g. UploadProgressRegistry).
     *
     * @return UploadResult containing the uploaded file information.
     */
    suspend fun uploadVideo(
        fileName: String,
        contentType: String,
        channel: ByteReadChannel,
        contentLength: Long,
        onPartUploaded: (bytes: Long) -> Unit = {}
    ): UploadResult
}

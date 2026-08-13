package com.plugins.Backblaze_B2.storage

import com.plugins.Backblaze_B2.Models.UploadResult
import com.plugins.Backblaze_B2.UploadConstants.CHUNK_SIZE
import com.plugins.Backblaze_B2.UploadConstants.MAX_PART_RETRIES
import com.plugins.Backblaze_B2.UploadConstants.MAX_START_RETRIES
import com.plugins.Backblaze_B2.UploadConstants.RETRY_BASE_DELAY_MS
import com.plugins.Backblaze_B2.apis.BackblazeApi
import com.plugins.Backblaze_B2.apis.CancelLargeFileRequest
import com.plugins.Backblaze_B2.apis.FinishLargeFileRequest
import com.plugins.Backblaze_B2.apis.GetUploadPartUrlRequest
import com.plugins.Backblaze_B2.apis.StartLargeFileRequest
import com.plugins.Backblaze_B2.apis.StartLargeFileResponse
import io.ktor.utils.io.ByteReadChannel
import java.security.MessageDigest
import kotlinx.coroutines.delay

/**
 * Uploads large files to Backblaze B2 with a bounded, near-constant memory
 * footprint -- suitable for servers with as little as ~100-200MB of RAM.
 *
 * Design:
 *  - Bytes are read directly off the live request channel as they arrive
 *    from the client (true streaming -- no waiting for the full body).
 *  - Exactly one CHUNK_SIZE chunk is ever resident in memory at a time.
 *  - Because that one chunk IS fully buffered (unlike a zero-copy design),
 *    a failed upload of that chunk can be retried in place, with a fresh
 *    upload URL, without touching the source channel again.
 *  - Once a chunk has been consumed from the channel there is no going
 *    back -- so chunks are processed strictly sequentially.
 */
class BackblazeStorageImpl(
    private val api: BackblazeApi
) : BackblazeStorage {

    override suspend fun uploadVideo(
        fileName: String,
        contentType: String,
        channel: ByteReadChannel,
        contentLength: Long,
        onPartUploaded: (bytes: Long) -> Unit
    ): UploadResult {

        require(contentLength > 0) {
            "contentLength must be known and greater than 0."
        }

        val startResponse = startLargeFileWithRetry(fileName, contentType)

        try {

            val partSha1Array = mutableListOf<String>()
            val chunkReader = ChunkReader(source = channel, chunkSize = CHUNK_SIZE)
            var partNumber = 1

            while (true) {

                val chunk = chunkReader.nextChunk() ?: break

                val sha1Hex = sha1Of(chunk)

                val contentSha1 = uploadPartWithRetry(
                    fileId = startResponse.fileId,
                    partNumber = partNumber,
                    chunk = chunk,
                    sha1Hex = sha1Hex
                )

                partSha1Array += contentSha1
                onPartUploaded(chunk.length.toLong())
                partNumber++
            }

           val data =  api.finishLargeFile(
                FinishLargeFileRequest(
                    fileId = startResponse.fileId,
                    partSha1Array = partSha1Array
                )
            )

            println("FILE_ID : ${data.fileId}")

            return UploadResult(
                fileId = startResponse.fileId,
                fileName = fileName,
                contentLength = contentLength,
                contentType = contentType
            )

        } catch (e: Exception) {

            runCatching {
                api.cancelLargeFile(CancelLargeFileRequest(fileId = startResponse.fileId))
            }

            throw e
        }
    }

    private fun sha1Of(chunk: Chunk): String {
        val digest = MessageDigest.getInstance("SHA-1").apply {
            update(chunk.bytes, 0, chunk.length)
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }

    /**
     * Retries the SAME in-memory chunk -- requesting a fresh upload URL
     * each attempt -- since the chunk's bytes are still resident and don't
     * need to be re-read from the (non-rewindable) source.
     */
    private suspend fun uploadPartWithRetry(
        fileId: String,
        partNumber: Int,
        chunk: Chunk,
        sha1Hex: String
    ): String {

        var lastError: Exception? = null

        repeat(MAX_PART_RETRIES) { attempt ->
            try {
                val uploadUrl = api.getUploadPartUrl(GetUploadPartUrlRequest(fileId = fileId))

                val response = api.uploadPart(
                    uploadUrl = uploadUrl.uploadUrl,
                    authorizationToken = uploadUrl.authorizationToken,
                    partNumber = partNumber,
                    chunk = chunk,
                    sha1Hex = sha1Hex
                )

                return response.contentSha1
            } catch (e: Exception) {
                lastError = e
                delay(RETRY_BASE_DELAY_MS * (attempt + 1))
            }
        }

        throw lastError ?: IllegalStateException("Part $partNumber failed with no exception captured.")
    }

    /**
     * Only the "start" call is retried here -- it happens before any file
     * bytes are read, so it's always safe to retry regardless of the
     * source channel's state.
     */
    private suspend fun startLargeFileWithRetry(
        fileName: String,
        contentType: String
    ): StartLargeFileResponse {

        var lastError: Exception? = null

        repeat(MAX_START_RETRIES) { attempt ->
            try {
                return api.startLargeFile(
                    StartLargeFileRequest(
                        fileName = fileName,
                        contentType = contentType,
                        bucketId = "" // overwritten with config.bucketId inside BackblazeApiImpl
                    )
                )
            } catch (e: Exception) {
                lastError = e
                delay(RETRY_BASE_DELAY_MS * (attempt + 1))
            }
        }

        throw lastError ?: IllegalStateException("startLargeFile failed with no exception captured.")
    }
}

package com.plugins.Backblaze_B2.storage

import com.plugins.Backblaze_B2.client.UploadConstants.CHUNK_SIZE
import com.plugins.Backblaze_B2.client.UploadConstants.MAX_PART_RETRIES
import com.plugins.Backblaze_B2.client.UploadConstants.MAX_START_RETRIES
import com.plugins.Backblaze_B2.client.UploadConstants.RETRY_BASE_DELAY_MS
import com.plugins.Backblaze_B2.Models.UploadResult
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
 * footprint -- suitable for servers with as little as ~100-200MB of RAM,
 * and verified for files up to 10GB+.
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
 *
 * Scaling math: B2 allows at most 10,000 parts per large file. With the
 * default 30MB CHUNK_SIZE that's a ceiling of ~293GB per upload; a 10GB
 * file needs only ~342 parts, well inside the limit. [MAX_PART_COUNT]
 * below fails fast (before any bytes are read) rather than discovering
 * the limit from a B2 error thousands of parts in.
 */
class BackblazeStorageImpl(
    private val api: BackblazeApi
) : BackblazeStorage {

    private companion object {
        const val MAX_PART_COUNT = 10_000
    }

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

        val expectedParts = (contentLength + CHUNK_SIZE - 1) / CHUNK_SIZE
        require(expectedParts <= MAX_PART_COUNT) {
            "File requires $expectedParts parts at ${CHUNK_SIZE}B/part, " +
                "which exceeds B2's $MAX_PART_COUNT part limit. Increase CHUNK_SIZE."
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

            val finishResponse = api.finishLargeFile(
                FinishLargeFileRequest(
                    fileId = startResponse.fileId,
                    partSha1Array = partSha1Array
                )
            )

            return UploadResult(
                fileId = finishResponse.fileId,
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

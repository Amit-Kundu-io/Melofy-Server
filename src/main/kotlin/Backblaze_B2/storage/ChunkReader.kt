package com.plugins.Backblaze_B2.storage

import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.readAvailable

/**
 * A chunk of bytes read off the source channel. [bytes] is a reused backing
 * array that is larger than the meaningful data on the final chunk -- always
 * read exactly [length] bytes from it, never bytes.size.
 */
data class Chunk(val bytes: ByteArray, val length: Int)

/**
 * Reads [chunkSize]-byte chunks from [source] on demand, without ever
 * holding more than one chunk in memory.
 *
 * IMPORTANT: [bytes] in the returned [Chunk] is the SAME backing array every
 * time -- it is only valid until the next call to [nextChunk]. Callers must
 * fully consume (upload + retry) a chunk before requesting the next one.
 * This is safe as long as chunks are processed strictly sequentially, which
 * is required anyway (parts must be uploaded in order).
 */
class ChunkReader(
    private val source: ByteReadChannel,
    private val chunkSize: Int
) {
    private val buffer = ByteArray(chunkSize)

    /** Returns the next chunk, or null once the source is fully drained. */
    suspend fun nextChunk(): Chunk? {
        var totalRead = 0

        while (totalRead < chunkSize) {
            val n = source.readAvailable(buffer, totalRead, chunkSize - totalRead)
            if (n == -1) break // source exhausted
            totalRead += n
        }

        return if (totalRead == 0) null else Chunk(buffer, totalRead)
    }
}

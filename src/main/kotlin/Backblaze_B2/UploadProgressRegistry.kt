package com.plugins.Backblaze_B2

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

/**
 * Tracks per-upload progress (bytes forwarded to Backblaze so far) keyed by
 * a client-generated upload id, so the Android app can poll
 * GET /videos/upload_status/{id} on a separate connection while the big
 * upload request is still streaming.
 *
 * In-memory only -- fine for a single instance. If you scale to multiple
 * server instances behind a load balancer, back this with Redis (or route
 * status polls to the same instance handling the upload) instead.
 */
object UploadProgressRegistry {

    private val progress = ConcurrentHashMap<String, AtomicLong>()
    private val totals = ConcurrentHashMap<String, Long>()

    fun start(uploadId: String, totalBytes: Long) {
        progress[uploadId] = AtomicLong(0)
        totals[uploadId] = totalBytes
    }

    fun addBytes(uploadId: String, bytes: Long) {
        progress[uploadId]?.addAndGet(bytes)
    }

    fun snapshot(uploadId: String): Pair<Long, Long>? {
        val done = progress[uploadId]?.get() ?: return null
        val total = totals[uploadId] ?: return null
        return done to total
    }

    fun finish(uploadId: String) {
        progress.remove(uploadId)
        totals.remove(uploadId)
    }
}

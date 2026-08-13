package com.plugins.Backblaze_B2

object UploadConstants {

    // Size of each chunk read from the incoming request body and forwarded
    // to Backblaze as one large-file "part". Chosen to comfortably clear
    // B2's minimum part size while keeping peak memory well under a
    // 100-200MB server budget: only ONE chunk is ever resident at a time.
    const val CHUNK_SIZE: Int = 30 * 1024 * 1024

    const val MAX_START_RETRIES = 3
    const val MAX_PART_RETRIES = 3
    const val RETRY_BASE_DELAY_MS = 500L
}

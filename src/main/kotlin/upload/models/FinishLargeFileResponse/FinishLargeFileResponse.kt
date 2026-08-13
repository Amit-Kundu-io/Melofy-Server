package com.plugins.upload.models.FinishLargeFileResponse

import kotlinx.serialization.Serializable

@Serializable
data class FinishLargeFileResponse(
    val fileId: String,
    val fileName: String,
    val bucketId: String,
    val contentType: String,
    val contentLength: Long,
    val fileInfo: Map<String, String> = emptyMap()
)
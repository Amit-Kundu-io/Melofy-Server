package com.plugins.Backblaze_B2.Models

import kotlinx.serialization.Serializable

@Serializable
data class UploadResult(
    val fileId: String,
    val fileName: String,
    val contentLength: Long,
    val contentType: String
)

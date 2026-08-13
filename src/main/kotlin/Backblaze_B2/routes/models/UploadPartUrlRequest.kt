package com.plugins.Backblaze_B2.routes

import kotlinx.serialization.Serializable

@Serializable
data class UploadPartUrlRequest(val fileId: String)

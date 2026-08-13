package com.plugins.Backblaze_B2.routes

import kotlinx.serialization.Serializable

@Serializable
data class FinishUploadRequest(val fileId: String, val partSha1sInOrder: List<String>)

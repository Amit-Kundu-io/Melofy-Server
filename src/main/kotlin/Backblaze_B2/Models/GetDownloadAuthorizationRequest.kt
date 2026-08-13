package com.plugins.Backblaze_B2.Models

import kotlinx.serialization.Serializable

@Serializable
data class GetDownloadAuthorizationRequest(
    val bucketId: String,
    val fileNamePrefix: String,
    val validDurationInSeconds: Int
)

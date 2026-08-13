package com.plugins.Backblaze_B2.Models

import kotlinx.serialization.Serializable

@Serializable
data class GetDownloadAuthorizationRequest(
    val bucketId: String,
    val fileNamePrefix: String,
    val validDurationInSeconds: Int
)

@Serializable
data class GetDownloadAuthorizationResponse(
    val authorizationToken: String
)
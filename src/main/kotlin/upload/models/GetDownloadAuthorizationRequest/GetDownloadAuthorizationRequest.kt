package com.plugins.upload.models.GetDownloadAuthorizationRequest

import kotlinx.serialization.Serializable

@Serializable
data class GetDownloadAuthorizationRequest(
    val bucketId: String,
    val fileNamePrefix: String,
    val validDurationInSeconds: Int
)

@Serializable
data class B2DownloadAuthorizationResponse(
    val authorizationToken: String
)



@Serializable
data class VideoPlayUrlResponse(
    val url: String,
    val expiresInSeconds: Int
)
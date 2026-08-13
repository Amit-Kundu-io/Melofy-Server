package com.plugins.Backblaze_B2.Models

import kotlinx.serialization.Serializable

@Serializable
data class GetDownloadAuthorizationResponse(
    val authorizationToken: String
)

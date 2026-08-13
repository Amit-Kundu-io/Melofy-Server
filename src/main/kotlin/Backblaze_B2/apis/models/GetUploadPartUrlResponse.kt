package com.plugins.Backblaze_B2.apis

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Effectively single-use upload URL + auth token for one part. Request a
 * fresh one for every part -- and every retry of a part.
 */
@Serializable
data class GetUploadPartUrlResponse(

    @SerialName("fileId")
    val fileId: String,

    @SerialName("uploadUrl")
    val uploadUrl: String,

    @SerialName("authorizationToken")
    val authorizationToken: String
)

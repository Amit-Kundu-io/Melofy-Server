package com.plugins.Backblaze_B2.apis

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GetUploadPartUrlRequest(

    @SerialName("fileId")
    val fileId: String
)

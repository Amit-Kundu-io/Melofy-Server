package com.plugins.Backblaze_B2.apis

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FinishLargeFileRequest(

    @SerialName("fileId")
    val fileId: String,

    @SerialName("partSha1Array")
    val partSha1Array: List<String>
)

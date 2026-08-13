package com.plugins.Backblaze_B2.apis

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class B2PartInfo(

    @SerialName("fileId")
    val fileId: String,

    @SerialName("partNumber")
    val partNumber: Int,

    @SerialName("contentLength")
    val contentLength: Long,

    @SerialName("contentSha1")
    val contentSha1: String
)

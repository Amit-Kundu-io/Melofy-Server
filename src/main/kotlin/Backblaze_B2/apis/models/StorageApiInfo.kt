package com.plugins.Backblaze_B2.apis

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StorageApiInfo(

    @SerialName("apiUrl")
    val apiUrl: String,

    @SerialName("downloadUrl")
    val downloadUrl: String,

    @SerialName("recommendedPartSize")
    val recommendedPartSize: Long,

    @SerialName("absoluteMinimumPartSize")
    val absoluteMinimumPartSize: Long
)

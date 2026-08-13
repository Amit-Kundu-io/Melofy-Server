package com.plugins.Backblaze_B2.apis

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Request body for b2_start_large_file. */
@Serializable
data class StartLargeFileRequest(

    @SerialName("bucketId")
    val bucketId: String,

    @SerialName("fileName")
    val fileName: String,

    @SerialName("contentType")
    val contentType: String
)

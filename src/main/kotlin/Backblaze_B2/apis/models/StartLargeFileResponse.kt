package com.plugins.Backblaze_B2.apis

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StartLargeFileResponse(

    @SerialName("fileId")
    val fileId: String,

    @SerialName("fileName")
    val fileName: String
)

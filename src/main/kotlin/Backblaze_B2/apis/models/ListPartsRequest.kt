package com.plugins.Backblaze_B2.apis

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ListPartsRequest(

    @SerialName("fileId")
    val fileId: String,

    @SerialName("startPartNumber")
    val startPartNumber: Int? = null,

    @SerialName("maxPartCount")
    val maxPartCount: Int = 1000
)

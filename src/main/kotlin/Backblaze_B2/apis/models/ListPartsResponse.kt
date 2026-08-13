package com.plugins.Backblaze_B2.apis

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ListPartsResponse(

    @SerialName("parts")
    val parts: List<B2PartInfo>,

    @SerialName("nextPartNumber")
    val nextPartNumber: Int? = null
)

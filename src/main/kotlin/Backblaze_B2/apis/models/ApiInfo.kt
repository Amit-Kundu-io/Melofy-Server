package com.plugins.Backblaze_B2.apis

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ApiInfo(

    @SerialName("storageApi")
    val storageApi: StorageApiInfo
)

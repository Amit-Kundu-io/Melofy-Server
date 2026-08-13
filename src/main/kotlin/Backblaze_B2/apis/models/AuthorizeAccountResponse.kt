package com.plugins.Backblaze_B2.apis

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Response of b2_authorize_account (v4). */
@Serializable
data class AuthorizeAccountResponse(

    @SerialName("accountId")
    val accountId: String,

    @SerialName("authorizationToken")
    val authorizationToken: String,

    @SerialName("apiInfo")
    val apiInfo: ApiInfo
)

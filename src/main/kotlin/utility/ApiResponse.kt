package com.amit_kundu_io.utility

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
/** Uniform API envelope used by successful and failed responses. */
data class ApiResponse<T>(
    @SerialName("data")
    val data: T? = null,
    @SerialName("message")
    val message: List<String> = emptyList(),
    @SerialName("succedeed")
    val succeeded: Boolean,
    @SerialName("totalItems")
    val totalItems: Int? = null,
    @SerialName("type")
    val type: String,
)

/** Factory methods keep route handlers consistent and avoid repeated response boilerplate. */
object ApiResponses {
    /** Builds a successful standard response with optional payload and total count. */
    fun <T> success(
        data: T? = null,
        message: List<String> = listOf("Success"),
        totalItems: Int? = null,
        type: String = "SUCCESS",
    ) = ApiResponse(data, message, true, totalItems, type)

    /** Builds a failed standard response without exposing internal exception details. */
    fun error(message: String, type: String = "ERROR") =
        ApiResponse<String>(null, listOf(message), false, null, type)

    /** Builds a successful response for actions that do not return a payload. */
    fun acknowledgement(message: String, type: String) =
        ApiResponse<String>(null, listOf(message), true, null, type)
}

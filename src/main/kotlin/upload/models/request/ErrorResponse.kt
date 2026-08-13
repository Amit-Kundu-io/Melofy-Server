package upload.models.request

import kotlinx.serialization.Serializable


@Serializable data class ErrorResponse(val error: String)
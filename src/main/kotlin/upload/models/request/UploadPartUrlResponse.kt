package upload.models.request

import kotlinx.serialization.Serializable


@Serializable
data class UploadPartUrlResponse(val uploadUrl: String, val authorizationToken: String)

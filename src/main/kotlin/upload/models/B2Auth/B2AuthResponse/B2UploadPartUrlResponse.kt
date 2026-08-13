package upload.models.B2Auth.B2AuthResponse

import kotlinx.serialization.Serializable

@Serializable
data class B2UploadPartUrlResponse(val uploadUrl: String, val authorizationToken: String)

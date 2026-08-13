package upload.models.B2Auth.B2AuthResponse

import kotlinx.serialization.Serializable

@Serializable
data class B2StartLargeFileResponse(val fileId: String, val fileName: String)

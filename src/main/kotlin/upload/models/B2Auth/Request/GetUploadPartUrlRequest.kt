package upload.models.B2Auth.Request

import kotlinx.serialization.Serializable

@Serializable
internal data class GetUploadPartUrlRequest(val fileId: String)
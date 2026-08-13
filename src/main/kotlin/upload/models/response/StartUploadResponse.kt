package upload.models.response

import kotlinx.serialization.Serializable


@Serializable
data class StartUploadResponse(val fileId: String)
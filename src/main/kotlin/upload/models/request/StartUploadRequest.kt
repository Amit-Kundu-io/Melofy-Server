package upload.models.request

import kotlinx.serialization.Serializable


@Serializable
data class StartUploadRequest(val fileName: String, val contentType: String)
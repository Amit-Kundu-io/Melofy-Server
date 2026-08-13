package upload.models.B2Auth.Request

import kotlinx.serialization.Serializable

@Serializable
internal data class StartLargeFileRequest(val bucketId: String, val fileName: String, val contentType: String)
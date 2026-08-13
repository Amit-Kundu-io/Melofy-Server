package upload.models.B2Auth.Request

import kotlinx.serialization.Serializable


@Serializable
internal data class FinishLargeFileRequest(val fileId: String, val partSha1Array: List<String>)
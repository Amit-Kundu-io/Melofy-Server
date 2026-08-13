package upload.models.request

import kotlinx.serialization.Serializable

@Serializable data class FinishUploadRequest(val fileId: String, val partSha1Array: List<String>)

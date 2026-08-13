package upload.models.request

import kotlinx.serialization.Serializable

@Serializable data class PartsDetailRequest(val fileId: String)

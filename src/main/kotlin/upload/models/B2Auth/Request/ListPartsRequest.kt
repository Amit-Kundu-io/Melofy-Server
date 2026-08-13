package upload.models.B2Auth.Request

import kotlinx.serialization.Serializable


@Serializable
internal data class ListPartsRequest(val fileId: String, val startPartNumber: Int? = null, val maxPartCount: Int)
package upload.models

import kotlinx.serialization.Serializable

@Serializable
data class B2StorageApiInfo(
    val apiUrl: String,
    val downloadUrl: String,
    val bucketId: String? = null,
    val bucketName: String? = null,
    val recommendedPartSize: Long? = null,
    val absoluteMinimumPartSize: Long? = null,
    val s3ApiUrl: String? = null
)







@Serializable
data class B2ApiInfo(val storageApi: B2StorageApiInfo)
package upload.models.B2Auth.B2AuthResponse

import kotlinx.serialization.Serializable
import upload.models.B2ApiInfo

@Serializable
data class B2AuthResponse(
    val authorizationToken: String,
    val apiInfo: B2ApiInfo,
    val accountId: String? = null
) {
    val apiUrl: String get() = apiInfo.storageApi.apiUrl
    val downloadUrl: String get() = apiInfo.storageApi.downloadUrl
}
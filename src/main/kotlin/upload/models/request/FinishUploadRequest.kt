package upload.models.request

import com.amit_kundu_io.song_upload.data.models.req.CreateSongRequest
import kotlinx.serialization.Serializable

@Serializable
data class FinishUploadRequest(
    val fileId: String,
    val partSha1Array: List<String>,
    val song: CreateSongRequest ?= null,
)

package com.amit_kundu_io.song_upload.service.service

import com.amit_kundu_io.song_upload.data.models.req.CreateSongRequest
import com.amit_kundu_io.song_upload.data.models.res.SongResponse

/** Song use cases exposed to HTTP routes. */
interface SongService {
    /** Creates a validated song from an API request. */
    suspend fun upload(request: CreateSongRequest): SongResponse

    /** Deletes a song specified by its string UUID. */
    suspend fun delete(id: String): Boolean

    /*** Get Song with Play list wise*/
    suspend fun songsByPlayListId(playListId: String): List<SongResponse>

    /*** search suggestion */
    suspend fun suggestName(text: String): List<String>


    /*** search songs */
    suspend fun searchSongs(text: String): List<SongResponse>

}
package com.amit_kundu_io.song_upload.data.repo


import com.amit_kundu_io.song_upload.data.models.NewSong
import com.amit_kundu_io.song_upload.data.models.Song
import com.amit_kundu_io.song_upload.data.models.res.SongResponse
import kotlin.uuid.Uuid

/** Database contract for song writes and deletes. */
interface SongRepository {
    /** Persists a song and optionally creates its initial playlist membership. */
    suspend fun create(song: NewSong, playlistId: Uuid?): Song

    /** Removes a song by UUID and reports whether a record existed. */
    suspend fun delete(id: Uuid): Boolean


    /*** Get Song with Play list wise*/
    suspend fun songsByPlayListId(playListId: String): List<SongResponse>

    /*** search suggestion */
    suspend fun suggestName(text: String): List<String>

    /*** search songs */
    suspend fun searchSongs(text: String): List<SongResponse>

}
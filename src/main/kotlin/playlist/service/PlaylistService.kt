package com.amit_kundu_io.playlist.service

import com.amit_kundu_io.playlist.data.module.req.CreatePlaylistRequest
import com.amit_kundu_io.playlist.data.module.res.PlaylistPageResponse
import com.amit_kundu_io.playlist.data.module.res.PlaylistResponse
import com.amit_kundu_io.playlist.data.module.res.PlaylistSongsResponse

/** Playlist use cases available to route handlers. */
interface PlaylistService {
    /** Creates a validated playlist. */
    suspend fun create(request: CreatePlaylistRequest): PlaylistResponse
    /** Gets a playlist from its route UUID. */
    suspend fun get(id: String): PlaylistResponse
    /** Lists playlists from an optional cursor and page size. */
    suspend fun list(cursor: String?, limit: String?): PlaylistPageResponse
    /** Adds an existing song to an existing playlist. */
    suspend fun addSong(playlistId: String, songId: String)
    /** Lists a page of songs belonging to a playlist. */
    suspend fun songs(id: String, cursor: String?, limit: String?): PlaylistSongsResponse
}
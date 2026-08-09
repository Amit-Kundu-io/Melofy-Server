package com.amit_kundu_io.playlist

import com.amit_kundu_io.song_upload.data.models.res.SongResponse
import kotlinx.serialization.Serializable

@Serializable
/** Playlist metadata, including the maintained count of attached songs. */
data class PlaylistResponse(
    val id: String,
    val name: String,
    val description: String?,
    val artworkUrl: String?,
    val songCount: Long,
    val createdAt: String,
    val updatedAt: String,
)

@Serializable
/** Cursor-based page of songs belonging to one playlist. */
data class PlaylistSongsResponse(
    val items: List<SongResponse>,
    val nextCursor: Long?,
)

@Serializable
/** Cursor-based page of playlist metadata. */
data class PlaylistPageResponse(
    val items: List<PlaylistResponse>,
    val nextCursor: String?,
    val totalCount: Long,
)

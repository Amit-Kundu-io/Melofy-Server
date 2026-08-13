package com.amit_kundu_io.song_upload.data.models.res

import kotlinx.serialization.Serializable

@Serializable
/** Public representation of a stored song returned by the API. */
data class SongResponse(
    val id: String,
    val title: String,
    val artistName: String,
    val audioUrl: String,
    val albumName: String?,
    val description: String?,
    val genre: String?,
    val language: String?,
    val durationSeconds: Int?,
    val artworkUrl: String?,
    val fileName: String?,
    val videoId: String?,
    val releaseDate: String?,
    val isExplicit: Boolean,
    val createdAt: String,
    val updatedAt: String,
)

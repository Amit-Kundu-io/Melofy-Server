package com.amit_kundu_io.song_upload

import kotlinx.serialization.Serializable

@Serializable
/** Client payload for creating a song; playlistId optionally attaches it atomically to a playlist. */
data class CreateSongRequest(

    val title: String,
    val artistName: String,
    val audioUrl: String,
    val albumName: String? = null,
    val description: String? = null,
    val genre: String? = null,
    val language: String? = null,
    val durationSeconds: Int? = null,
    val artworkUrl: String? = null,
    val releaseDate: String? = null,
    val isExplicit: Boolean = false,
    val playlistId: String? = null,
)

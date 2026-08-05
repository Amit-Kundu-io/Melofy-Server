package com.amit_kundu_io.playlist

import kotlinx.serialization.Serializable

@Serializable
/** Client payload for creating a playlist. */
data class CreatePlaylistRequest(
    val name: String,
    val description: String? = null,
    val artworkUrl: String? = null,
)

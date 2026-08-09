package com.amit_kundu_io.playlist.data.module

import java.time.Instant
import kotlin.uuid.Uuid

/** Internal playlist entity used between repository and service layers. */
data class Playlist(
    val id: Uuid,
    val name: String,
    val description: String?,
    val artworkUrl: String?,
    val songCount: Long,
    val createdAt: Instant,
    val updatedAt: Instant,
)

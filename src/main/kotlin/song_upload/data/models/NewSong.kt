package com.amit_kundu_io.song_upload.data.models

import java.time.LocalDate

/** Validated song values ready for persistence. */
data class NewSong(
    val title: String,
    val artistName: String,
    val audioUrl: String,
    val albumName: String?,
    val description: String?,
    val genre: String?,
    val language: String?,
    val durationSeconds: Int?,
    val artworkUrl: String?,
    val releaseDate: LocalDate?,
    val isExplicit: Boolean,
    val fileName: String?,
    val videoId: String?,
)
package com.amit_kundu_io.song_upload.data.models

import com.amit_kundu_io.song_upload.data.models.res.SongResponse
import java.time.Instant
import java.time.LocalDate
import kotlin.uuid.Uuid

/** Internal song entity mapped from the database and converted to a response at the boundary. */
data class Song(
    val id: Uuid,
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
    val releaseDate: LocalDate?,
    val isExplicit: Boolean,
    val createdAt: Instant,
    val updatedAt: Instant,
) {
    /** Converts the internal entity to the serializable API representation. */
    fun toResponse() = SongResponse(
        id = id.toString(),
        title = title,
        artistName = artistName,
        audioUrl = audioUrl,
        albumName = albumName,
        description = description,
        genre = genre,
        language = language,
        durationSeconds = durationSeconds,
        artworkUrl = artworkUrl,
        fileName = fileName,
        videoId = videoId,
        releaseDate = releaseDate?.toString(),
        isExplicit = isExplicit,
        createdAt = createdAt.toString(),
        updatedAt = updatedAt.toString(),
    )
}
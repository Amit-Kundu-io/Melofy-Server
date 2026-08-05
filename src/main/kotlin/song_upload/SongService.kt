package com.amit_kundu_io.song_upload

import java.net.URI
import java.time.LocalDate
import java.time.format.DateTimeParseException
import kotlin.uuid.Uuid

/** Signals client input that cannot be accepted as a song. */
class SongValidationException(message: String) : IllegalArgumentException(message)

/** Song use cases exposed to HTTP routes. */
interface SongService {
    /** Creates a validated song from an API request. */
    suspend fun upload(request: CreateSongRequest): SongResponse
    /** Deletes a song specified by its string UUID. */
    suspend fun delete(id: String): Boolean
}

/** Validates and normalizes song input before repository calls. */
class SongServiceImpl(private val repository: SongRepository) : SongService {
    /** Validates a creation request and returns the newly stored song. */
    override suspend fun upload(request: CreateSongRequest): SongResponse {
        val newSong = NewSong(
            title = request.title.required("title", 255),
            artistName = request.artistName.required("artistName", 255),
            audioUrl = request.audioUrl.validUrl("audioUrl"),
            albumName = request.albumName.clean("albumName", 255),
            description = request.description.clean("description", 10_000),
            genre = request.genre.clean("genre", 100),
            language = request.language.clean("language", 50),
            durationSeconds = request.durationSeconds?.also {
                if (it <= 0) throw SongValidationException("durationSeconds must be positive")
            },
            artworkUrl = request.artworkUrl?.validUrl("artworkUrl"),
            releaseDate = request.releaseDate?.parseDate(),
            isExplicit = request.isExplicit,
        )
        val playlistId = request.playlistId?.let { value ->
            try { Uuid.parse(value) } catch (_: IllegalArgumentException) {
                throw SongValidationException("Give Correct playlistId ")
            }
        }
        return repository.create(newSong, playlistId).toResponse()
    }

    /** Validates a song ID before removing the matching record. */
    override suspend fun delete(id: String): Boolean {
        val songId = try { Uuid.parse(id) } catch (_: IllegalArgumentException) {
            throw SongValidationException("id must be a UUID")
        }
        return repository.delete(songId)
    }

    /** Trims and validates a mandatory short text field. */
    private fun String.required(field: String, max: Int): String =
        trim().takeIf { it.isNotEmpty() && it.length <= max }
            ?: throw SongValidationException("$field is required and must be at most $max characters")

    /** Trims an optional text field and converts an empty value to null. */
    private fun String?.clean(field: String, max: Int): String? = this?.trim()?.let {
        if (it.length > max) throw SongValidationException("$field must be at most $max characters")
        it.ifEmpty { null }
    }

    /** Accepts only absolute HTTP or HTTPS URLs. */
    private fun String.validUrl(field: String): String {
        val value = trim()
        val uri = try { URI(value) } catch (_: IllegalArgumentException) { null }
        val isHttpUrl = uri != null && uri.scheme in setOf("https", "http") && !uri.host.isNullOrBlank()
        if (!isHttpUrl) {
            throw SongValidationException("$field must be an absolute HTTP(S) URL")
        }
        return value
    }

    /** Parses a client date in ISO-8601 YYYY-MM-DD format. */
    private fun String.parseDate(): LocalDate = try {
        LocalDate.parse(this)
    } catch (_: DateTimeParseException) {
        throw SongValidationException("releaseDate must use ISO-8601 format (YYYY-MM-DD)")
    }
}

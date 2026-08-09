package com.amit_kundu_io.playlist

import java.net.URI
import kotlin.uuid.Uuid

/** Signals invalid playlist input supplied by a client. */
class PlaylistValidationException(message: String) : IllegalArgumentException(message)

/** Signals a syntactically valid playlist identifier that is absent from storage. */
class PlaylistNotFoundException : NoSuchElementException("Playlist not found")

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

/** Validates playlist input and prepares cursor-paginated API results. */
class PlaylistServiceImpl(private val repository: PlaylistRepository) : PlaylistService {
    /** Validates playlist input and creates the playlist. */
    override suspend fun create(request: CreatePlaylistRequest): PlaylistResponse {
        val playlist = repository.create(
            name = request.name.required("name", 255),
            description = request.description.clean("description", 10_000),
            artworkUrl = request.artworkUrl?.validUrl("artworkUrl"),
        )
        return playlist.toResponse()
    }

    /** Returns one playlist or signals that its UUID is absent. */
    override suspend fun get(id: String): PlaylistResponse {
        val playlist = repository.find(id.toUuid("id")) ?: throw PlaylistNotFoundException()
        return playlist.toResponse()
    }

    /** Produces a bounded cursor page of playlists and its total count. */
    override suspend fun list(cursor: String?, limit: String?): PlaylistPageResponse {
        val pageSize = limit?.toIntOrNull()?.also {
            if (it !in 1..100) throw PlaylistValidationException("limit must be between 1 and 100")
        } ?: 50
        val cursorId = cursor?.toUuid("cursor")
        val playlists = repository.list(cursorId, pageSize)
        return PlaylistPageResponse(
            items = playlists.map { it.toResponse() },
            nextCursor = playlists.lastOrNull()?.id?.toString()?.takeIf { playlists.size == pageSize },
            totalCount = repository.totalCount(),
        )
    }

    /** Attaches an existing song to an existing playlist. */
    override suspend fun addSong(playlistId: String, songId: String) {
        val playlistUuid = playlistId.toUuid("playlistId")
        val songUuid = songId.toUuid("songId")
        if (!repository.addSong(playlistUuid, songUuid)) {
            throw PlaylistNotFoundException()
        }
    }

    /** Produces a bounded cursor page of songs in one playlist. */
    override suspend fun songs(id: String, cursor: String?, limit: String?): PlaylistSongsResponse {
        val playlistId = id.toUuid("id")
        if (repository.find(playlistId) == null) throw PlaylistNotFoundException()
        val pageSize = limit?.toIntOrNull()?.also {
            if (it !in 1..100) throw PlaylistValidationException("limit must be between 1 and 100")
        } ?: 50
        val cursorValue = cursor?.toLongOrNull()?.also {
            if (it <= 0) throw PlaylistValidationException("cursor must be a positive number")
        }
        val results = repository.songs(playlistId, cursorValue, pageSize)
        return PlaylistSongsResponse(
            items = results.map { it.second.toResponse() },
            nextCursor = results.lastOrNull()?.first?.takeIf { results.size == pageSize },
        )
    }

    /** Converts a playlist entity into the public response model. */
    private fun Playlist.toResponse() = PlaylistResponse(
        id = id.toString(), name = name, description = description, artworkUrl = artworkUrl,
        songCount = songCount, createdAt = createdAt.toString(), updatedAt = updatedAt.toString(),
    )

    /** Parses one required UUID route or query value. */
    private fun String.toUuid(field: String): Uuid = try {
        Uuid.parse(this)
    } catch (_: IllegalArgumentException) {
        throw PlaylistValidationException("$field must be a UUID")
    }

    /** Trims and validates a mandatory playlist text field. */
    private fun String.required(field: String, max: Int): String = trim().takeIf { it.isNotEmpty() && it.length <= max }
        ?: throw PlaylistValidationException("$field is required and must be at most $max characters")

    /** Trims an optional playlist text field and converts empty text to null. */
    private fun String?.clean(field: String, max: Int): String? = this?.trim()?.let {
        if (it.length > max) throw PlaylistValidationException("$field must be at most $max characters")
        it.ifEmpty { null }
    }

    /** Validates artwork URLs before they are stored. */
    private fun String.validUrl(field: String): String {
        val value = trim()
        val uri = try { URI(value) } catch (_: IllegalArgumentException) { null }
        if (uri == null || uri.scheme !in setOf("http", "https") || uri.host.isNullOrBlank()) {
            throw PlaylistValidationException("$field must be an absolute HTTP(S) URL")
        }
        return value
    }
}

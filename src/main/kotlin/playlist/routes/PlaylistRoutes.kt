package com.amit_kundu_io.playlist.routes

import com.amit_kundu_io.playlist.data.module.req.CreatePlaylistRequest
import com.amit_kundu_io.playlist.service.PlaylistService
import com.amit_kundu_io.utility.ApiResponses
import com.amit_kundu_io.utility.halper.doc
import com.amit_kundu_io.utility.validation_exception.PlaylistNotFoundException
import com.amit_kundu_io.utility.validation_exception.PlaylistValidationException
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receiveText
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import org.koin.ktor.ext.inject

/** Registers playlist creation, retrieval, pagination, and membership endpoints. */
fun Route.playlistRoutes() {
    val playlistService by inject<PlaylistService>()
    val json = Json { ignoreUnknownKeys = true }

    route("/playlists") {
        /** Lists a bounded page of playlists using an optional primary-key cursor. */
        get {
            try {
                val page = playlistService.list(
                    cursor = call.request.queryParameters["cursor"],
                    limit = call.request.queryParameters["limit"],
                )
                call.respond(ApiResponses.success(page, totalItems = page.totalCount.toInt(), type = "PLAYLISTS"))
            } catch (exception: PlaylistValidationException) {
                call.respond(HttpStatusCode.BadRequest, ApiResponses.error(exception.message ?: "Invalid pagination", "VALIDATION_ERROR"))
            }
        }.doc()

        /** Creates playlist metadata before songs are attached to it. */
        post {
            try {
                val request = json.decodeFromString<CreatePlaylistRequest>(call.receiveText())
                call.respond(HttpStatusCode.Created, ApiResponses.success(playlistService.create(request), type = "PLAYLIST_CREATED"))
            } catch (exception: PlaylistValidationException) {
                call.respond(HttpStatusCode.BadRequest, ApiResponses.error(exception.message ?: "Invalid playlist data", "VALIDATION_ERROR"))
            } catch (_: SerializationException) {
                call.respond(HttpStatusCode.BadRequest, ApiResponses.error("Invalid JSON body. Send a JSON object with name.", "VALIDATION_ERROR"))
            }
        }.doc()

        /** Returns one playlist and its cached number of songs. */
        get("/{id}") {
            try {
                call.respond(ApiResponses.success(playlistService.get(call.parameters["id"].orEmpty()), type = "PLAYLIST"))
            } catch (_: PlaylistNotFoundException) {
                call.respond(HttpStatusCode.NotFound, ApiResponses.error("playlistId does not exist", "NOT_FOUND"))
            } catch (exception: PlaylistValidationException) {
                call.respond(HttpStatusCode.BadRequest, ApiResponses.error(exception.message ?: "Invalid playlist id", "VALIDATION_ERROR"))
            }
        }.doc()

        /** Adds one existing song to an existing playlist. */
        post("/{id}/songs/{songId}") {
            try {
                playlistService.addSong(call.parameters["id"].orEmpty(), call.parameters["songId"].orEmpty())
                call.respond(HttpStatusCode.OK, ApiResponses.acknowledgement("Song added to playlist", "PLAYLIST_SONG_ADDED"))
            } catch (_: PlaylistNotFoundException) {
                call.respond(HttpStatusCode.NotFound, ApiResponses.error("Playlist or song not found", "NOT_FOUND"))
            } catch (exception: PlaylistValidationException) {
                call.respond(HttpStatusCode.BadRequest, ApiResponses.error(exception.message ?: "Invalid request", "VALIDATION_ERROR"))
            }
        }.doc()

        /** Lists a bounded cursor page of songs that belong to this playlist. */
        get("/{id}/songs") {
            try {
                call.respond(ApiResponses.success(playlistService.songs(
                    id = call.parameters["id"].orEmpty(),
                    cursor = call.request.queryParameters["cursor"],
                    limit = call.request.queryParameters["limit"],
                ), type = "PLAYLIST_SONGS"))
            } catch (_: PlaylistNotFoundException) {
                call.respond(HttpStatusCode.NotFound, ApiResponses.error("Playlist not found", "NOT_FOUND"))
            } catch (exception: PlaylistValidationException) {
                call.respond(HttpStatusCode.BadRequest, ApiResponses.error(exception.message ?: "Invalid pagination", "VALIDATION_ERROR"))
            }
        }.doc()
    }
}

package com.amit_kundu_io.song_upload

import com.amit_kundu_io.playlist.PlaylistNotFoundException
import com.amit_kundu_io.utility.ApiResponses
import com.amit_kundu_io.utility.halper.doc
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receiveText
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import org.koin.ktor.ext.inject
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

/** Registers public song creation and deletion endpoints. */
fun Route.songRoutes() {
    val songService by inject<SongService>()
    val json = Json { ignoreUnknownKeys = true }

    route("/songs") {
        /** Creates song metadata and optionally attaches the new song to a playlist. */
        post {
            try {
                val request = json.decodeFromString<CreateSongRequest>(call.receiveText())
                call.respond(HttpStatusCode.Created, ApiResponses.success(songService.upload(request), type = "SONG_CREATED"))
            } catch (exception: SongValidationException) {
                call.respond(HttpStatusCode.BadRequest, ApiResponses.error(exception.message ?: "Invalid song data", "VALIDATION_ERROR"))
            } catch (_: PlaylistNotFoundException) {
                call.respond(HttpStatusCode.NotFound, ApiResponses.error("playlistId does not exist", "NOT_FOUND"))
            } catch (_: SerializationException) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ApiResponses.error("Invalid JSON body. Send a JSON object with title, artistName, and audioUrl.", "VALIDATION_ERROR"),
                )
            }
        }.doc()

        /** Deletes a song and removes its playlist memberships through the database relation. */
        delete("/{id}") {
            try {
                if (!songService.delete(call.parameters["id"].orEmpty())) {
                    return@delete call.respond(HttpStatusCode.NotFound, ApiResponses.error("Song not found", "NOT_FOUND"))
                }
                call.respond(HttpStatusCode.OK, ApiResponses.acknowledgement("Song deleted", "SONG_DELETED"))
            } catch (exception: SongValidationException) {
                call.respond(HttpStatusCode.BadRequest, ApiResponses.error(exception.message ?: "Invalid song id", "VALIDATION_ERROR"))
            }
        }.doc()
    }
}

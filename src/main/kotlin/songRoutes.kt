package com.amit_kundu_io

import com.amit_kundu_io.utility.halper.doc
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.request
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.routing.openapi.describe
import io.ktor.utils.io.ExperimentalKtorApi
import kotlinx.serialization.Serializable
import kotlin.reflect.full.memberProperties

@Serializable
data class CreateSongRequest(
    val title: String,
    val artist: String,
    val duration: Int
)

@OptIn(ExperimentalKtorApi::class)
fun Route.songRoutes() {

    // GET /songs
    get("/songs") {
        call.respond("OK")
    }.doc ()
    // GET /songs?page=1&size=10&search=love
    get("/songs/search") {
        val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
        val size = call.request.queryParameters["size"]?.toIntOrNull() ?: 10
        val search = call.request.queryParameters["search"]

        call.respond(
            mapOf(
                "page" to page,
                "size" to size,
                "search" to search
            )
        )
    }.doc()

    // GET /songs/123
    get("/songs/{id}") {
        val id = call.parameters["id"]

        call.respond(
            mapOf(
                "songId" to id
            )
        )
    }.doc()

    // POST /songs
    post("/songs") {
        val request = call.receive<CreateSongRequest>()

        call.respond(
            HttpStatusCode.Created,
            request
        )
    }.doc()

    // PUT /songs/123
    put("/songs/{id}") {
        val id = call.parameters["id"]
        val request = call.receive<CreateSongRequest>()

        call.respond(
            mapOf(
                "id" to id,
                "song" to request
            )
        )
    }.doc()



    // DELETE /songs/123
    delete("/songs/{id}") {
        val id = call.parameters["id"]

        call.respond("Deleted Song $id")
    }.doc()

    // Header Example
    get("/profile") {
        val token = call.request.headers["Authorization"]

        call.respond(
            mapOf(
                "token" to token
            )
        )
    }.doc()
}

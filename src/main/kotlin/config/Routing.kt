package com.amit_kundu_io.config

import com.amit_kundu_io.utility.halper.doc
import com.amit_kundu_io.song_upload.route.songRoutes
import com.amit_kundu_io.playlist.routes.playlistRoutes
import com.amit_kundu_io.utility.ApiResponses
import com.plugins.Backblaze_B2.routes.uploadRoutes
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.utils.io.*

@OptIn(ExperimentalKtorApi::class)
/** Registers public HTTP endpoints after serialization and DI are ready. */
fun Application.configureRouting() {
    routing {
        get("/") {
            call.respond("Melofy Server running......")
        }.doc()
        songRoutes()
        playlistRoutes()
        uploadRoutes()
    }
}


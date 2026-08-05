package com.amit_kundu_io.config

import com.amit_kundu_io.utility.halper.doc
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.utils.io.*

@OptIn(ExperimentalKtorApi::class)
fun Application.configureRouting() {
    routing {
        get("/") {
            call.respondText("Hello, World!")
        }.doc()
    }
}


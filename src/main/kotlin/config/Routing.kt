package com.amit_kundu_io.config

import com.amit_kundu_io.songRoutes
import com.amit_kundu_io.utility.halper.doc
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.resources.*
import io.ktor.server.resources.*
import io.ktor.server.routing.openapi.describe
import io.ktor.utils.io.ExperimentalKtorApi
import kotlinx.serialization.Serializable

@OptIn(ExperimentalKtorApi::class)
fun Application.configureRouting() {
    routing {
        get("/") {
            call.respondText("Hello, World!")
        }.doc()


        songRoutes()

    }
}


@Serializable
@Resource("/name")
class Name

package com.amit_kundu_io.config

import com.amit_kundu_io.utility.ApiResponses
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.response.*
import org.slf4j.LoggerFactory

/** Converts uncaught failures into the project's standard API error envelope. */
fun Application.configureStatusPages() {
    val logger = LoggerFactory.getLogger("StatusPages")
    install(StatusPages) {
        exception<BadRequestException> { call, cause ->
            call.respond(HttpStatusCode.BadRequest, ApiResponses.error(cause.message ?: "Invalid request", "BAD_REQUEST"))
        }
        exception<Throwable> { call, cause ->
            logger.error("Unhandled request failure", cause)
            call.respond(HttpStatusCode.InternalServerError, ApiResponses.error("Internal server error", "INTERNAL_SERVER_ERROR"))
        }
    }
}

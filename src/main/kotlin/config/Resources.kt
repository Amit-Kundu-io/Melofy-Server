package com.amit_kundu_io.config

import io.ktor.server.application.*
import io.ktor.server.resources.*

/** Enables Ktor's type-safe resource routing support. */
fun Application.configureResources() {
    install(Resources)
}

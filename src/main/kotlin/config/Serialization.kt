package com.amit_kundu_io.config

import io.ktor.server.application.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation

/** Enables Kotlinx JSON serialization for API responses and framework integrations. */
fun Application.configureSerialization() {
    install(ContentNegotiation) {
        json()
    }
}

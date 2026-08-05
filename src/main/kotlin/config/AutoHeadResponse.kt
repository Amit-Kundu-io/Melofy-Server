package com.amit_kundu_io.config

import io.ktor.server.application.*
import io.ktor.server.plugins.autohead.*

/** Lets Ktor answer HEAD requests for routes that already support GET. */
fun Application.configureAutoHeadResponse() {
    install(AutoHeadResponse)
}

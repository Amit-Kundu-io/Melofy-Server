package com.amit_kundu_io.config

import io.ktor.http.ContentType
import io.ktor.openapi.OpenApiInfo
import io.ktor.server.application.*
import io.ktor.server.plugins.compression.*
import io.ktor.server.plugins.swagger.*
import io.ktor.server.routing.*
import io.ktor.server.routing.openapi.OpenApiDocSource

fun Application.configureHttp() {
    install(Compression)
    routing {
        swaggerUI("/swagger") {
            info = OpenApiInfo(
                title = "Music API",
                version = "1.0.0"
            )

            source = OpenApiDocSource.Routing(
                ContentType.Application.Json
            ) {
                routingRoot.descendants()
            }
        }
    }
}

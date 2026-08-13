package com.amit_kundu_io

import com.amit_kundu_io.config.configureAutoHeadResponse
import com.amit_kundu_io.config.configureHttp
import com.amit_kundu_io.config.configureKoin
import com.amit_kundu_io.config.configureResources
import com.amit_kundu_io.config.configureRouting
import com.amit_kundu_io.config.configureSerialization
import com.amit_kundu_io.config.configureStatusPages
import com.amit_kundu_io.database.DatabaseFactory
import com.amit_kundu_io.database.FlywayFactory
import io.ktor.server.engine.*
import io.ktor.server.application.*
import io.ktor.server.netty.Netty

/** Starts the Netty server with the local development bind address. */
fun main() {
// Detect environment (default: local)

    val env = System.getenv("APP_ENV") ?: "local"

    // Use Render's dynamic port if available
    val port = System.getenv("PORT")?.toIntOrNull() ?: 8080

    // Bind host based on environment
    val host = if (env == "local") "127.0.0.1" else "0.0.0.0"

    embeddedServer(Netty, host = host, port = port, module = Application::module)
        .start(wait = true)
}



/** Application composition root: infrastructure is initialized before HTTP routes are registered. */
fun Application.module() {
    DatabaseFactory.init()
    FlywayFactory.migrate()

    configureKoin()
    configureSerialization()
    configureStatusPages()
    configureHttp()
    configureAutoHeadResponse()
    configureResources()
    configureRouting()
}

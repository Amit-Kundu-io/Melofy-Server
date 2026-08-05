package com.amit_kundu_io

import com.amit_kundu_io.config.configureAutoHeadResponse
import com.amit_kundu_io.config.configureHttp
import com.amit_kundu_io.config.configureResources
import com.amit_kundu_io.config.configureRouting
import com.amit_kundu_io.config.configureSecurity
import com.amit_kundu_io.config.configureSerialization
import com.amit_kundu_io.config.configureStatusPages
import io.ktor.server.engine.*
import io.ktor.server.application.*
import io.ktor.server.netty.Netty

fun main() {
    // Detect environment (default: local)
    val env = "local"

    // Use Render's dynamic port if available
    val port =  8080

    // Bind host based on environment
    val host =  "127.0.0.1"

    embeddedServer(Netty, host = host, port = port, module = Application::module)
        .start(wait = true)
}



fun Application.module() {
    configureRouting()
    //configureKoin()
    configureSerialization()
    configureStatusPages()
    configureHttp()
    configureAutoHeadResponse()
    configureResources()
    configureSecurity()
}

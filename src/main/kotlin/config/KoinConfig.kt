package com.amit_kundu_io.config

import io.ktor.server.application.*
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger


fun Application.configureKoin() {
    install(Koin) {
        slf4jLogger()
        // TODO: wire your real DI modules here, e.g. modules(serviceModule, repositoryModule)
        modules(
        )
    }
}
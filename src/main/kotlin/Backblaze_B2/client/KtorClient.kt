package com.plugins.Backblaze_B2.client

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.ResponseException
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

object KtorClient {

    fun create(): HttpClient {

        return HttpClient(CIO) {

            expectSuccess = true

            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true
                        explicitNulls = false
                        isLenient = true
                        encodeDefaults = true
                    }
                )
            }

            install(HttpTimeout) {
                // Uploading multi-GB parts to Backblaze over a slow network
                // leg can legitimately take minutes -- don't let the client
                // time out mid-part.
                requestTimeoutMillis = 10 * 60_000
                connectTimeoutMillis = 30_000
                socketTimeoutMillis = 10 * 60_000
            }

            HttpResponseValidator {
                handleResponseExceptionWithRequest { cause, _ ->
                    if (cause is ResponseException) {
                        println("HTTP ERROR: ${cause.response.status}")
                    }
                }
            }

            // NOTE: no defaultRequest{} base URL here -- every call in
            // BackblazeApiImpl already uses a full absolute URL (either the
            // fixed B2 authorize endpoint, or a URL returned by a previous
            // B2 response), so a default host has nothing to add and only
            // risks masking a mistake if that ever changes.
        }
    }
}

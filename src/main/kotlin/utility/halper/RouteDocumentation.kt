package com.amit_kundu_io.utility.halper

import com.sun.beans.introspect.PropertyInfo
import io.ktor.client.request.request
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.RouteScopedPlugin
import io.ktor.server.routing.Route
import io.ktor.server.routing.openapi.describe
import io.ktor.utils.io.ExperimentalKtorApi

@OptIn(ExperimentalKtorApi::class)
fun Route.doc(
    summary: String? = null,
) = describe {
    summary?.let {
        this.summary = summary
    }
    responses {
        HttpStatusCode.OK {
            description = "Success"
        }
    }
}
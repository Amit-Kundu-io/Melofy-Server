package com.amit_kundu_io

import io.ktor.http.HttpStatusCode
import kotlin.reflect.KClass

class ApiDoc {

    var summary: String? = null
    var description: String? = null

    var requestType: KClass<*>? = null
    internal val responses = mutableListOf<HttpStatusCode>()

    inline fun <reified T : Any> request() {
        requestType = T::class
    }

    fun ok() {
        responses += HttpStatusCode.OK
    }

    fun created() {
        responses += HttpStatusCode.Created
    }

    fun badRequest() {
        responses += HttpStatusCode.BadRequest
    }

    fun unauthorized() {
        responses += HttpStatusCode.Unauthorized
    }

    fun notFound() {
        responses += HttpStatusCode.NotFound
    }
}
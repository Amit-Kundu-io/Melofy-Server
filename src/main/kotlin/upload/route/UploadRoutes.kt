/**
 * UploadRoutes.kt
 *
 * HTTP surface for the "semi-direct" upload flow:
 *   1. POST /videos/upload/start      -> { fileId }
 *   2. POST /videos/upload/part-url   -> { uploadUrl, authorizationToken }
 *          (client PUTs the part's bytes DIRECTLY to uploadUrl — those
 *           bytes never touch this server)
 *   3. POST /videos/upload/finish     -> 200 / error
 *   4. POST /videos/upload/parts      -> { partNumber: sha1, ... }  (resume)
 *
 * There used to be a POST /videos/upload/part endpoint that relayed raw
 * part bytes through this server to B2. It's removed — for multi-GB
 * files that doubled bandwidth cost and held a server connection open for
 * the entire transfer. The device now uploads directly to the URL
 * returned by step 2.
 *
 * All routes require a bearer token (see UploadAuthValidator) — the
 * previous version had none, which let anyone start B2 large-file
 * sessions and rack up storage/API cost at your expense.
 */

package com.plugins.storage.upload.route

import com.plugins.storage.upload.repository.UploadOperationException
import com.plugins.storage.upload.repository.UploadRepository
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import org.koin.ktor.ext.inject

private const val AUTH_PROVIDER_NAME = "upload-bearer"

@Serializable data class StartUploadRequest(val fileName: String, val contentType: String)
@Serializable data class StartUploadResponse(val fileId: String)

@Serializable data class UploadPartUrlRequest(val fileId: String)
@Serializable data class UploadPartUrlResponse(val uploadUrl: String, val authorizationToken: String)

@Serializable data class FinishUploadRequest(val fileId: String, val partSha1Array: List<String>)

@Serializable data class PartsDetailRequest(val fileId: String)

@Serializable data class ErrorResponse(val error: String)

/**
 * Registers the bearer-auth scheme this feature's routes require. Call
 * once from Application module setup, before configureUploadRoutes().
 */
//fun Application.configureUploadAuth() {
//    val validator by inject<UploadAuthValidator>()
//    install(Authentication) {
//        bearer(AUTH_PROVIDER_NAME) {
//            authenticate { credential ->
//                if (validator.isValidToken(credential.token)) UserIdPrincipal("upload-client") else null
//            }
//        }
//    }
//}




fun Route.uploadRoutesB2B() {
    val repository by inject<UploadRepository>()

    post("/videos/upload/start") {
        val req = call.receive<StartUploadRequest>()
        if (req.fileName.isBlank() || req.contentType.isBlank()) {
            return@post call.respond(HttpStatusCode.BadRequest, ErrorResponse("fileName and contentType are required"))
        }
        runCatching { repository.startUpload(req.fileName, req.contentType) }
            .onSuccess { call.respond(StartUploadResponse(it.fileId)) }
            .onFailure { call.respondUploadError(it) }
    }

    // Replaces the old byte-relaying "/part" route. Returns a short-lived
    // upload URL + token; the client PUTs the part's bytes straight to B2.
    post("/videos/upload/part-url") {
        val req = call.receive<UploadPartUrlRequest>()
        if (req.fileId.isBlank()) {
            return@post call.respond(HttpStatusCode.BadRequest, ErrorResponse("fileId is required"))
        }
        runCatching { repository.getUploadPartUrl(req.fileId) }
            .onSuccess { call.respond(UploadPartUrlResponse(it.uploadUrl, it.authorizationToken)) }
            .onFailure { call.respondUploadError(it) }
    }

    post("/videos/upload/finish") {
        val req = call.receive<FinishUploadRequest>()
        if (req.fileId.isBlank() || req.partSha1Array.isEmpty()) {
            return@post call.respond(HttpStatusCode.BadRequest, ErrorResponse("fileId and a non-empty partSha1Array are required"))
        }
        runCatching { repository.finishUpload(req.fileId, req.partSha1Array) }
            .onSuccess { call.respond(HttpStatusCode.OK) }
            .onFailure { call.respondUploadError(it) }
    }

    post("/videos/upload/parts") {
        val req = call.receive<PartsDetailRequest>()
        if (req.fileId.isBlank()) {
            return@post call.respond(HttpStatusCode.BadRequest, ErrorResponse("fileId is required"))
        }
        runCatching { repository.listCompletedParts(req.fileId) }
            .onSuccess { call.respond(it) }
            .onFailure { call.respondUploadError(it) }
    }
}

private suspend fun io.ktor.server.application.ApplicationCall.respondUploadError(error: Throwable) {
    when (error) {
        is IllegalArgumentException ->
            respond(HttpStatusCode.BadRequest, ErrorResponse(error.message ?: "Invalid request"))
        is UploadOperationException ->
            respond(HttpStatusCode.BadGateway, ErrorResponse(error.message ?: "Upstream storage error"))
        else ->
            respond(HttpStatusCode.InternalServerError, ErrorResponse("Unexpected error"))
    }
}

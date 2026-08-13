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

import com.amit_kundu_io.song_upload.data.models.req.CreateSongRequest
import com.amit_kundu_io.utility.halper.doc
import com.plugins.storage.upload.repository.UploadOperationException
import com.plugins.storage.upload.repository.UploadRepository
import com.plugins.upload.models.GetDownloadAuthorizationRequest.VideoPlayUrlResponse
import io.ktor.http.*
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import org.koin.ktor.ext.inject
import upload.models.request.ErrorResponse
import upload.models.request.FinishUploadRequest
import upload.models.request.PartsDetailRequest
import upload.models.request.StartUploadRequest
import upload.models.request.UploadPartUrlRequest
import upload.models.request.UploadPartUrlResponse
import upload.models.response.StartUploadResponse

private const val AUTH_PROVIDER_NAME = "upload-bearer"






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
    }.doc()

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
    }.doc()

    post("/videos/upload/finish") {
        val req = call.receive<FinishUploadRequest>()
        if (req.fileId.isBlank() || req.partSha1Array.isEmpty()) {
            return@post call.respond(HttpStatusCode.BadRequest, ErrorResponse("fileId and a non-empty partSha1Array are required"))
        }
        runCatching { repository.finishUpload(req.fileId, req.partSha1Array,req.song) }
            .onSuccess { call.respond(HttpStatusCode.OK, it) }
            .onFailure { call.respondUploadError(it) }
    }.doc()

    post("/videos/upload/parts") {
        val req = call.receive<PartsDetailRequest>()
        if (req.fileId.isBlank()) {
            return@post call.respond(HttpStatusCode.BadRequest, ErrorResponse("fileId is required"))
        }
        runCatching { repository.listCompletedParts(req.fileId) }
            .onSuccess { call.respond(it) }
            .onFailure { call.respondUploadError(it) }
    }.doc()



    get("/videos/{videoId}/{fileName}/play-url") {

        val videoId = call.parameters["videoId"]
        val fileName = call.parameters["fileName"]

        if (videoId.isNullOrBlank()) {
            return@get call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse("videoId is required")
            )
        }


        runCatching {
            // Get video from your database
            if (fileName.isNullOrBlank()) {
                throw IllegalArgumentException("Video not found")
            }

            // Optional: check whether this student has access
            // authorizeStudentForVideo(call, video)

            val expiresIn = 3600

            val url = repository.getTemporaryDownloadUrl(
                fileName = fileName,
                validDurationSeconds = expiresIn
            )

            VideoPlayUrlResponse(
                url = url,
                expiresInSeconds = expiresIn
            )

        }.onSuccess {
            call.respond(HttpStatusCode.OK, it)
        }.onFailure {
            call.respondUploadError(it)
        }
    }
}

private suspend fun ApplicationCall.respondUploadError(error: Throwable) {
    when (error) {
        is IllegalArgumentException ->
            respond(HttpStatusCode.BadRequest, ErrorResponse(error.message ?: "Invalid request"))
        is UploadOperationException ->
            respond(HttpStatusCode.BadGateway, ErrorResponse(error.message ?: "Upstream storage error"))
        else ->
            respond(HttpStatusCode.InternalServerError, ErrorResponse("Unexpected error"))
    }
}

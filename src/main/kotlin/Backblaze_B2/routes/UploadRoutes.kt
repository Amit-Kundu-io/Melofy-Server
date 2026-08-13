package com.plugins.Backblaze_B2.routes

import com.plugins.Backblaze_B2.apis.BackblazeApi
import com.plugins.Backblaze_B2.apis.CancelLargeFileRequest
import com.plugins.Backblaze_B2.apis.FinishLargeFileRequest
import com.plugins.Backblaze_B2.apis.GetUploadPartUrlRequest
import com.plugins.Backblaze_B2.apis.ListPartsRequest
import com.plugins.Backblaze_B2.apis.StartLargeFileRequest
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import kotlinx.serialization.Serializable
import org.koin.ktor.ext.inject

@Serializable
data class WatchUrlResponse(val url: String)

@Serializable
data class CancelUploadRequest(val fileId: String)

/**
 * Orchestration-only routes for direct-to-B2 client uploads (Android
 * VideoUploadApiImpl and friends). The server NEVER sees the file bytes
 * here -- it only brokers short-lived B2 URLs/tokens; the client reads
 * its own parts and PUTs them straight to B2. This is what makes the
 * design scale to multi-GB files on a phone: peak server memory per
 * upload is a few JSON objects, not the file itself.
 *
 * Route <-> BackblazeApi mapping:
 *   /videos/upload/start     -> startLargeFile
 *   /videos/upload/part-url  -> getUploadPartUrl
 *   /videos/upload/finish    -> finishLargeFile
 *   /videos/upload/parts     -> listParts   (resume source of truth)
 *   /videos/upload/cancel    -> cancelLargeFile
 *   /videos/watch-url        -> generateWatchUrl
 *
 * NOTE: this module doesn't add its own auth check -- wrap these routes
 * in your existing `authenticate(...) { }` block same as the rest of the
 * API, since VideoUploadApiImpl sends a Bearer token to every call here.
 */
fun Route.uploadRoutes() {

    val api by inject<BackblazeApi>()

    post("/videos/upload/start") {
        val req = call.receive<StartUploadRequest>()
        val result = api.startLargeFile(
            StartLargeFileRequest(bucketId = "", fileName = req.fileName, contentType = req.contentType)
        )
        call.respond(StartUploadResponse(result.fileId))
    }

    post("/videos/upload/part-url") {
        val req = call.receive<UploadPartUrlRequest>()
        val result = api.getUploadPartUrl(GetUploadPartUrlRequest(fileId = req.fileId))
        call.respond(UploadPartUrlResponse(result.uploadUrl, result.authorizationToken))
    }

    post("/videos/upload/finish") {
        val req = call.receive<FinishUploadRequest>()
        val result = api.finishLargeFile(
            FinishLargeFileRequest(fileId = req.fileId, partSha1Array = req.partSha1sInOrder)
        )
        call.respond(HttpStatusCode.OK, mapOf("fileId" to result.fileId))
    }

    post("/videos/upload/parts") {
        val req = call.receive<PartsDetailRequest>()
        val result = api.listParts(ListPartsRequest(fileId = req.fileId))
        val partSha1ByNumber = result.parts.associate { it.partNumber to it.contentSha1 }
        call.respond(partSha1ByNumber)
    }

    post("/videos/upload/cancel") {
        val req = call.receive<CancelUploadRequest>()
        api.cancelLargeFile(CancelLargeFileRequest(fileId = req.fileId))
        call.respond(HttpStatusCode.OK)
    }

    post("/videos/watch-url") {
        val fileName = call.request.headers["X-File-Name"]
            ?: return@post call.respond(HttpStatusCode.BadRequest, "Missing X-File-Name header")

        val url = api.generateWatchUrl(fileName)
        call.respond(HttpStatusCode.OK, WatchUrlResponse(url))
    }
}

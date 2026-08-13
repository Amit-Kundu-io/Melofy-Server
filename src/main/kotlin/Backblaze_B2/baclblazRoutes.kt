package com.plugins.Backblaze_B2

import com.plugins.Backblaze_B2.apis.BackblazeApi
import com.plugins.Backblaze_B2.storage.BackblazeStorage
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.contentLength
import io.ktor.server.request.receiveChannel
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import java.util.UUID
import org.koin.ktor.ext.inject
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("BackblazeUpload")

fun Route.baclblazRoutes() {

    val uploadService by inject<BackblazeStorage>()
    val api by inject<BackblazeApi>()

    route("/videos") {

        // Raw-stream upload: NOT multipart. The whole request body is the
        // file. Metadata travels in headers instead of form fields, which
        // lets Ktor start reading (and forwarding to Backblaze) bytes the
        // moment they arrive, instead of waiting for a multipart parser to
        // find and hand off the file part.
        post("/upload_stream") {

            val fileName = call.request.headers["X-File-Name"]
                ?: throw IllegalArgumentException("X-File-Name header is required.")

            val contentType = call.request.headers["X-Content-Type"]
                ?: "application/octet-stream"

            val contentLength = call.request.contentLength()
                ?: throw IllegalArgumentException(
                    "Content-Length is required and must be known up front " +
                        "for large file uploads."
                )

            // Optional: client-generated id so it can poll progress on a
            // separate connection while this request is still streaming.
            val uploadId = call.request.headers["X-Upload-Id"] ?: UUID.randomUUID().toString()
            UploadProgressRegistry.start(uploadId, contentLength)

            try {
                val result = uploadService.uploadVideo(
                    fileName = fileName,
                    contentType = contentType,
                    channel = call.receiveChannel(),
                    contentLength = contentLength,
                    onPartUploaded = { bytes -> UploadProgressRegistry.addBytes(uploadId, bytes) }
                )

                call.respond(HttpStatusCode.Created, result)

            } catch (e: Exception) {
                // Without this, an exception thrown while the client is
                // still mid-upload can surface on the client as a raw
                // connection/stream reset instead of a real error message
                // -- log the actual cause here every time.
                logger.error("Upload failed for uploadId=$uploadId, fileName=$fileName", e)
                throw e // let StatusPages (see Application setup) turn this into a clean JSON error response
            } finally {
                UploadProgressRegistry.finish(uploadId)
            }
        }

        // Poll this from a second connection (e.g. every second) using the
        // same X-Upload-Id sent with the upload request.
        get("/upload_status/{uploadId}") {

            val uploadId = call.parameters["uploadId"]
                ?: throw IllegalArgumentException("uploadId is required.")

            val (done, total) = UploadProgressRegistry.snapshot(uploadId)
                ?: return@get call.respond(HttpStatusCode.NotFound)

            call.respond(
                HttpStatusCode.OK,
                mapOf("bytesUploaded" to done, "totalBytes" to total)
            )
        }

    }
}


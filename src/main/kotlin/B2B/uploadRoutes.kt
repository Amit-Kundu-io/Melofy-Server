package B2B

import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import kotlinx.serialization.Serializable
import org.koin.ktor.ext.inject
import kotlin.getValue

@Serializable
data class StartUploadRequest(val fileName: String, val contentType: String)

@Serializable
data class StartUploadResponse(val fileId: String)

@Serializable
data class FinishUploadRequest(val fileId: String, val partSha1Array: List<String>)

@Serializable
data class PartsDetailRequest(val fileId: String)

fun Route.uploadRoutesB2B1() {

    val b2Client by inject<B2Client>()


    post("/videos/upload/start") {
        val req = call.receive<StartUploadRequest>()
        val result = b2Client.startLargeFile(req.fileName, req.contentType)
        call.respond(StartUploadResponse(result.fileId))
    }

    post("/videos/upload/part") {
        val fileId = call.request.headers["X-File-Id"]
            ?: return@post call.respond(HttpStatusCode.BadRequest, "Missing X-File-Id")
        val partNumber = call.request.headers["X-Part-Number"]?.toIntOrNull()
            ?: return@post call.respond(HttpStatusCode.BadRequest, "Missing X-Part-Number")
        val sha1 = call.request.headers["X-Content-Sha1"]
            ?: return@post call.respond(HttpStatusCode.BadRequest, "Missing X-Content-Sha1")

        val bytes = call.receive<ByteArray>()

        val uploadUrlInfo = b2Client.getUploadPartUrl(fileId)
        val result = b2Client.uploadPart(
            uploadUrl = uploadUrlInfo.uploadUrl,
            uploadAuthToken = uploadUrlInfo.authorizationToken,
            partNumber = partNumber,
            sha1 = sha1,
            contentLength = bytes.size.toLong(),
            bytes = bytes
        )
        call.respond(HttpStatusCode.OK, result)
    }

    post("/videos/upload/finish") {
        val req = call.receive<FinishUploadRequest>()
        val success = b2Client.finishLargeFile(req.fileId, req.partSha1Array)
        if (success) call.respond(HttpStatusCode.OK)
        else call.respond(HttpStatusCode.InternalServerError, "B2 finish_large_file failed")
    }

    post("/videos/upload/parts_detail") {
        val req = call.receive<PartsDetailRequest>()
        val parts = b2Client.listPartsWithSha1(req.fileId)
        call.respond(parts)
    }

}
/**
 * UploadRepositoryImpl.kt
 *
 * B2-backed implementation of UploadRepository. Translates B2Client's
 * B2ApiException into UploadOperationException so routes never need to
 * know B2-specific exception types.
 */

package com.plugins.storage.upload.repository

import B2B.B2Client
import com.plugins.storage.upload.b2.B2ApiException

class UploadRepositoryImpl(
    private val b2Client: B2Client
) : UploadRepository {

    override suspend fun startUpload(fileName: String, contentType: String): StartUploadResult =
        wrapErrors("start upload for '$fileName'") {
            val result = b2Client.startLargeFile(fileName, contentType)
            StartUploadResult(fileId = result.fileId, fileName = result.fileName)
        }

    override suspend fun getUploadPartUrl(fileId: String): UploadPartUrlResult =
        wrapErrors("get upload part URL for fileId '$fileId'") {
            val result = b2Client.getUploadPartUrl(fileId)
            UploadPartUrlResult(uploadUrl = result.uploadUrl, authorizationToken = result.authorizationToken)
        }

    override suspend fun finishUpload(fileId: String, partSha1InOrder: List<String>) {
        wrapErrors("finish upload for fileId '$fileId'") {
            require(partSha1InOrder.isNotEmpty()) { "partSha1InOrder must not be empty" }
            b2Client.finishLargeFile(fileId, partSha1InOrder)
        }
    }

    override suspend fun listCompletedParts(fileId: String): Map<Int, String> =
        wrapErrors("list completed parts for fileId '$fileId'") {
            b2Client.listPartsWithSha1(fileId)
        }

    private suspend inline fun <T> wrapErrors(action: String, block: suspend () -> T): T = try {
        block()
    } catch (e: B2ApiException) {
        throw UploadOperationException("Failed to $action: ${e.message}", e)
    } catch (e: IllegalArgumentException) {
        throw e // validation errors — let routes map these to 400 distinctly
    } catch (e: Exception) {
        throw UploadOperationException("Failed to $action", e)
    }
}

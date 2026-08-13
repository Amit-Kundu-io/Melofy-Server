/**
 * UploadRepositoryImpl.kt
 *
 * B2-backed implementation of UploadRepository. Translates B2Client's
 * B2ApiException into UploadOperationException so routes never need to
 * know B2-specific exception types.
 */

package com.plugins.storage.upload.repository

import com.plugins.upload.b2.B2Client
import utility.wrapErrors

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

    override suspend fun finishUpload(
        fileId: String,
        partSha1InOrder: List<String>
    ): String =
        wrapErrors("finish upload for fileId '$fileId'") {
            require(partSha1InOrder.isNotEmpty()) {
                "partSha1InOrder must not be empty"
            }

            val result = b2Client.finishLargeFile(
                fileId,
                partSha1InOrder
            )

            b2Client.getDownloadUrl(result.fileName)
        }

    override suspend fun listCompletedParts(fileId: String): Map<Int, String> =
        wrapErrors("list completed parts for fileId '$fileId'") {
            b2Client.listPartsWithSha1(fileId)
        }

    override suspend fun getTemporaryDownloadUrl(
        fileName: String,
        validDurationSeconds: Int
    ): String =
        wrapErrors("generate temporary download URL") {
            b2Client.getTemporaryDownloadUrl(
                fileName = fileName,
                validDurationSeconds = validDurationSeconds
            )
        }


}

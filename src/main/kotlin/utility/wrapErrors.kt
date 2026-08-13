package utility

import com.plugins.storage.upload.repository.UploadOperationException
import upload.util.B2ApiException


suspend inline fun <T> wrapErrors(action: String, block: suspend () -> T): T = try {
    block()
} catch (e: B2ApiException) {
    throw UploadOperationException("Failed to $action: ${e.message}", e)
} catch (e: IllegalArgumentException) {
    throw e // validation errors — let routes map these to 400 distinctly
} catch (e: Exception) {
    throw UploadOperationException("Failed to $action", e)
}

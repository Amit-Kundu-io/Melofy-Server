package upload.auth

import com.plugins.upload.auth.UploadAuthValidator
import java.security.MessageDigest

class SharedSecretUploadAuthValidator(
    private val expectedToken: String
) : UploadAuthValidator {

    init {
        require(expectedToken.isNotBlank()) {
            "Upload API token must not be blank — set the UPLOAD_API_TOKEN environment variable"
        }
    }

    override fun isValidToken(token: String): Boolean {
        val expectedBytes = expectedToken.toByteArray()
        val actualBytes = token.toByteArray()
        // Constant-time comparison — a naive == leaks timing info about
        // how many leading bytes matched, which matters for a bearer
        // secret checked on every request.
        return MessageDigest.isEqual(expectedBytes, actualBytes)
    }
}
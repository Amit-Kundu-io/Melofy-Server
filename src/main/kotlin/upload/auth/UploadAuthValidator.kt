package com.plugins.upload.auth


/**
 * UploadAuthValidator.kt
 *
 * The previous version of these routes had NO authentication at all —
 * anyone who found the URL could start B2 large-file uploads at your
 * expense. This is the minimum fix: every /videos/upload/* route now
 * requires a valid bearer token (checked in UploadRoutes via Ktor's
 * Authentication plugin).
 *
 * SharedSecretUploadAuthValidator is a placeholder good enough to close
 * the "wide open" hole immediately (single shared token from env/secrets
 * manager, constant-time compared). For a real multi-user production app,
 * replace the binding in UploadModule with a validator that checks a
 * per-user session/JWT issued by your existing auth system, so you can
 * also authorize "does THIS user own THIS fileId" in the routes — that
 * ownership check is out of scope for a shared-secret token and should be
 * added once real user auth is wired in.
 *
*/*/

import java.security.MessageDigest

interface UploadAuthValidator {
    fun isValidToken(token: String): Boolean
}

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

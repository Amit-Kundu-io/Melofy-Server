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



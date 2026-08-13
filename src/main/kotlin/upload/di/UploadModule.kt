/**
 * UploadModule.kt
 *
 * Single Koin module for the upload feature. Install with:
 *   install(Koin) { modules(uploadModule) }
 *   configureUploadAuth()      // from UploadRoutes.kt — installs Authentication
 *   configureUploadRoutes()    // from UploadRoutes.kt — registers the routes
 *
 * Secrets (B2 key pair, upload API token) are read from environment
 * variables, never hardcoded — fail fast at startup if any are missing
 * rather than surfacing a confusing failure on the first request.
 */

package com.plugins.storage.upload.di


import com.plugins.storage.upload.repository.UploadRepository
import com.plugins.storage.upload.repository.UploadRepositoryImpl
import com.plugins.upload.auth.UploadAuthValidator
import com.plugins.upload.b2.B2Client
import com.plugins.upload.b2.KtorClient
import com.studycore.util.Env
import com.studycore.util.Env.B2_APP_KEY
import com.studycore.util.Env.B2_BUCKET_ID
import com.studycore.util.Env.KEY_ID
import io.ktor.client.HttpClient
import org.koin.dsl.module
import upload.auth.SharedSecretUploadAuthValidator

private fun requiredEnv(name: String): String =
    System.getenv(name)?.takeIf { it.isNotBlank() }
        ?: error("Missing required environment variable: $name")

val uploadModule1 = module {

    single<HttpClient> { KtorClient.create() }


    single {
        B2Client(
            keyId = KEY_ID,
            bucketId = B2_BUCKET_ID,
            appKey = B2_APP_KEY,
            httpClient = get()
        )
    }

    single<UploadRepository> { UploadRepositoryImpl(get(),get()) }

    single<UploadAuthValidator> { SharedSecretUploadAuthValidator(requiredEnv("UPLOAD_API_TOKEN")) }
}

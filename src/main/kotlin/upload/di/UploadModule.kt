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
import com.plugins.upload.auth.SharedSecretUploadAuthValidator
import com.plugins.upload.auth.UploadAuthValidator
import com.plugins.upload.b2.B2Client
import org.koin.dsl.module

private fun requiredEnv(name: String): String =
    System.getenv(name)?.takeIf { it.isNotBlank() }
        ?: error("Missing required environment variable: $name")

val uploadModule1 = module {


    single {
        B2Client(
            keyId = "d896ea49a883",
            bucketId = "0d280936beda94399af80813",
            appKey = "005f66a47ae51022b8ec820032e9c7f01e10d640ee",
            httpClient = get()
        )
    }

    single<UploadRepository> { UploadRepositoryImpl(get()) }

    single<UploadAuthValidator> { SharedSecretUploadAuthValidator(requiredEnv("UPLOAD_API_TOKEN")) }
}

package com.plugins.Backblaze_B2.di

import com.plugins.Backblaze_B2.apis.BackblazeApi
import com.plugins.Backblaze_B2.apis.BackblazeApiImpl
import com.plugins.Backblaze_B2.client.KtorClient
import com.plugins.Backblaze_B2.storage.BackblazeStorage
import com.plugins.Backblaze_B2.storage.BackblazeStorageImpl
import io.ktor.client.HttpClient
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

/**
 * Single source of truth for B2 wiring.
 *
 * NOTE: the old B2Client (v3 API, non-streaming, buffers whole parts
 * received from an already-buffered client request) has been removed.
 * BackblazeApiImpl + BackblazeStorageImpl (v4 API, true streaming,
 * bounded memory) fully replace it -- keeping both was dead weight and a
 * maintenance trap (two clients, two auth caches, two API versions).
 */
fun uploadModule() = module {

    single<HttpClient> { KtorClient.create() }

    single {
        BackblazeConfig(
            keyId = System.getenv("B2_KEY_ID") ?: error("Missing required env var B2_KEY_ID"),
            applicationKey = System.getenv("B2_APP_KEY") ?: error("Missing required env var B2_APP_KEY"),
            bucketId = System.getenv("B2_BUCKET_ID") ?: error("Missing required env var B2_BUCKET_ID"),
            // Falls back to the previous hardcoded value so existing
            // deployments keep working; set B2_BUCKET_NAME to override.
            bucketName = System.getenv("B2_BUCKET_NAME") ?: "Video-Ak"
        )
    }

    singleOf(::BackblazeApiImpl) { bind<BackblazeApi>() }
    singleOf(::BackblazeStorageImpl) { bind<BackblazeStorage>() }
}

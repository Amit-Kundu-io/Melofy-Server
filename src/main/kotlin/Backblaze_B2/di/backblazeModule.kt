package com.plugins.Backblaze_B2.di

import B2B.B2Client
import com.plugins.Backblaze_B2.apis.BackblazeApi
import com.plugins.Backblaze_B2.apis.BackblazeApiImpl
import com.plugins.Backblaze_B2.client.KtorClient
import com.plugins.Backblaze_B2.storage.BackblazeStorage
import com.plugins.Backblaze_B2.storage.BackblazeStorageImpl
import io.ktor.client.HttpClient
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

fun uploadModule() = module {

    // This binding was missing before -- BackblazeApiImpl needs an
    // HttpClient injected, and without registering one here Koin fails at
    // startup trying to resolve BackblazeApiImpl's constructor.
    single<HttpClient> { KtorClient.create() }

    single {
        BackblazeConfig(
            keyId = "d896ea49a883",//System.getenv("B2_KEY_ID") ?: error("Missing required env var B2_KEY_ID"),
            applicationKey = "005f66a47ae51022b8ec820032e9c7f01e10d640ee",//System.getenv("B2_APPLICATION_KEY") ?: error("Missing required env var B2_APPLICATION_KEY"),
            bucketId ="0d280936beda94399af80813" ,//System.getenv("B2_BUCKET_ID") ?: error("Missing required env var B2_BUCKET_ID")
            bucketName = "Video-Ak"
        )
    }

    singleOf(::BackblazeApiImpl) { bind<BackblazeApi>() }
    singleOf(::BackblazeStorageImpl) { bind<BackblazeStorage>() }

    single {
        B2Client(
            keyId = "d896ea49a883",//System.getenv("B2_KEY_ID") ?: error("Missing required env var B2_KEY_ID"),
            bucketId = "0d280936beda94399af80813",//System.getenv("B2_BUCKET_ID") ?: error("Missing required env var B2_BUCKET_ID")
            appKey = "005f66a47ae51022b8ec820032e9c7f01e10d640ee",
            httpClient = get()
        )
    }
}

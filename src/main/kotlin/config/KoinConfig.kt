package com.amit_kundu_io.config

import com.amit_kundu_io.song_upload.data.repoimpl.SongRepositoryImpl
import com.amit_kundu_io.song_upload.service.service_impl.SongServiceImpl
import com.amit_kundu_io.playlist.data.repoimpl.PlaylistRepositoryImpl
import com.amit_kundu_io.playlist.data.repo.PlaylistRepository
import com.amit_kundu_io.playlist.service.PlaylistService
import com.amit_kundu_io.playlist.service.PlaylistServiceImpl
import com.amit_kundu_io.song_upload.data.repo.SongRepository
import com.amit_kundu_io.song_upload.service.service.SongService
import com.plugins.storage.upload.di.uploadModule1
import io.ktor.server.application.*
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger
import org.koin.dsl.module


/** Registers application services and repositories with Koin. */
fun Application.configureKoin() {
    install(Koin) {
        slf4jLogger()
        modules(songModule,uploadModule1)
    }
}

/** Dependency graph for the song and playlist feature modules. */
private val songModule = module {
    single<SongRepository> { SongRepositoryImpl() }
    single<SongService> { SongServiceImpl(get()) }
    single<PlaylistRepository> { PlaylistRepositoryImpl() }
    single<PlaylistService> { PlaylistServiceImpl(get()) }
}

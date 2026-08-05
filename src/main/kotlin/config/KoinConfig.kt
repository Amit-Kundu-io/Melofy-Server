package com.amit_kundu_io.config

import com.amit_kundu_io.song_upload.SongRepository
import com.amit_kundu_io.song_upload.SongRepositoryImpl
import com.amit_kundu_io.song_upload.SongService
import com.amit_kundu_io.song_upload.SongServiceImpl
import com.amit_kundu_io.playlist.PlaylistRepository
import com.amit_kundu_io.playlist.PlaylistRepositoryImpl
import com.amit_kundu_io.playlist.PlaylistService
import com.amit_kundu_io.playlist.PlaylistServiceImpl
import io.ktor.server.application.*
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger
import org.koin.dsl.module


/** Registers application services and repositories with Koin. */
fun Application.configureKoin() {
    install(Koin) {
        slf4jLogger()
        modules(songModule)
    }
}

/** Dependency graph for the song and playlist feature modules. */
private val songModule = module {
    single<SongRepository> { SongRepositoryImpl() }
    single<SongService> { SongServiceImpl(get()) }
    single<PlaylistRepository> { PlaylistRepositoryImpl() }
    single<PlaylistService> { PlaylistServiceImpl(get()) }
}

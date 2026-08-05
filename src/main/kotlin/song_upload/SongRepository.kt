package com.amit_kundu_io.song_upload

import com.amit_kundu_io.playlist.PlaylistNotFoundException
import com.amit_kundu_io.playlist.PlaylistSongsTable
import com.amit_kundu_io.playlist.PlaylistsTable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.plus
import org.jetbrains.exposed.v1.core.minus
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update
import java.time.Instant
import kotlin.uuid.Uuid

/** Database contract for song writes and deletes. */
interface SongRepository {
    /** Persists a song and optionally creates its initial playlist membership. */
    suspend fun create(song: NewSong, playlistId: Uuid?): Song
    /** Removes a song by UUID and reports whether a record existed. */
    suspend fun delete(id: Uuid): Boolean
}

/** JDBC implementation; song creation and optional playlist attachment share one transaction. */
class SongRepositoryImpl : SongRepository {
    /** Inserts a song and, when supplied, its playlist membership atomically. */
    override suspend fun create(song: NewSong, playlistId: Uuid?): Song = withContext(Dispatchers.IO) {
        transaction {
            if (playlistId != null && PlaylistsTable.selectAll().where { PlaylistsTable.id eq playlistId }.empty()) {
                throw PlaylistNotFoundException()
            }
            val now = Instant.now()
            val id = Uuid.random()
            SongsTable.insert {
                it[SongsTable.id] = id
                it[title] = song.title
                it[artistName] = song.artistName
                it[albumName] = song.albumName
                it[description] = song.description
                it[genre] = song.genre
                it[language] = song.language
                it[durationSeconds] = song.durationSeconds
                it[audioUrl] = song.audioUrl
                it[artworkUrl] = song.artworkUrl
                it[releaseDate] = song.releaseDate
                it[isExplicit] = song.isExplicit
                it[createdAt] = now
                it[updatedAt] = now
            }
            if (playlistId != null) {
                PlaylistSongsTable.insert {
                    it[PlaylistSongsTable.playlistId] = playlistId
                    it[PlaylistSongsTable.songId] = id
                    it[PlaylistSongsTable.addedAt] = now
                }
                PlaylistsTable.update({ PlaylistsTable.id eq playlistId }) {
                    it[PlaylistsTable.songCount] = PlaylistsTable.songCount + 1
                }
            }
            Song(
                id = id,
                title = song.title,
                artistName = song.artistName,
                audioUrl = song.audioUrl,
                albumName = song.albumName,
                description = song.description,
                genre = song.genre,
                language = song.language,
                durationSeconds = song.durationSeconds,
                artworkUrl = song.artworkUrl,
                releaseDate = song.releaseDate,
                isExplicit = song.isExplicit,
                createdAt = now,
                updatedAt = now,
            )
        }
    }

    /** Deletes a song and adjusts the cached count of every affected playlist. */
    override suspend fun delete(id: Uuid): Boolean = withContext(Dispatchers.IO) {
        transaction {
            val playlistIds = PlaylistSongsTable.selectAll()
                .where { PlaylistSongsTable.songId eq id }
                .map { it[PlaylistSongsTable.playlistId] }
            val deleted = SongsTable.deleteWhere { SongsTable.id eq id } == 1
            if (deleted) {
                playlistIds.forEach { playlistId ->
                    PlaylistsTable.update({ PlaylistsTable.id eq playlistId }) {
                        it[PlaylistsTable.songCount] = PlaylistsTable.songCount - 1
                    }
                }
            }
            deleted
        }
    }
}

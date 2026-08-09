package com.amit_kundu_io.playlist.data.repoimpl

import com.amit_kundu_io.playlist.data.module.Playlist
import com.amit_kundu_io.playlist.data.repo.PlaylistRepository
import com.amit_kundu_io.playlist.db_table.PlaylistSongsTable
import com.amit_kundu_io.playlist.db_table.PlaylistsTable
import com.amit_kundu_io.song_upload.data.models.Song
import com.amit_kundu_io.song_upload.db_table.SongsTable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.v1.core.Op
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.greater
import org.jetbrains.exposed.v1.core.less
import org.jetbrains.exposed.v1.core.plus
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update
import java.time.Instant
import kotlin.uuid.Uuid



/** Indexed JDBC implementation using keyset pagination for large collections. */
class PlaylistRepositoryImpl : PlaylistRepository {
    /** Inserts playlist metadata and starts its song count at zero. */
    override suspend fun create(name: String, description: String?, artworkUrl: String?): Playlist = withContext(Dispatchers.IO) {
        transaction {
            val now = Instant.now()
            val id = Uuid.random()
            PlaylistsTable.insert {
                it[PlaylistsTable.id] = id
                it[PlaylistsTable.name] = name
                it[PlaylistsTable.description] = description
                it[PlaylistsTable.artworkUrl] = artworkUrl
                it[PlaylistsTable.songCount] = 0
                it[PlaylistsTable.createdAt] = now
                it[PlaylistsTable.updatedAt] = now
            }
            Playlist(id, name, description, artworkUrl, 0, now, now)
        }
    }

    /** Finds one playlist by its UUID, or returns null when it does not exist. */
    override suspend fun find(id: Uuid): Playlist? = withContext(Dispatchers.IO) {
        transaction { PlaylistsTable.selectAll().where { PlaylistsTable.id eq id }.singleOrNull()?.toPlaylist() }
    }

    /** Retrieves the next playlist page using the primary-key cursor. */
    override suspend fun list(cursor: Uuid?, limit: Int): List<Playlist> = withContext(Dispatchers.IO) {
        transaction {
            PlaylistsTable.selectAll()
                .where { cursor?.let { PlaylistsTable.id greater it } ?: Op.TRUE }
                .orderBy(PlaylistsTable.id to SortOrder.ASC)
                .limit(limit)
                .map { it.toPlaylist() }
        }
    }

    /** Counts playlists for the list response's totalItems field. */
    override suspend fun totalCount(): Long = withContext(Dispatchers.IO) {
        transaction { PlaylistsTable.selectAll().count() }
    }

    /** Adds an existing song once and increments the playlist's cached song count. */
    override suspend fun addSong(playlistId: Uuid, songId: Uuid): Boolean = withContext(Dispatchers.IO) {
        transaction {
            if (PlaylistsTable.selectAll().where { PlaylistsTable.id eq playlistId }.empty()) return@transaction false
            if (SongsTable.selectAll().where { SongsTable.id eq songId }.empty()) return@transaction false
            if (!PlaylistSongsTable.selectAll().where {
                    (PlaylistSongsTable.playlistId eq playlistId) and (PlaylistSongsTable.songId eq songId)
                }.empty()) return@transaction true
            PlaylistSongsTable.insert {
                it[PlaylistSongsTable.playlistId] = playlistId
                it[PlaylistSongsTable.songId] = songId
                it[PlaylistSongsTable.addedAt] = Instant.now()
            }
            PlaylistsTable.update({ PlaylistsTable.id eq playlistId }) {
                it[PlaylistsTable.songCount] = PlaylistsTable.songCount + 1
            }
            true
        }
    }

    /** Retrieves one indexed page of songs for a playlist without OFFSET scans. */
    override suspend fun songs(playlistId: Uuid, cursor: Long?, limit: Int): List<Pair<Long, Song>> = withContext(Dispatchers.IO) {
        transaction {
            val query = SongsTable.innerJoin(PlaylistSongsTable)
                .selectAll()
                .where {
                    (PlaylistSongsTable.playlistId eq playlistId) and
                        (cursor?.let { PlaylistSongsTable.id less it } ?: Op.TRUE)
                }
                .orderBy(PlaylistSongsTable.id to SortOrder.DESC)
                .limit(limit)
            query.map { it[PlaylistSongsTable.id] to it.toSong() }
        }
    }

    /** Maps a playlist query row to the internal playlist entity. */
    private fun ResultRow.toPlaylist() = Playlist(
        id = this[PlaylistsTable.id], name = this[PlaylistsTable.name],
        description = this[PlaylistsTable.description], artworkUrl = this[PlaylistsTable.artworkUrl],
        songCount = this[PlaylistsTable.songCount],
        createdAt = this[PlaylistsTable.createdAt], updatedAt = this[PlaylistsTable.updatedAt],
    )

    /** Maps a joined playlist-song query row to the internal song entity. */
    private fun ResultRow.toSong() = Song(
        id = this[SongsTable.id], title = this[SongsTable.title], artistName = this[SongsTable.artistName],
        audioUrl = this[SongsTable.audioUrl], albumName = this[SongsTable.albumName],
        description = this[SongsTable.description], genre = this[SongsTable.genre], language = this[SongsTable.language],
        durationSeconds = this[SongsTable.durationSeconds], artworkUrl = this[SongsTable.artworkUrl],
        releaseDate = this[SongsTable.releaseDate], isExplicit = this[SongsTable.isExplicit],
        createdAt = this[SongsTable.createdAt], updatedAt = this[SongsTable.updatedAt],
    )
}

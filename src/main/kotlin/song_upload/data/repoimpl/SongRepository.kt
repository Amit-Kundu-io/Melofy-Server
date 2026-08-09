package com.amit_kundu_io.song_upload.data.repoimpl

import com.amit_kundu_io.playlist.db_table.PlaylistSongsTable
import com.amit_kundu_io.playlist.db_table.PlaylistsTable
import com.amit_kundu_io.song_upload.data.models.NewSong
import com.amit_kundu_io.song_upload.data.models.Song

import com.amit_kundu_io.song_upload.data.models.res.SongResponse
import com.amit_kundu_io.song_upload.db_table.SongsTable
import com.amit_kundu_io.song_upload.data.repo.SongRepository
import com.amit_kundu_io.utility.validation_exception.PlaylistNotFoundException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.v1.core.JoinType
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.like
import org.jetbrains.exposed.v1.core.lowerCase
import org.jetbrains.exposed.v1.core.plus
import org.jetbrains.exposed.v1.core.minus
import org.jetbrains.exposed.v1.core.or
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update
import java.time.Instant
import kotlin.uuid.Uuid


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

    override suspend fun songsByPlayListId(
        playListId: String
    ): List<SongResponse> = withContext(Dispatchers.IO) {

        val playlistUuid = Uuid.parse(playListId)

        transaction {
            PlaylistSongsTable
                .join(
                    otherTable = SongsTable,
                    joinType = JoinType.INNER,
                    onColumn = PlaylistSongsTable.songId,
                    otherColumn = SongsTable.id
                )
                .selectAll()
                .where {
                    PlaylistSongsTable.playlistId eq playlistUuid
                }
                .orderBy(PlaylistSongsTable.id to SortOrder.ASC)
                .map { row ->
                    SongResponse(
                        id = row[SongsTable.id].toString(),
                        title = row[SongsTable.title],
                        artistName = row[SongsTable.artistName],
                        albumName = row[SongsTable.albumName],
                        description = row[SongsTable.description],
                        genre = row[SongsTable.genre],
                        language = row[SongsTable.language],
                        durationSeconds = row[SongsTable.durationSeconds],
                        audioUrl = row[SongsTable.audioUrl],
                        artworkUrl = row[SongsTable.artworkUrl],
                        releaseDate = row[SongsTable.releaseDate]?.toString(),
                        isExplicit = row[SongsTable.isExplicit],
                        createdAt = row[SongsTable.createdAt].toString(),
                        updatedAt = row[SongsTable.updatedAt].toString()
                    )
                }
        }
    }

    override suspend fun suggestName(text: String): List<String> {

        val query = text.trim()

        if (query.length < 2) {
            return emptyList()
        }

        val search = query.lowercase()

        return transaction {

            SongsTable
                .select(SongsTable.title)
                .where {
                    (SongsTable.title.lowerCase() like "$search%") or
                            (SongsTable.artistName.lowerCase() like "$search%")
                }
                .orderBy(
                    SongsTable.title to SortOrder.ASC
                )
                .limit(10)
                .map { row ->
                    row[SongsTable.title]
                }
                .distinct()
        }
    }

    override suspend fun searchSongs(text: String): List<SongResponse> {

        val query = text.trim()

        if (query.isBlank()) { return emptyList() }

        return transaction {

            SongsTable
                .selectAll()
                .where {
                    SongsTable.title.lowerCase() like "%${query.lowercase()}%"
                }
                .orderBy(
                    SongsTable.title to SortOrder.ASC
                )
                .limit(50)
                .map { row ->
                    SongResponse(
                        id = row[SongsTable.id].toString(),
                        title = row[SongsTable.title],
                        artistName = row[SongsTable.artistName],
                        albumName = row[SongsTable.albumName],
                        description = row[SongsTable.description],
                        genre = row[SongsTable.genre],
                        language = row[SongsTable.language],
                        durationSeconds = row[SongsTable.durationSeconds],
                        audioUrl = row[SongsTable.audioUrl],
                        artworkUrl = row[SongsTable.artworkUrl],
                        releaseDate = row[SongsTable.releaseDate]?.toString(),
                        isExplicit = row[SongsTable.isExplicit],
                        createdAt = row[SongsTable.createdAt].toString(),
                        updatedAt = row[SongsTable.updatedAt].toString()
                    )
                }
        }
    }
}

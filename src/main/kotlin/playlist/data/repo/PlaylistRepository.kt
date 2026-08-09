package com.amit_kundu_io.playlist.data.repo

import com.amit_kundu_io.playlist.data.module.Playlist
import com.amit_kundu_io.song_upload.data.models.Song
import kotlin.uuid.Uuid

/** Database contract for playlist reads and song membership changes. */
interface PlaylistRepository {
    /** Persists new playlist metadata. */
    suspend fun create(name: String, description: String?, artworkUrl: String?): Playlist
    /** Finds one playlist by its UUID. */
    suspend fun find(id: Uuid): Playlist?
    /** Returns the next primary-key cursor page of playlists. */
    suspend fun list(cursor: Uuid?, limit: Int): List<Playlist>
    /** Counts all playlists for API pagination metadata. */
    suspend fun totalCount(): Long
    /** Attaches a song once and reports whether the required records exist. */
    suspend fun addSong(playlistId: Uuid, songId: Uuid): Boolean
    /** Returns the next membership-cursor page of songs in one playlist. */
    suspend fun songs(playlistId: Uuid, cursor: Long?, limit: Int): List<Pair<Long, Song>>
}

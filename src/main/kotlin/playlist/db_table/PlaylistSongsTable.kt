package com.amit_kundu_io.playlist.db_table

import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.javatime.timestamp

/** Join table mapping songs to playlists; its monotonic id is the page cursor. */
object PlaylistSongsTable : Table("playlist_songs") {
    val id = long("id").autoIncrement()
    val playlistId = uuid("playlist_id")
    val songId = uuid("song_id")
    val addedAt = timestamp("added_at")

    override val primaryKey = PrimaryKey(id)
}

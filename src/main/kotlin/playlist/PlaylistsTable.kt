package com.amit_kundu_io.playlist

import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.javatime.timestamp

/** Exposed mapping for playlist metadata. */
object PlaylistsTable : Table("playlists") {
    val id = uuid("id")
    val name = varchar("name", 255)
    val description = text("description").nullable()
    val artworkUrl = text("artwork_url").nullable()
    val songCount = long("song_count")
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")

    override val primaryKey = PrimaryKey(id)
}

/** Join table mapping songs to playlists; its monotonic id is the page cursor. */
object PlaylistSongsTable : Table("playlist_songs") {
    val id = long("id").autoIncrement()
    val playlistId = uuid("playlist_id")
    val songId = uuid("song_id")
    val addedAt = timestamp("added_at")

    override val primaryKey = PrimaryKey(id)
}

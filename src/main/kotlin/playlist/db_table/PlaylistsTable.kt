package com.amit_kundu_io.playlist.db_table

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


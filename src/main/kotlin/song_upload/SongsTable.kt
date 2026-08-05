package com.amit_kundu_io.song_upload

import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.javatime.date
import org.jetbrains.exposed.v1.javatime.timestamp
import java.util.UUID

/** Exposed mapping for the songs table created by the Flyway song migration. */
object SongsTable : Table("songs") {
    val id = uuid("id")
    val title = varchar("title", 255)
    val artistName = varchar("artist_name", 255)
    val albumName = varchar("album_name", 255).nullable()
    val description = text("description").nullable()
    val genre = varchar("genre", 100).nullable()
    val language = varchar("language", 50).nullable()
    val durationSeconds = integer("duration_seconds").nullable()
    val audioUrl = text("audio_url")
    val artworkUrl = text("artwork_url").nullable()
    val releaseDate = date("release_date").nullable()
    val isExplicit = bool("is_explicit")
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")

    override val primaryKey = PrimaryKey(id)
}

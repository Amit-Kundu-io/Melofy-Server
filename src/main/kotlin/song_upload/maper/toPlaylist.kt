package song_upload.maper

import com.amit_kundu_io.playlist.data.module.Playlist
import com.amit_kundu_io.playlist.db_table.PlaylistsTable
import org.jetbrains.exposed.v1.core.ResultRow

/** Maps a playlist query row to the internal playlist entity. */
 fun ResultRow.toPlaylist() = Playlist(
    id = this[PlaylistsTable.id], name = this[PlaylistsTable.name],
    description = this[PlaylistsTable.description], artworkUrl = this[PlaylistsTable.artworkUrl],
    songCount = this[PlaylistsTable.songCount],
    createdAt = this[PlaylistsTable.createdAt], updatedAt = this[PlaylistsTable.updatedAt],
)
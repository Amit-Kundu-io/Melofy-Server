package song_upload.maper

import com.amit_kundu_io.song_upload.data.models.Song
import com.amit_kundu_io.song_upload.db_table.SongsTable
import org.jetbrains.exposed.v1.core.ResultRow

/** Maps a joined playlist-song query row to the internal song entity. */
 fun ResultRow.toSong() = Song(
    id = this[SongsTable.id],
    title = this[SongsTable.title],
    artistName = this[SongsTable.artistName],
    audioUrl = this[SongsTable.audioUrl],
    albumName = this[SongsTable.albumName],
    description = this[SongsTable.description],
    genre = this[SongsTable.genre],
    language = this[SongsTable.language],
    durationSeconds = this[SongsTable.durationSeconds],
    artworkUrl = this[SongsTable.artworkUrl],
    fileName = this[SongsTable.fileName],
    videoId = this[SongsTable.videoId],
    releaseDate = this[SongsTable.releaseDate],
    isExplicit = this[SongsTable.isExplicit],
    createdAt = this[SongsTable.createdAt],
    updatedAt = this[SongsTable.updatedAt],
)
CREATE TABLE playlists (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    artwork_url TEXT,
    song_count BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT playlists_name_not_blank CHECK (length(btrim(name)) > 0)
);

CREATE TABLE playlist_songs (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    playlist_id UUID NOT NULL REFERENCES playlists(id) ON DELETE CASCADE,
    song_id UUID NOT NULL REFERENCES songs(id) ON DELETE CASCADE,
    added_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT playlist_songs_unique_song_per_playlist UNIQUE (playlist_id, song_id)
);

CREATE INDEX idx_playlist_songs_playlist_cursor ON playlist_songs (playlist_id, id DESC);
CREATE INDEX idx_playlist_songs_song_id ON playlist_songs (song_id);
CREATE INDEX idx_playlists_created_at ON playlists (created_at DESC);

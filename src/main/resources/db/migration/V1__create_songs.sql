CREATE TABLE songs (
    id UUID PRIMARY KEY,
    owner_id VARCHAR(255) NOT NULL,
    title VARCHAR(255) NOT NULL,
    artist_name VARCHAR(255) NOT NULL,
    album_name VARCHAR(255),
    description TEXT,
    genre VARCHAR(100),
    language VARCHAR(50),
    duration_seconds INTEGER,
    audio_url TEXT NOT NULL,
    artwork_url TEXT,
    release_date DATE,
    is_explicit BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT songs_title_not_blank CHECK (length(btrim(title)) > 0),
    CONSTRAINT songs_artist_name_not_blank CHECK (length(btrim(artist_name)) > 0),
    CONSTRAINT songs_duration_positive CHECK (duration_seconds IS NULL OR duration_seconds > 0)
);

CREATE INDEX idx_songs_owner_id ON songs (owner_id);
CREATE INDEX idx_songs_created_at ON songs (created_at DESC);

-- V4__add_file_name_and_video_id_to_songs.sql

ALTER TABLE songs
    ADD COLUMN file_name VARCHAR(255),
    ADD COLUMN video_id VARCHAR(255);

CREATE INDEX idx_songs_video_id ON songs (video_id);
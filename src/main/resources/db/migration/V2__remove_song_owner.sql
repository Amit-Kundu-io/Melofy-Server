
DROP INDEX IF EXISTS idx_songs_owner_id;

ALTER TABLE songs
    DROP COLUMN IF EXISTS owner_id;

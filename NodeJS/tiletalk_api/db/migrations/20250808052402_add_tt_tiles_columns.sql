-- migrate:up
ALTER TABLE tt_tiles
ADD COLUMN flip BOOLEAN DEFAULT false,
ADD COLUMN tile_bg NUMERIC DEFAULT 0,
ADD COLUMN callout TEXT,
ADD COLUMN title TEXT;

-- migrate:down
ALTER TABLE tt_tiles
DROP COLUMN flip,
DROP COLUMN tile_bg,
DROP COLUMN callout,
DROP COLUMN title;
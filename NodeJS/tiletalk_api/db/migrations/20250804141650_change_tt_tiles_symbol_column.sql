-- migrate:up
ALTER TABLE tt_tiles ALTER COLUMN symbol TYPE TEXT;

-- migrate:down
ALTER TABLE tt_tiles ALTER COLUMN symbol TYPE CHAR(1);

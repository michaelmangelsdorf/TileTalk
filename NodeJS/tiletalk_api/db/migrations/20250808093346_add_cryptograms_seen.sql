-- migrate:up
ALTER TABLE tt_cryptograms
ADD COLUMN seen BOOLEAN NOT NULL DEFAULT false;

-- migrate:down
ALTER TABLE tt_cryptograms
DROP COLUMN seen;
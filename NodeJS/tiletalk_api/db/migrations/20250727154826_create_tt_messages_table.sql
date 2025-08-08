
-- migrate:up
create table tt_messages (
  id SERIAL PRIMARY KEY,
  tile_id INTEGER REFERENCES tt_tiles(id) ON DELETE CASCADE,
  responder_id INTEGER REFERENCES tt_users(id) ON DELETE CASCADE,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
  UNIQUE (tile_id, responder_id)
);

-- migrate:down
drop table tt_messages;

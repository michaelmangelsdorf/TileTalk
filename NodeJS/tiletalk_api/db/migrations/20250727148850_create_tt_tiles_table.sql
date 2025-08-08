-- migrate:up
create table tt_tiles (
  id SERIAL PRIMARY KEY,
  owner_id INTEGER REFERENCES tt_users(id) ON DELETE CASCADE,
  x_coord INTEGER NOT NULL,
  y_coord INTEGER NOT NULL,
  starter_id INTEGER REFERENCES tt_users(id) ON DELETE CASCADE,
  symbol CHAR(1),
  animation_type INTEGER DEFAULT 0,
  UNIQUE(owner_id,x_coord,y_coord)
);

-- migrate:down
drop table tt_tiles;

-- migrate:up
create table tt_contacts (
  id SERIAL PRIMARY KEY,
  requester_id INTEGER REFERENCES tt_users(id) ON DELETE CASCADE,
  target_id INTEGER REFERENCES tt_users(id) ON DELETE CASCADE,
  authorized INTEGER DEFAULT 0,
  UNIQUE(requester_id, target_id)
);

-- migrate:down
drop table tt_contacts;

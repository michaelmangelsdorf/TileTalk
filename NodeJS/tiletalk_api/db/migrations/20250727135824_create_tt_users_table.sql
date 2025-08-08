-- migrate:up
create table tt_users (
  id SERIAL PRIMARY KEY,
  username TEXT NOT NULL,
  password_hash TEXT NOT NULL,
  public_key TEXT
);

-- migrate:down
drop table tt_users;

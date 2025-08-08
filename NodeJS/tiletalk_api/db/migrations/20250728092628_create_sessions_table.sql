-- migrate:up
create table sessions (
  sid TEXT PRIMARY KEY,
  sess JSONB,
  expire TIMESTAMP WITHOUT TIME ZONE
);

-- migrate:down
drop table sessions;

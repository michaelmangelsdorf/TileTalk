-- migrate:up
create table tt_cryptograms (
  id SERIAL PRIMARY KEY,
  message_id INTEGER REFERENCES tt_messages(id) ON DELETE CASCADE,
  recipient_id INTEGER REFERENCES tt_users(id) ON DELETE CASCADE,
  payload JSONB, --ivBase64: encryptedAESKeyBase64: --encryptedDataBytesBase64:
  UNIQUE (message_id, recipient_id)
);

-- migrate:down
drop table tt_cryptograms;

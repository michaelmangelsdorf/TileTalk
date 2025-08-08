async function isTileOwnerOrAuthorizedContact(req, ownerId, userId) {
  // Parse both IDs to integers to ensure a correct type-safe comparison.
  const ownerIdInt = parseInt(ownerId, 10);
  const userIdInt = parseInt(userId, 10);

  // A user can access a tile if they are the owner...
  if (userIdInt === ownerIdInt) {
    return true;
  }

  // ...or if they are an authorized contact of the owner.
  const result = await req.db.query(
    `SELECT id FROM tt_contacts
     WHERE
        authorized = 1 AND
        (
            (requester_id = $1 AND target_id = $2) OR
            (requester_id = $2 AND target_id = $1)
        )`,
    [userIdInt, ownerIdInt],
  );

  // If the query returns any rows, it means they are authorized contacts.
  return result.rows.length > 0;
}

export default isTileOwnerOrAuthorizedContact;
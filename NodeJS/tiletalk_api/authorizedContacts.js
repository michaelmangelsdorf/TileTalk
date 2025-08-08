async function authorizedContacts(req, userId) {
  // Returns the list of authorized contacts for a given userID

  return await req.db.query(
    `SELECT
            CASE
                WHEN requester_id = $1 THEN target_id
                WHEN target_id = $1 THEN requester_id
            END AS contact_id
        FROM
            tt_contacts
        WHERE
            (requester_id = $1 OR target_id = $1) AND authorized = 1`,
    [userId],
  );
}

export default authorizedContacts;

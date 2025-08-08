import express from "express";
import { ensureAuthenticatedApi } from "../../auth_middleware.js";
import isTileOwnerOrAuthorizedContact from "../../isTileOwnerOrAuthorizedContact.js";

const router = express.Router();

router.get("/read", ensureAuthenticatedApi, async (req, res) => {
  const loggedInUserId = req.user?.id;
  const { owner_id, x_coord, y_coord } = req.query || {};

  try {
    if (!(await isTileOwnerOrAuthorizedContact(req, owner_id, loggedInUserId))) {
      return res.respond("UNAUTHORIZED_TILE_ACCESS", loggedInUserId);
    }

    // Step 1: Select messages for the user, including the new 'seen' flag.
    const result = await req.db.query(
      `SELECT m.responder_id, c.payload, m.created_at, c.seen
       FROM tt_tiles AS t
       JOIN tt_messages AS m ON t.id = m.tile_id
       JOIN tt_cryptograms AS c ON m.id = c.message_id
       WHERE t.owner_id = $1
         AND t.x_coord = $2
         AND t.y_coord = $3
         AND c.recipient_id = $4
       ORDER BY m.created_at ASC`,
      [owner_id, x_coord, y_coord, loggedInUserId],
    );

    if (result.rows.length === 0) {
      return res.respond("NO_MESSAGES_ON_TILE");
    }

    // Step 2: Mark the retrieved cryptograms as 'seen' for the logged-in user.
    // This is done after fetching so the user receives the current 'seen' status,
    // and subsequent fetches will show them as 'seen'.
    await req.db.query(
        `UPDATE tt_cryptograms
         SET seen = true
         WHERE recipient_id = $1
           AND message_id IN (
             SELECT m.id
             FROM tt_messages m
             JOIN tt_tiles t ON m.tile_id = t.id
             WHERE t.owner_id = $2
               AND t.x_coord = $3
               AND t.y_coord = $4
           )`,
        [loggedInUserId, owner_id, x_coord, y_coord],
    );


    res.respond("LISTING_MESSAGES", result.rows);

  } catch (err) {
    console.error("[GET /messages/read] Error:", err);
    res.respond("INTERNAL_SERVER_ERROR");
  }
});

export default router;
import express from "express";
import { ensureAuthenticatedApi } from "../../auth_middleware.js";
import getTileId from "../../getTileId.js";

const router = express.Router();

router.delete("/delete", ensureAuthenticatedApi, async (req, res) => {
  // Logged-in user is deleting a message from his own grid
  // or a contact's grid. Delete the message row and also
  // the associated cryptograms for all contacts

  const loggedInUserId = req.user?.id;
  const {
    owner_id, // Tile owner
    x_coord,
    y_coord,
  } = req.query || {};

  // Find a matching tile
  const tileIdResult = await getTileId(req, owner_id, x_coord, y_coord);

  if (tileIdResult.rows.length === 0) {
    res.respond("TILE_NOT_FOUND", [owner_id, x_coord, y_coord]);
    return;
  }

  const tile_id = tileIdResult.rows[0].id;

  // We now have a tile_id for deleting the message row
  try {
    // They are trying to delete an existing message, and we
    // consider that since they were authorized to create it,
    // they should be able to delete it

    const deleteResult = await req.db.query(
      `DELETE FROM tt_messages
        WHERE tile_id = $1 AND responder_id = $2`,
      [tile_id, loggedInUserId],
    );
    res.respond("MESSAGE_DELETED", null);
  } catch (err) {
    res.respond("MESSAGE_DELETE_ERROR", null);
  }
});

export default router;
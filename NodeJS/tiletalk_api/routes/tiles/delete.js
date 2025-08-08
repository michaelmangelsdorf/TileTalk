import express from "express";
import { ensureAuthenticatedApi } from "../../auth_middleware.js";

const router = express.Router();

router.delete("/delete", ensureAuthenticatedApi, async (req, res) => {
  // The logged-in user is deleting a tile on some user's grid.
  // Deleting the tile will cascade deletion of all messages and
  // cryptograms linked to it

  const loggedInUserId = req.user?.id;
  const tileId = req.query?.tileId;

  try {
    // The tile can be deleted if the logged-in user is the grid owner,
    // or is the user who has "started"/created the tile.

    const deleteResult = await req.db.query(
      `DELETE FROM tt_tiles
       WHERE id = $1 AND (starter_id = $2 OR owner_id = $2)
       RETURNING id`,
      [tileId, loggedInUserId],
    );
    if (deleteResult.rows.length === 0) {
      res.respond("TILE_NOT_FOUND", null);
    } else {
      res.respond("TILE_DELETED", null);
    }
  } catch (err) {
    res.respond("INTERNAL_SERVER_ERROR");
  }
});

export default router;
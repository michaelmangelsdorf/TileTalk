import express from "express";
import { ensureAuthenticatedApi } from "../../auth_middleware.js";
import isTileOwnerOrAuthorizedContact from "../../isTileOwnerOrAuthorizedContact.js";

const router = express.Router();

router.get("/read", ensureAuthenticatedApi, async (req, res) => {
  // The logged-in user is trying to read the symbol,
  // animation status etc. of a tile on their own grid, or the grid
  // of an authorized user

  const loggedInUserId = req.user?.id;
  const { owner_id, x_coord, y_coord } = req.query || {};

  try {
    if (!(await isTileOwnerOrAuthorizedContact(req, owner_id, loggedInUserId))) {
      return res.respond("UNAUTHORIZED_TILE_ACCESS", loggedInUserId);
    }

    const result = await req.db.query(
      `SELECT * FROM tt_tiles
            WHERE owner_id = $1
            AND x_coord = $2
            AND y_coord = $3`,
      [owner_id, x_coord, y_coord],
    );

    if (result.rows.length === 0) {
      // Corrected: Send null instead of an array for the data payload.
      res.respond("TILE_NOT_FOUND", null);
    } else {
      const tile = result.rows[0];
      res.respond("RETURNING_TILE_DATA", tile);
    }
  } catch (err) {
    console.error("[GET /tile/read] Error:", err);
    res.respond("INTERNAL_SERVER_ERROR");
  }
});

export default router;
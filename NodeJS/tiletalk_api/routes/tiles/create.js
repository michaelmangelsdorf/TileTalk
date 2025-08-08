
import express from "express";
import { ensureAuthenticatedApi } from "../../auth_middleware.js";
import isTileOwnerOrAuthorizedContact from "../../isTileOwnerOrAuthorizedContact.js";

const router = express.Router();

router.post("/create", ensureAuthenticatedApi, async (req, res) => {
  const loggedInUserId = req.user?.id;
  try {
    const {
      owner_id,
      x_coord,
      y_coord,
      symbol,
      animation_type,
      flip,
      tile_bg,
      callout,
      title,
    } = req.body || {};

    if (!(await isTileOwnerOrAuthorizedContact(req, owner_id, loggedInUserId))) {
      return res.respond("UNAUTHORIZED_TILE_ACCESS", loggedInUserId);
    }

    if (x_coord > 3 || y_coord > 3 || x_coord < 0 || y_coord < 0) {
      return res.respond("TILE_COORDS_OUT_OF_BOUNDS", [x_coord, y_coord]);
    }

    const insertResult = await req.db.query(
      `INSERT INTO tt_tiles (owner_id, x_coord, y_coord,
                starter_id, symbol, animation_type, flip, tile_bg, callout, title)
                VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10) RETURNING id`,
      [owner_id, x_coord, y_coord, loggedInUserId, symbol, animation_type, flip, tile_bg, callout, title],
    );

    res.respond("TILE_CREATED", insertResult.rows[0].id);
  } catch (err) {
    console.error("[POST /tile/create] Error:", err);
    res.respond("INTERNAL_SERVER_ERROR");
  }
});

export default router;
import express from "express";
import { ensureAuthenticatedApi } from "../../auth_middleware.js";
import isTileOwnerOrAuthorizedContact from "../../isTileOwnerOrAuthorizedContact.js";
import authorizedContacts from "../../authorizedContacts.js";
import { connectedUsers } from "../../tiletalk_api.js";

const router = express.Router();

router.post("/update", ensureAuthenticatedApi, async (req, res) => {
  const loggedInUserId = req.user?.id;
  let {
    id,
    symbol,
    animation_type,
    flip,
    tile_bg,
    callout,
    title,
  } = req.body || {};

  if (!id) {
    return res.status(400).json({ message: "Tile ID is required for an update." });
  }

  // Sanitize and truncate title and callout
  if (title && title.length > 30) {
    title = title.substring(0, 30);
  }
  if (callout && callout.length > 30) {
    callout = callout.substring(0, 30);
  }

  try {
    const tileResult = await req.db.query(
      `SELECT * FROM tt_tiles WHERE id = $1`,
      [id]
    );

    if (tileResult.rows.length === 0) {
      return res.respond("TILE_NOT_FOUND", { tileId: id });
    }

    const tile = tileResult.rows[0];
    const isOwner = tile.owner_id === loggedInUserId;
    const isStarter = tile.starter_id === loggedInUserId;

    const canUpdateMainFields = isOwner || isStarter;

    const updates = {};
    if (canUpdateMainFields) {
        if (symbol !== undefined) updates.symbol = symbol;
        if (animation_type !== undefined) updates.animation_type = animation_type;
        if (flip !== undefined) updates.flip = flip;
        if (tile_bg !== undefined) updates.tile_bg = tile_bg;
        if (callout !== undefined) updates.callout = callout;
        if (title !== undefined) updates.title = title;
    }

    const fieldsToUpdate = Object.keys(updates);
    if (fieldsToUpdate.length === 0) {
        return res.status(400).json({ message: "No fields to update provided or insufficient permissions." });
    }

    const setClause = fieldsToUpdate.map((field, index) => `"${field}" = $${index + 1}`).join(", ");
    const values = fieldsToUpdate.map(field => updates[field]);

    const finalQuery = `UPDATE tt_tiles SET ${setClause} WHERE id = $${fieldsToUpdate.length + 1} RETURNING *`;
    values.push(id);

    const updateResult = await req.db.query(finalQuery, values);
    const updatedTile = updateResult.rows[0];

    // --- NOTIFICATION LOGIC ---
    const contactsResult = await authorizedContacts(req, updatedTile.owner_id);
    const contactIds = contactsResult.rows.map(row => row.contact_id);
    contactIds.push(parseInt(updatedTile.owner_id));

    const notification = JSON.stringify({
        type: 'tile_update',
        tileOwnerId: updatedTile.owner_id,
        x: updatedTile.x_coord,
        y: updatedTile.y_coord,
        updatedBy: loggedInUserId
    });

    contactIds.forEach(contactId => {
        const ws = connectedUsers.get(contactId);
        if (ws && ws.readyState === ws.OPEN) {
            ws.send(notification);
        }
    });

    res.respond("TILE_UPDATED", updatedTile);

  } catch (err) {
    console.error("[POST /tile/update] Error:", err);
    res.respond("INTERNAL_SERVER_ERROR");
  }
});

export default router;
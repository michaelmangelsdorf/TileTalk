import express from "express";
import { ensureAuthenticatedApi } from "../../auth_middleware.js";
import isTileOwnerOrAuthorizedContact from "../../isTileOwnerOrAuthorizedContact.js";
import getTileId from "../../getTileId.js";
import { connectedUsers } from "../../tiletalk_api.js";
import authorizedContacts from "../../authorizedContacts.js";

const router = express.Router();

router.post("/create", ensureAuthenticatedApi, async (req, res) => {
  // Insert a message (set of cryptograms, one for each contact)
  // for a tile on a contact's grid

  const loggedInUserId = req.user?.id;

  try {
    const {
      owner_id, // Tile owner
      x_coord,
      y_coord,
      // responder_id = loggedInUser,
      message_set, //Array of recipient_id, payload object
    } = req.body || {};

    // Corrected: Added 'await' to ensure the async authorization check completes.
    if (!(await isTileOwnerOrAuthorizedContact(req, owner_id, loggedInUserId))) {
      res.respond("UNAUTHORIZED_TILE_ACCESS", loggedInUserId);
      return;
    }

    // Authorized to create a message on this grid, but
    // tile must already exist in order to create a message

    const tileIdResult = await getTileId(req, owner_id, x_coord, y_coord);

    if (tileIdResult.rows.length === 0) {
      // Corrected "NO_TILE_FOR_MESSAGE" to "TILE_NOT_FOUND" to use a valid response message.
      res.respond("TILE_NOT_FOUND", [owner_id, x_coord, y_coord]);
      return;
    }

    const tile_id = tileIdResult.rows[0].id;

    // We now have a tile_id and need to create a message row

    // Unique constraint: (tile_id, responder_id)
    // so this will fail if the logged-in user already created
    // a previous message for the tile (delete to update)

    let result = await req.db.query(
      `INSERT INTO tt_messages (tile_id, responder_id)
                VALUES ($1, $2) RETURNING id`,
      [tile_id, loggedInUserId],
    );

    // To do: if (result.rows.length === 0) {
    const message_id = result.rows[0].id;

    // We now have a message_id and need to create cryptogram rows
    // for the message for all the responder's contacts.
    // These have already been prepared by the front-end,
    // so we can just extract them from the request body

    for (const message of message_set) {
      result = await req.db.query(
        `INSERT INTO tt_cryptograms (message_id, recipient_id, payload)
                VALUES ($1, $2, $3) RETURNING id`,
        [message_id, message.recipient_id, message.payload],
      );
    }

    // --- NOTIFICATION LOGIC STARTS HERE ---

    // 1. Get the list of the logged-in user's authorized contacts
    const contactsResult = await authorizedContacts(req, loggedInUserId); //
    const contactIds = contactsResult.rows.map(row => row.contact_id);

    // 2. Also notify the owner of the tile grid
    contactIds.push(parseInt(owner_id));

    // 3. Construct the notification message
    const notification = JSON.stringify({
        type: 'new_message',
        tileOwnerId: owner_id,
        x: x_coord,
        y: y_coord,
        authorId: loggedInUserId
    });

    // 4. Send notification to each connected contact
    contactIds.forEach(contactId => {
        if (connectedUsers.has(contactId)) {
            const ws = connectedUsers.get(contactId);
            if (ws.readyState === ws.OPEN) {
                ws.send(notification);
            }
        }
    });

    // --- NOTIFICATION LOGIC ENDS HERE ---
    
    res.respond("MESSAGE_CREATED", result.rows[0].id);

  } catch (err) {
    // Corrected: Added detailed error logging to expose the root cause of failures.
    console.error("[POST /message/create] Error:", err);
    res.respond("INTERNAL_SERVER_ERROR", null);
  }
});

export default router;
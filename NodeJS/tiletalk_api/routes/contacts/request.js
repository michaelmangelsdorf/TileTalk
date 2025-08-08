import "dotenv/config";
import express from "express";
import { ensureAuthenticatedApi } from "../../auth_middleware.js";
import authorizedContacts from "../../authorizedContacts.js";

const router = express.Router();
const maxContacts = process.env.MAX_CONTACTS;

router.post("/request", ensureAuthenticatedApi, async (req, res) => {
  // Logged-in user is requesting to be authorized as a contact
  // of targetUserId

  const requesterId = req.user?.id;
  const { targetUserId: targetId } = req.body;

  if (!targetId) {
    return res.respond("CONTACT_REQUEST_ERROR", "targetUserId is missing from the request body.");
  }

  try {
    const numberOfContacts = (await authorizedContacts(req, requesterId)).rows
      .length;
    if (numberOfContacts >= maxContacts) {
      res.respond("MAX_NUMBER_OF_CONTACTS_REACHED", requesterId);
      return;
    }

    // Check if the contact request already exists
    const existingRequest = await req.db.query(
      `SELECT id FROM tt_contacts
       WHERE (requester_id = $1 AND target_id = $2)
          OR (requester_id = $2 AND target_id = $1)`,
      [requesterId, targetId]
    );

    if (existingRequest.rows.length > 0) {
      return res.respond("CONTACT_REQUEST_ALREADY_EXISTS");
    }

    const result = await req.db.query(
      `INSERT INTO tt_contacts (requester_id, target_id, authorized)
            VALUES ($1,$2,0) RETURNING id`,
      [requesterId, targetId],
    );

    if (result.rows.length === 0) {
      res.respond("CONTACT_REQUEST_ERROR", null);
    } else {
      res.respond("CONTACT_REQUESTED", null);
    }
  } catch (err) {
    console.error("Error creating contact request:", err); // Log the actual error
    res.respond("CONTACT_REQUEST_ERROR", err.message); // Send a more descriptive error message
  }

  return;
});

export default router;
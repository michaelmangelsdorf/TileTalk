import express from "express";
import { ensureAuthenticatedApi } from "../../auth_middleware.js";
import authorizedContacts from "../../authorizedContacts.js";

const router = express.Router();
const maxContacts = process.env.MAX_CONTACTS;

router.post("/accept", ensureAuthenticatedApi, async (req, res) => {
  // The user with acceptedUserId (requester) has made a contact request for
  // the logged-in user (target).
  // This route allows the logged-in user to "accept" this contact request
  // by setting the rows authorized flag to true/1.

  const loggedInUserId = req.user?.id;
  const acceptedUserId = req.body?.acceptedUserId;

  try {
    // Await the asynchronous function call here
    const numberOfContacts = (await authorizedContacts(req, loggedInUserId)).rows.length;
    if (numberOfContacts >= maxContacts) {
      res.respond("MAX_NUMBER_OF_CONTACTS_REACHED", loggedInUserId);
      return;
    }

    const result = await req.db.query(
      `UPDATE tt_contacts SET
            authorized = 1
            WHERE target_id = $1 and requester_id = $2 RETURNING id`,
      [loggedInUserId, acceptedUserId],
    );

    res.respond(
      result.rows.length === 0 ? "REQUESTER_NOT_FOUND" : "CONTACT_REQUEST_ACCEPTED", // Corrected message ID
      null,
    );
  } catch (err) {
    res.respond("CONTACT_REQUEST_FAILED", null);
  }
});

export default router;
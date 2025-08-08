import express from "express";
import { ensureAuthenticatedApi } from "../../auth_middleware.js";
import authorizedContacts from "../../authorizedContacts.js";

const router = express.Router();

router.get("/list", ensureAuthenticatedApi, async (req, res) => {
  // Returns user_ids for authorized contacts, pending contact requests,
  // and incoming contact requests for the logged-in user

  const userId = req.user?.id;

  try {
    // 1. Get Authorized Contacts - Await the asynchronous function call
    const authorizedResult = await authorizedContacts(req, userId);
    const contacts = authorizedResult.rows.map((row) => row.contact_id);

    // 2. Get Pending (User is requester, authorized=0)
    const pendingResult = await req.db.query(
      `SELECT
            target_id AS contact_id
        FROM
            tt_contacts
        WHERE
            requester_id = $1 AND authorized = 0`,
      [userId],
    );
    const pending = pendingResult.rows.map((row) => row.contact_id);

    // 3. Get Incoming (User is target, authorized=0)
    const incomingResult = await req.db.query(
      `SELECT
            requester_id AS contact_id
        FROM
            tt_contacts
        WHERE
            target_id = $1 AND authorized = 0`,
      [userId],
    );
    const incoming = incomingResult.rows.map((row) => row.contact_id);

    res.respond("CONTACTS_LIST_SUCCESS", {
      userId: userId,
      contacts: contacts,
      pending: pending,
      incoming: incoming,
    });
  } catch (err) {
    res.respond("CONTACTS_LIST_ERROR", err);
  }
});

export default router;
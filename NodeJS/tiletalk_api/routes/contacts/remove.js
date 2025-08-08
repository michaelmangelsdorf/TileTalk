import express from "express";
import { ensureAuthenticatedApi } from "../../auth_middleware.js";

const router = express.Router();

router.post("/remove", ensureAuthenticatedApi, async (req, res) => {
  // Logged-in user wants to remove themselves from a contact
  // pair with removableUserId, so that they seize to be authorised
  // or pending contacts of each other.

  const loggedInUserId = req.user?.id;
  const removableUserId = req.query.removableUserId;

  try {
    await req.db.query(
      `DELETE FROM tt_contacts
            WHERE (target_id = $1 and requester_id = $2)
            or (target_id = $2 and requester_id = $1)`,
      [loggedInUserId, removableUserId],
    );
    res.respond("CONTACT_REMOVED", null);
  } catch (err) {
    res.respond("CONTACT_NOT_REMOVED", null);
  }
  return;
});

export default router;
import express from "express";
import { ensureAuthenticatedApi } from "../../auth_middleware.js";

const router = express.Router();

router.put("/put", ensureAuthenticatedApi, async (req, res) => {
  const userId = req.query.userId;
  const publicKey = req.body.publicKey;

  try {
    const result = await req.db.query(
      `UPDATE tt_users
         SET public_key = $1
         WHERE id = $2 RETURNING id`,
      [publicKey, userId],
    );

    if (result.rows.length === 0) {
      res.respond("USER_NOT_FOUND", userId);
    } else {
      res.respond("USER_PROFILE_UPDATED");
    }
  } catch (err) {
    res.respond("INTERNAL_SERVER_ERROR", null);
  }
  return;
});

export default router;
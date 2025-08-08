import express from "express";
import { ensureAuthenticatedApi } from "../../auth_middleware.js";

const router = express.Router();

router.get("/get", ensureAuthenticatedApi, async (req, res) => {
  // If userId is explicitly 0, it's a session validation call.
  // We should return the profile of the currently authenticated user.
  if (req.query.userId && parseInt(req.query.userId, 10) === 0) {
    // req.user is populated by passport.js from the session
    const userProfile = {
      id: req.user.id,
      username: req.user.username,
      public_key: req.user.public_key, // Make sure public_key is on req.user
    };
    return res.respond("USER_PROFILE_FOUND", userProfile);
  }

  const userId = req.query.userId;
  const username = req.query.username;

  try {
    let result;
    if (userId) {
      result = await req.db.query(
        "SELECT id, username, public_key FROM tt_users WHERE id = $1",
        [userId],
      );
    } else if (username) {
      result = await req.db.query(
        "SELECT id, username, public_key FROM tt_users WHERE username = $1",
        [username],
      );
    } else {
      // If no userId or username is provided (and it's not the userId=0 case),
      // then it's a bad request.
      return res.status(400).json({ message: "User ID or username is required." });
    }

    if (result.rows.length === 0) {
      res.respond("USER_NOT_FOUND", userId || username);
      return;
    }

    const userProfile = result.rows[0];
    res.respond("USER_PROFILE_FOUND", userProfile);
  } catch (err) {
    console.error("[GET /user/get] Error:", err);
    res.respond("INTERNAL_SERVER_ERROR", null);
  }
});

export default router;
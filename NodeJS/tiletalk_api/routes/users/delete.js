import express from "express";
import { ensureAuthenticatedApi } from "../../auth_middleware.js";
import outcome from "../../routeResponseMiddleware.js";

const router = express.Router();

router.delete("/delete", ensureAuthenticatedApi, async (req, res) => {
  // The logged-in user is deleting their own user record.

  const userId = req.user?.id;

  try {
    // Delete the user from the database
    const deleteResult = await req.db.query(
      "DELETE FROM tt_users WHERE id = $1 RETURNING id",
      [userId],
    );

    if (deleteResult.rows.length === 0) {
      res.respond("USER_NOT_FOUND", userId);
      return;
    }

    req.logOut((err) => {
        if (err) {
            console.error("Error during logout:", err);
            return res.respond("INTERNAL_SERVER_ERROR");
        }
        req.session.destroy((err) => {
            if (err) {
                console.error("Error destroying session:", err);
                return res.respond("INTERNAL_SERVER_ERROR");
            }
            res.respond("USER_DELETED_SUCCESSFULLY");
        });
    });

  } catch (err) {
    res.respond("INTERNAL_SERVER_ERROR");
  }
});

export default router;
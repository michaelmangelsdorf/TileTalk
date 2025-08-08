import express from "express";

const router = express.Router();

router.get("/logout", (req, res, next) => {
  if (req.isAuthenticated()) {
    req.logout((err) => {
      if (err) {
        return next(err);
      }

      req.session.destroy((err) => {
          if (err) {
              console.error("Error destroying session:", err);
              return res.respond("INTERNAL_SERVER_ERROR");
          }
          res.respond("LOGGED_OUT");
      });
    });
  } else {
    res.respond("NOT_LOGGED_IN");
  }
});

export default router;
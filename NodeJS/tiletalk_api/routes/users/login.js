import express from "express";
import passport from "passport";

const router = express.Router();

router.post("/login", (req, res, next) => {
  passport.authenticate("local", (err, user, info) => {
    if (err) {
      return next(err);
    }
    if (!user) {
      return res.respond("NOT_LOGGED_IN", info.message || "Authentication failed.");
    }

    req.logIn(user, (loginErr) => {
      if (loginErr) {
        return next(loginErr);
      }
      
      req.session.save((saveErr) => {
        if (saveErr) {
          return next(saveErr);
        }

        const loginResponse = {
          userId: user.id,
          userName: user.username,
        };

        res.respond("LOGGED_IN", loginResponse);
      });
    });
  })(req, res, next);
});

export default router;
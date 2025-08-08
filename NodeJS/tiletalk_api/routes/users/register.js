import express from "express";
import bcrypt from "bcrypt";
import { body, validationResult } from "express-validator";

const router = express.Router();

const handleValidationErrors = (req, res, next) => {
  const errors = validationResult(req);
  if (!errors.isEmpty()) {
    return res.status(400).json({
      message: errors.array()[0].msg,
    });
  }
  next();
};

router.post(
  "/register",

  // Validation chain for userName
  body("userName")
    .trim()
    .isLength({ min: 3, max: 20 })
    .withMessage("Username must be between 3 and 20 characters.")
    .isAlphanumeric()
    .withMessage("Username must contain only letters and numbers."),

  // Validation chain for password
  body("password")
    .trim()
    .isLength({ min: 6 })
    .withMessage("Password must be at least 6 characters long."),

  handleValidationErrors, // Apply validation error handling

  async (req, res) => {
    const { userName, password, publicKey } = req.body || {}; // Added publicKey

    try {
      // Check if username already exists
      const checkResult = await req.db.query(
        "SELECT id FROM tt_users WHERE username = $1",
        [userName],
      );

      if (checkResult.rows.length > 0) {
        console.warn(
          `[POST /users/register] Username '${userName}' ` +
            `already registered.`,
        );
        return res.status(409).json({
          message: "Username already registered!",
        });
      }

      // Hash the password
      const saltRounds = parseInt(process.env.BCRYPT_SALT_ROUNDS);
      const hashedPassword = await bcrypt.hash(password, saltRounds);

      // Insert new user into the database
      const insertResult = await req.db.query(
        `INSERT INTO tt_users (username, password_hash, public_key)
                VALUES ($1, $2, $3) RETURNING id`, // Added public_key
        [userName, hashedPassword, publicKey], // Added publicKey
      );

      const newUserId = insertResult.rows[0].id;
      const newUser = { id: newUserId, username: userName, publicKey: publicKey };

      // Note: We don't automatically log in here.
      // The client will perform a separate login request after registration
      // to establish a session correctly.

      res.respond("REGISTRATION_SUCCESSFUL", newUser);
    } catch (err) {
      res.respond("INTERNAL_SERVER_ERROR", null);
    }
  },
);

export default router;
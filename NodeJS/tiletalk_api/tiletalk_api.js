import express from "express";
import { WebSocketServer } from "ws";
import { Pool } from "pg";
import bcrypt from "bcrypt";
import passport from "passport";
import { Strategy as LocalStrategy } from "passport-local";
import session from "express-session";
import connectPg from "connect-pg-simple";
import helmet from "helmet";
import cors from "cors";
import "dotenv/config";
import routeResponseMiddleware from "./routeResponseMiddleware.js";
import http from "http";

const app = express();

const apiPort = process.env.HTTP_PORT;
const pgUser = process.env.PG_USER;
const pgHost = process.env.PG_HOST;
const pgDBName = process.env.PG_DATABASE;
const pgPassword = process.env.PG_PASSWORD;
const pgPort = process.env.PG_PORT;
const sessionSecret = process.env.SESSION_SECRET;
const saltRounds = parseInt(process.env.BCRYPT_SALT_ROUNDS);

app.use(express.json());

app.use(express.urlencoded({ extended: true }));
app.use(cors());
app.use(
  helmet({
    contentSecurityPolicy: {
      useDefaults: true,
      directives: {
        "default-src": ["'self'"],
        "script-src": ["'self'"],
        "style-src": ["'self'"],
        "img-src": ["'self'", "srvde.swirlsea.org", "data:"],
        "connect-src": ["'self'", `http://localhost:${apiPort}`],
      },
    },
  }),
);

const db = new Pool({
  user: pgUser,
  host: pgHost,
  database: pgDBName,
  password: pgPassword,
  port: pgPort,
});

app.use((req, res, next) => {
  req.db = db;
  req.saltRounds = saltRounds;
  next();
});

const pgSession = connectPg(session);

const sessionMiddleware = session({
    store: new pgSession({
      pool: db,
      tableName: "sessions",
    }),
    secret: sessionSecret,
    resave: false,
    saveUninitialized: false,
    cookie: {
      maxAge: 1000 * 60 * 60 * 3,
      secure: process.env.NODE_ENV === "production",
      httpOnly: true,
    },
});

app.use(sessionMiddleware);

app.use(passport.initialize());
app.use(passport.session());

passport.use(
  new LocalStrategy(
    {
      usernameField: "userName",
    },
    async function verify(userName, password, cb) {
      try {
        const result = await db.query(
          "SELECT * FROM tt_users WHERE username = $1",
          [userName],
        );

        if (result.rows.length > 0) {
          const user = result.rows[0];
          const dbHash = user.password_hash;

          bcrypt.compare(password, dbHash, (err, valid) => {
            if (err) {
              console.error("[Passport Local Strategy] bcrypt error:", err);
              return cb(err);
            } else {
              if (valid) {
                return cb(null, user);
              } else {
                return cb(null, false, { message: "Incorrect password" });
              }
            }
          });
        } else {
          return cb(null, false, { message: "Unknown username" });
        }
      } catch (err) {
        console.error("[Passport Local Strategy] Database query error:", err);
        return cb(err);
      }
    },
  ),
);

passport.serializeUser((user, done) => {
  console.log(`[Passport] Serializing user ID: ${user.id}`);
  done(null, user.id);
});

passport.deserializeUser(async (id, done) => {
  try {
    console.log(`[Passport] Deserializing user ID: ${id}`);
    const result = await db.query(
      "SELECT id, username FROM tt_users WHERE id = $1",
      [id],
    );
    if (result.rows.length > 0) {
      done(null, result.rows[0]);
    } else {
      console.warn(
        `[Passport] User with ID ${id} not found during deserialization`,
      );
      done(null, false);
    }
  } catch (err) {
    console.error(`[Passport] Error deserializing user ID ${id}:`, err);
    done(err);
  }
});

app.use(routeResponseMiddleware);

import ttApiRouter from "./api_router.js";
app.use("/", ttApiRouter);

app.use((err, req, res, next) => {
  console.error("[TileTalk API Global Error Handler]", err.stack);
  res.status(500).json({
    message: "Internal server error",
    error: err.message,
  });
});

const connectedUsers = new Map();

db.connect()
  .then(() => {
    console.log("Postgres DB connected");

    const server = app.listen(apiPort, () => {
        console.log(`TileTalk Messaging API Server listening on port ${apiPort}`);
        // Send the 'ready' signal to PM2 after the server is listening.
        if (process.send) {
          process.send('ready');
          console.log("Sent 'ready' signal to PM2.");
        }
    });

   const wss = new WebSocketServer({ noServer: true });

   server.on("upgrade", (request, socket, head) => {
     const res = new http.ServerResponse(request);
    
       res.writeHead = (statusCode, headers) => {
           if (statusCode === 401) {
               socket.destroy();
           }
       };
      
      // sessionMiddleware(request, res, () => {
      //   if (!request.session.passport || !request.session.passport.user) {
      //     socket.destroy();
      //     return;
      //   }
      //})
      
         wss.handleUpgrade(request, socket, head, (ws) => {
           wss.emit("connection", ws, request);
         });

  //  wss.on('connection', (ws, request) => {
  //     const userId = request.session.passport.user;
  //     connectedUsers.set(userId, ws);
  //     console.log(`WebSocket connection established for user ${userId}`);

  //     ws.on('close', () => {
  //       connectedUsers.delete(userId);
  //       console.log(`WebSocket connection closed for user ${userId}`);
  //     });
  //   });


   });
  })
  .catch(err => {
    console.error("Fatal: Could not connect to Postgres DB.", err);
    process.exit(1);
  });

export { connectedUsers };

// pm2 start tiletalk_api.js --name tiletalk_api --wait-ready
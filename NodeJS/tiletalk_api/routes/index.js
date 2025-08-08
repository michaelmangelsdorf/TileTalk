import express from "express";

// Import individual route modules

import usersRouterRegister from "./users/register.js";
import usersRouterLogin from "./users/login.js";
import usersRouterProfileGet from "./users/profile_get.js";
import usersRouterProfilePut from "./users/profile_put.js";
import usersRouterLogout from "./users/logout.js";
import usersRouterDelete from "./users/delete.js";

import contactsRouterList from "./contacts/list.js";
import contactsRouterRequest from "./contacts/request.js";
import contactsRouterAccept from "./contacts/accept.js";
import contactsRouterRemove from "./contacts/remove.js";

import tilesRouterCreate from "./tiles/create.js";
import tilesRouterRead from "./tiles/read.js";
import tilesRouterUpdate from "./tiles/update.js";
import tilesRouterDelete from "./tiles/delete.js";

import messagesRouterCreate from "./messages/create.js";
import messagesRouterRead from "./messages/read.js";
import messagesRouterDelete from "./messages/delete.js";

// Mount individual routers

const router = express.Router();

router.use("/user", usersRouterRegister);
router.use("/user", usersRouterLogin);
router.use("/user", usersRouterProfileGet);
router.use("/user", usersRouterProfilePut);
router.use("/user", usersRouterLogout);
router.use("/user", usersRouterDelete);

router.use("/contacts", contactsRouterList);
router.use("/contact", contactsRouterRequest);
router.use("/contact", contactsRouterAccept);
router.use("/contact", contactsRouterRemove);

router.use("/tile", tilesRouterCreate);
router.use("/tile", tilesRouterRead);
router.use("/tile", tilesRouterUpdate);
router.use("/tile", tilesRouterDelete);

router.use("/message", messagesRouterCreate);
router.use("/messages", messagesRouterRead);
router.use("/message", messagesRouterDelete);

export default router;

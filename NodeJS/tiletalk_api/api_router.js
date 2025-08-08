import express from "express";
import apiRoutes from "./routes/index.js";

const router = express.Router();

router.use("/", apiRoutes);

export default router;

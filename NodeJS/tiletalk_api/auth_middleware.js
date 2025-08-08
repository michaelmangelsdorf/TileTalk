function ensureAuthenticatedApi(req, res, next) {
  console.log(
    `[Auth Middleware] Checking authentication for path: ${req.path}`,
  );
  console.log(`[Auth Middleware] Full URL: ${req.originalUrl}`);

  if (req.isAuthenticated()) {
    console.log(
      `[Auth Middleware] User authenticated: ${req.user.username || req.user.id}`,
    );
    return next();
  }

  console.warn(`[Auth Middleware] Unauthorized access attempt to ${req.path}`);
  res.status(401).json({ message: "Unauthorized: Please log in." });
}

export { ensureAuthenticatedApi };

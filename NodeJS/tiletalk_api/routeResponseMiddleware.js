const messages = {
  // CONTACTS //

  CONTACT_REQUESTOR_NOT_FOUND: {
    status: 404,
    log: "Contact requestor not found",
    json: (data) => ({
      success: false,
      message: "Contact requestor not found",
      data: data,
    }),
  },

  CONTACT_REQUEST_ACCEPTED: {
    status: 200,
    log: "Contact request accepted",
    json: (data) => ({
      success: true,
      message: "Contact request accepted",
      data: data,
    }),
  },

  CONTACT_REQUEST_FAILED: {
    status: 500,
    log: "Unspecified error",
    json: (data) => ({
      success: false,
      message: "Unspecified error",
      data: data,
    }),
  },

  CONTACTS_LIST_SUCCESS: {
    status: 200,
    log: "Contact list created successfully",
    json: (data) => ({
      success: true,
      message: "Contact list created successfully",
      data: data,
    }),
  },

  CONTACTS_LIST_ERROR: {
    status: 500,
    log: "Error creating contact list",
    json: (data) => ({
      success: false,
      message: data,
    }),
  },

  CONTACT_REMOVED: {
    status: 200,
    log: "Contact pair removed",
    json: (data) => ({
      success: true,
      message: "Contact pair removed",
      data: data,
    }),
  },

  CONTACT_NOT_REMOVED: {
    status: 500,
    log: "Error removing contact pair",
    json: (data) => ({
      success: false,
      message: "Error removing contact pair",
      data: data,
    }),
  },

  MAX_NUMBER_OF_CONTACTS_REACHED: {
    status: 500,
    log: "Max number of contacts reached",
    json: (data) => ({
      success: false,
      message: "Max number of contacts reached",
      data: data,
    }),
  },

  CONTACT_REQUEST_ERROR: {
    status: 500,
    log: "Error while creating contact request",
    json: (data) => ({
      success: false,
      message: "Error while creating contact request",
      data: data,
    }),
  },

  CONTACT_REQUESTED: {
    status: 200,
    log: "Created contact request",
    json: (data) => ({
      success: true,
      message: "Created contact request",
      data: data,
    }),
  },

  CONTACT_REQUEST_ALREADY_EXISTS: {
    status: 409,
    log: "Contact request already exists",
    json: (data) => ({
      success: false,
      message: "Contact request already exists",
      data: data,
    }),
  },

  // MESSAGES //
  MESSAGE_CREATED: {
    status: 200,
    log: "Message created successfully",
    json: (data) => ({
      success: true,
      message: "Message created successfully",
      data: data
    })
  },

  CREATE_MESSAGE_NOT_AUTHORIZED: {
    status: 500,
    log: "Can't create message on tile - not owner or authorized contact",
    json: (data) => ({
      success: false,
      message: "Can't create message on tile - not owner or authorized contact",
      data: data,
    }),
  },

  MESSAGE_DELETED: {
    status: 200,
    log: "Message deleted from tile",
    json: (data) => ({
      success: true,
      message: "Message deleted from tile",
      data: data,
    }),
  },

  MESSAGE_DELETE_ERROR: {
    status: 500,
    log: "Message could not be deleted",
    json: (data) => ({
      success: false,
      message: "Message could not be deleted",
      data: data,
    }),
  },

  NO_MESSAGES_ON_TILE: {
    status: 200,
    log: "No messages found on tile",
    json: (data) => ({
      success: false,
      message: "No messages found on tile",
      data: data,
    }),
  },

  LISTING_MESSAGES: {
    status: 200,
    log: "Listing messages for tile",
    json: (data) => ({
      success: true,
      message: "Listing messages for tile",
      data: data,
    }),
  },

  UNAUTHORIZED_TILE_ACCESS: {
    status: 403,
    log: "Tile access not authorized",
    json: (data) => ({
      success: false,
      message: "Tile access not authorized",
      data: null, // Corrected: Send null instead of the user ID
    }),
  },

  TILE_COORDS_OUT_OF_BOUNDS: {
    status: 500,
    log: "Tile coordinate out of bounds",
    json: (data) => ({
      success: false,
      message: "Tile coordinate out of bounds",
      data: data,
    }),
  },

  TILE_CREATED: {
    status: 200,
    log: "Tile created successfully",
    json: (data) => ({
      success: true,
      message: "Tile created successfully",
      data: data,
    }),
  },

  TILE_DELETED: {
    status: 200,
    log: "Tile deleted successfully",
    json: (data) => ({
      success: true,
      message: "Tile deleted successfully",
      data: data,
    }),
  },

  TILE_UPDATED: {
    status: 200,
    log: "Tile updated successfully",
    json: (data) => ({
      success: true,
      message: "Tile updated successfully",
      data: data,
    }),
  },

  RETURNING_TILE_DATA: {
    status: 200,
    log: "Returning tile data",
    json: (data) => ({
      success: true,
      message: "Returning tile data",
      data: data,
    }),
  },

  TILE_NOT_FOUND: {
    status: 200,
    log: "Tile not found - unpopulated tile",
    json: (data) => ({
      success: false,
      message: "Tile not found - unpopulated tile",
      data: data,
    }),
  },

  SESSION_CREATION_FAILED: {
    status: 500,
    log: "Session creation failed",
    json: (data) => ({
      success: false,
      message: "Session creation failed",
    }),
  },

  LOGGED_IN: {
    status: 200,
    log: "Logged in successfully",
    json: (data) => ({
      success: true,
      message: "Logged in successfully",
      data: data,
    }),
  },

  LOGGED_OUT: {
    status: 200,
    log: "Logged out successfully",
    json: (data) => ({
      success: true,
      message: "Logged out successfully",
      data: null,
    }),
  },

  NOT_LOGGED_IN: {
    status: 500,
    log: "Not logged in",
    json: (data) => ({
      success: false,
      message: "Not logged in",
    }),
  },

  USER_NOT_FOUND: {
    status: 404,
    log: "UserId not found",
    json: (data) => ({
      success: false,
      message: "UserId not found",
      data: data,
    }),
  },

  USER_PROFILE_FOUND: {
    status: 200,
    log: "User profile found",
    json: (data) => ({
      success: true,
      message: "User profile found",
      data: data,
    }),
  },

  USER_PROFILE_UPDATED: {
    status: 200,
    log: "User profile updated successfully",
    json: () => ({
      success: true,
      message: "User profile updated successfully",
      data: null,
    }),
  },

  REGISTRATION_SUCCESSFUL: {
    status: 200,
    log: "Registration successful",
    json: (data) => ({
      success: true,
      message: "Registration successful",
      data: data,
    }),
  },

  INTERNAL_SERVER_ERROR: {
    status: 500,
    log: "An internal server error occurred.",
    json: {
      success: false,
      error: "An unexpected error occurred on the server.",
    },
  },
};

function outcome(req, res, msg_id, data = null) {
  const message = messages[msg_id];

  if (!message) {
    // Fallback for unknown message ID
    const errorMsg = `outcome(): Unknown message ID '${msg_id}'`;
    console.error(errorMsg);
    return res.status(500).json({
      success: false,
      error: "Internal Server Error",
    });
  }

  const logPrefix = `[${req.method} ${req.route ? req.route.path : req.originalUrl}]`;

  console.log(`${logPrefix} ${message.log}`);

  const jsonPayload =
    typeof message.json === "function" ? message.json(data) : message.json;

  res.status(message.status).json(jsonPayload);
}

function routeResponseMiddleware(req, res, next) {
  res.respond = (msg_id, data = null) => {
    outcome(req, res, msg_id, data);
  };

  next();
}

export default routeResponseMiddleware;
async function getTileId(req, owner_id, x_coord, y_coord) {
  return await req.db.query(
    `SELECT id FROM tt_tiles
            WHERE owner_id = $1
            AND x_coord = $2
            AND y_coord = $3`,
    [owner_id, x_coord, y_coord],
  );
}

export default getTileId;
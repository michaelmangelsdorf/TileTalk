package org.swirlsea.tiletalk.grid.ui.partials

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.swirlsea.tiletalk.grid.GridUiState
import org.swirlsea.tiletalk.grid.TileUiState

@Composable
fun TileGridView(
    gridState: GridUiState,
    onTileTap: (x: Int, y: Int) -> Unit,
    onTileLongPress: (x: Int, y: Int) -> Unit,
    selectedTile: Pair<Int, Int>?,
    modifier: Modifier = Modifier
) {
    val columns = 4
    val spacing = 2.dp

    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        modifier = modifier.padding(horizontal = spacing),
        horizontalArrangement = Arrangement.spacedBy(spacing),
        verticalArrangement = Arrangement.spacedBy(spacing),
        userScrollEnabled = false,
    ) {
        items(columns * columns) { index ->
            val x = index % columns
            val y = index / columns
            val tileState = if (y < gridState.tiles.size && x < gridState.tiles[y].size) {
                gridState.tiles[y][x]
            } else {
                TileUiState()
            }
            val isSelected = selectedTile?.first == x && selectedTile.second == y

            TileView(
                tileState = tileState,
                onTap = { onTileTap(x, y) },
                onLongPress = { onTileLongPress(x, y) },
                isSelected = isSelected,
                modifier = Modifier.aspectRatio(1f)
            )
        }
    }
}
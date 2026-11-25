package fr.uha.hassenforder.easy.sudoku.ui.field

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import fr.uha.hassenforder.easy.sudoku.model.GameState
import fr.uha.hassenforder.easy.sudoku.model.PuzzleLevel

@Composable
fun LevelColoredStar (
    level : PuzzleLevel
) {
    val level : Color =
        when (level) {
            PuzzleLevel.NO -> Color.White
            PuzzleLevel.EASY -> Color.Green
            PuzzleLevel.MEDIUM -> Color.Yellow
            PuzzleLevel.HARD -> Color.Red
            PuzzleLevel.DIABOLIC -> Color.Red
            PuzzleLevel.EXTREM -> Color.Black
        }
    Icon(imageVector = Icons.Filled.Star, contentDescription = "level", modifier = Modifier.size(48.dp), tint = level )
}

@Composable
fun ColoredState (
    state : GameState,
) {
    val stateColor : Color =
        when (state) {
            GameState.STARTED -> Color.Blue
            GameState.LOST -> Color.Red
            GameState.WON -> Color.Green
        }
    val stateIcon : ImageVector =
        when (state) {
            GameState.STARTED -> Icons.Filled.Flag
            GameState.LOST -> Icons.Filled.ThumbDown
            GameState.WON -> Icons.Filled.ThumbUp
        }
    Icon(imageVector = stateIcon, contentDescription = "level", modifier = Modifier.size(24.dp), tint = stateColor )
}

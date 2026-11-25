package fr.uha.hassenforder.easy.sudoku.ui.game

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import fr.uha.hassenforder.easy.android.ui.DecoratorProperties
import fr.uha.hassenforder.easy.android.ui.app.AppTopBar
import fr.uha.hassenforder.easy.android.ui.lazy.noHorizontalLines
import fr.uha.hassenforder.easy.android.ui.screen.StateScreen
import fr.uha.hassenforder.easy.sudoku.R
import fr.uha.hassenforder.easy.sudoku.model.Cell
import fr.uha.hassenforder.easy.sudoku.model.CellState
import fr.uha.hassenforder.easy.sudoku.ui.field.ColoredState
import fr.uha.hassenforder.easy.sudoku.ui.field.GridAction
import fr.uha.hassenforder.easy.sudoku.ui.field.LevelColoredStar
import fr.uha.hassenforder.easy.sudoku.ui.field.PuzzlePlayField
import fr.uha.hassenforder.easy.sudoku.ui.field.PuzzleProperties

@Composable
fun PlayGameScreen (
    vm: GameViewModel = hiltViewModel(),
    gid : Long
) {
    val uiState by vm.uiState.collectAsStateWithLifecycle()
    val defaultDocumentName = stringResource(id = R.string.title_game)

    LaunchedEffect(Unit) {
        vm.uiTitleBuilder.setScreenNameId(sid = R.string.title_play)
        vm.uiTitleBuilder.setDefaultDocumentName(defaultDocumentName)
        vm.load(gid)
    }

    StateScreen(state = uiState) {
        PlayGameScreenInternal(it, { vm.send(it) })
    }
}

private fun cellStateToUse (actionChecked : List<GridAction>) : CellState {
    if (actionChecked.contains(GridAction.EDIT)) return CellState.GUESS
    return CellState.GUESS
}

@Composable
fun PlayGameScreenInternal(
    uiState: GameViewModel.UIState,
    send : (GameViewModel.UIEvent) -> Unit,
) {
    Scaffold(
        topBar = { AppTopBar(uiState.uiTitle, { send(GameViewModel.UIEvent.OnBack) }) },
    ) { innerPadding ->
        Column (
            modifier = Modifier
                .padding(innerPadding)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LevelColoredStar(uiState.game.puzzle.level)
                Text(text = uiState.game.puzzle.description)
                Spacer(modifier = Modifier.weight(1f))
                ColoredState(uiState.game.game.state)
            }
            PuzzlePlayField(
                values = uiState.game.cells,
                helpers = uiState.helpers,
                onChange = { cell, value ->
                    if (cell.state != CellState.INITIAL) {
                        val cellState = cellStateToUse (uiState.actionChecked)
                        send(
                            GameViewModel.UIEvent.CellChanged(
                                cell.copy(
                                    value = value,
                                    state = cellState
                                )
                            )
                        )
                    }
                },
                actionChecked = uiState.actionChecked,
                onAction = { send(GameViewModel.UIEvent.UseAction(it)) },
                decorator = DecoratorProperties(
                    labelId = R.string.game,
                ),
                config = PuzzleProperties(
                    count = uiState.game.puzzle.size,
                ),
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

/*

 */

@Composable
private fun PuzzleCell (cell : Cell) {
    val colorLine = MaterialTheme.colorScheme.onBackground
    Row (
        modifier = Modifier
            .fillMaxWidth()
            .background(color = MaterialTheme.colorScheme.background)
            .drawBehind { noHorizontalLines(color = colorLine, strokeWidth = 3f) }
            .padding(top = 4.dp, bottom = 4.dp, start =16.dp)
    ) {
        Text(cell.value.toString())
    }
}

package fr.uha.hassenforder.easy.sudoku.ui.puzzle

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.unit.dp
import fr.uha.hassenforder.easy.android.ui.DecoratorProperties
import fr.uha.hassenforder.easy.android.ui.app.AppTopBar
import fr.uha.hassenforder.easy.android.ui.field.group.Orientation
import fr.uha.hassenforder.easy.android.ui.field.group.OutlinedEnumRadioGroup
import fr.uha.hassenforder.easy.android.ui.field.group.RadioProperties
import fr.uha.hassenforder.easy.android.ui.field.text.OutlinedTextField
import fr.uha.hassenforder.easy.android.ui.field.text.TextProperties
import fr.uha.hassenforder.easy.android.ui.lazy.noHorizontalLines
import fr.uha.hassenforder.easy.sudoku.R
import fr.uha.hassenforder.easy.sudoku.model.Cell
import fr.uha.hassenforder.easy.sudoku.model.CellState
import fr.uha.hassenforder.easy.sudoku.model.PuzzleLevel
import fr.uha.hassenforder.easy.sudoku.ui.field.PuzzleBuilderField
import fr.uha.hassenforder.easy.sudoku.ui.field.PuzzleProperties

@Composable
fun SuccessPuzzleScreen(
    uiState: PuzzleViewModel.UIState,
    send : (PuzzleViewModel.UIEvent) -> Unit,
) {
    Scaffold(
        topBar = { AppTopBar(uiState.uiTitle, { send(PuzzleViewModel.UIEvent.OnBack) }) },
    ) { innerPadding ->
        Column (
            modifier = Modifier
                .padding(innerPadding)
        ) {
            OutlinedTextField(
                value = uiState.puzzle.puzzle.description,
                onValueChange = { send(PuzzleViewModel.UIEvent.DescriptionChanged(it)) },
                properties = TextProperties(),
                modifier = Modifier.fillMaxWidth(),
                decorator = DecoratorProperties(
                    labelId = R.string.description,
                    errorId = uiState.validation.description,
                ),
            )
            val outlinedIcon = Icons.Outlined.Star
            val filledIcon = Icons.Filled.Star
            OutlinedEnumRadioGroup(
                value = uiState.puzzle.puzzle.level,
                onValueChange = { send(PuzzleViewModel.UIEvent.LevelChanged(PuzzleLevel.valueOf(it))) },
                properties = RadioProperties(
                    itemValues = PuzzleLevel.entries.toTypedArray(),
                    orientation = Orientation.HorizontalGrid(3),
                ),
                modifier = Modifier.fillMaxWidth(),
                decorator = DecoratorProperties(
                    labelId = R.string.level,
                    errorId = uiState.validation.level,
                ),
            )
            PuzzleBuilderField(
                values = uiState.puzzle.cells,
                onChange = { cell, value ->
                    send(
                        PuzzleViewModel.UIEvent.CellChanged(
                            cell.copy(
                                value = value,
                                state = CellState.INITIAL
                            )
                        )
                    )
                },
                decorator = DecoratorProperties(
                    labelId = R.string.puzzle,
                    errorId = uiState.validation.cells,
                ),
                config = PuzzleProperties(
                    count = uiState.puzzle.puzzle.size,
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

package fr.uha.hassenforder.easy.sudoku.ui.puzzle

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import fr.uha.hassenforder.easy.android.ui.screen.StateScreen
import fr.uha.hassenforder.easy.sudoku.R
import fr.uha.hassenforder.easy.sudoku.model.Puzzle
import fr.uha.hassenforder.easy.sudoku.model.PuzzleLevel

@Composable
fun CreatePuzzleScreen (
    vm: PuzzleViewModel = hiltViewModel(),
) {
    val uiState by vm.uiState.collectAsStateWithLifecycle()
    val defaultDocumentName = stringResource(id = R.string.title_puzzle)

    LaunchedEffect(Unit) {
        vm.uiTitleBuilder.setScreenNameId(sid = R.string.title_create)
        vm.uiTitleBuilder.setDefaultDocumentName(defaultDocumentName)
        val puzzle = Puzzle(
            0,
            "Puzzle",
            9,
            PuzzleLevel.EASY,
        )
        vm.create(puzzle)
    }

    StateScreen(state = uiState) {
        SuccessPuzzleScreen(it, { vm.send(it) })
    }

}

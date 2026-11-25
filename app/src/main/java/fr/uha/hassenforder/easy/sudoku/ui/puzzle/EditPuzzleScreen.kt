package fr.uha.hassenforder.easy.sudoku.ui.puzzle

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import fr.uha.hassenforder.easy.android.ui.screen.StateScreen
import fr.uha.hassenforder.easy.sudoku.R

@Composable
fun EditPuzzleScreen (
    vm: PuzzleViewModel = hiltViewModel(),
    pid : Long
) {
    val uiState by vm.uiState.collectAsStateWithLifecycle()
    val defaultDocumentName = stringResource(id = R.string.title_puzzle)

    LaunchedEffect(Unit) {
        vm.uiTitleBuilder.setScreenNameId(sid = R.string.title_edit)
        vm.uiTitleBuilder.setDefaultDocumentName(defaultDocumentName)
        vm.edit(pid)
    }

    StateScreen(state = uiState) {
        SuccessPuzzleScreen(it, { vm.send(it) })
    }
}

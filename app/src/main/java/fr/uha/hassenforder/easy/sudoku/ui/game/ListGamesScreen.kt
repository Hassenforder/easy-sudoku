package fr.uha.hassenforder.easy.sudoku.ui.game

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Create
import androidx.compose.material.icons.outlined.DoubleArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import fr.uha.hassenforder.easy.android.ui.UIConverter
import fr.uha.hassenforder.easy.android.ui.app.AppMenuEntry
import fr.uha.hassenforder.easy.android.ui.app.AppTopBar
import fr.uha.hassenforder.easy.android.ui.app.appMenuEntriesBuilder
import fr.uha.hassenforder.easy.android.ui.lazy.SwipeableItem
import fr.uha.hassenforder.easy.android.ui.screen.StateScreen
import fr.uha.hassenforder.easy.sudoku.R
import fr.uha.hassenforder.easy.sudoku.dto.PuzzleWithGames
import fr.uha.hassenforder.easy.sudoku.model.Game
import fr.uha.hassenforder.easy.sudoku.repository.GameRepository.ListMode
import fr.uha.hassenforder.easy.sudoku.ui.dialog.ConfirmationDeleteDialog
import fr.uha.hassenforder.easy.sudoku.ui.field.ColoredState
import fr.uha.hassenforder.easy.sudoku.ui.field.LevelColoredStar

@Composable
fun ListGamesScreen(
    vm: ListGamesViewModel = hiltViewModel(),
) {
    val uiState by vm.uiState.collectAsStateWithLifecycle()
    val defaultDocumentName = stringResource(id = R.string.title_games)

    LaunchedEffect(Unit) {
        vm.titleBuilder.setScreenNameId(sid = R.string.title_list)
        vm.titleBuilder.setDefaultDocumentName(defaultDocumentName)
    }

    StateScreen(uiState) {
        SuccessListScreen(it, { vm.send(it) } )
    }
}

private sealed class ShowDialog {
    object None : ShowDialog()
    class WarningDelete(val puzzle : PuzzleWithGames) : ShowDialog()
}

@Composable
private fun SuccessListScreen(
    uiState: ListGamesViewModel.UIState,
    send : (ListGamesViewModel.UIEvent) -> Unit,
) {
    val showDialog : MutableState<ShowDialog> = remember { mutableStateOf(ShowDialog.None) }

    when (showDialog.value) {
        is ShowDialog.None -> { }
        is ShowDialog.WarningDelete -> {
            val puzzle = (showDialog.value as ShowDialog.WarningDelete).puzzle
            ConfirmationDeleteDialog(
                onSelect = {
                    showDialog.value = ShowDialog.None
                    when (it) {
                        null -> {}
                        0 -> send(ListGamesViewModel.UIEvent.OnDeleteAll(puzzle))
                        1 -> {}
                    }
                }
            )
        }
    }
    val modeAsStrings = stringArrayResource(R.array.list_mode)
    val menuEntries = remember (uiState.listMode) {
        appMenuEntriesBuilder(
            selected = uiState.listMode,
            itemValues = ListMode.entries.toTypedArray(),
            itemLabels = modeAsStrings,
            onChange = { send(ListGamesViewModel.UIEvent.OnList(it)) }
        )
    }
    Scaffold(
        topBar = { AppTopBar(uiState.uiTitle, { send(ListGamesViewModel.UIEvent.OnBack) }, menuEntries ) },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.padding(innerPadding)
        ) {
            items(
                items = uiState.content,
                key = { content -> content.key },
                contentType = { content -> content.javaClass }
            ) { item ->
                when(item) {
                    is ListGamesViewModel.MixedContent.PuzzleContent -> {
                        SwipeableItem(
                            onEdit = { send(ListGamesViewModel.UIEvent.OnCreate(item.puzzle)) },
                            onDelete = { showDialog.value = ShowDialog.WarningDelete(item.puzzle) }
                        ) {
                            PuzzleItem(item.puzzle)
                        }
                    }
                    is ListGamesViewModel.MixedContent.GameContent -> {
                        SwipeableItem(
                            onEdit = { send(ListGamesViewModel.UIEvent.OnResume(item.game)) },
                            onDelete = { send(ListGamesViewModel.UIEvent.OnDelete(item.game)) }
                        ) {
                            GameItem(item.game)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PuzzleItem (
    puzzle : PuzzleWithGames,
) {
    ListItem(
        modifier = Modifier.fillMaxWidth(),
        leadingContent = {
            LevelColoredStar(puzzle.puzzle.level)
        },
        content = {
            Row (
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(puzzle.puzzle.description, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            }
        },
    )
}

@Composable
private fun GameItem (
    game : Game,
) {
    ListItem(
        modifier = Modifier.fillMaxWidth(),
        leadingContent = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.DoubleArrow,
                    contentDescription = "double arrow"
                )
                ColoredState(game.state)
            }
        },
        content = {
            Row (
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Create,
                        contentDescription = "created date"
                    )
                    Text(UIConverter.convert(game.gameCreatedDate))
                }
            }
        },
    )
}

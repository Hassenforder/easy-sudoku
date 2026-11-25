package fr.uha.hassenforder.easy.sudoku.ui.puzzle

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.outlined.Create
import androidx.compose.material.icons.outlined.ThumbDown
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material.icons.outlined.ToggleOff
import androidx.compose.material.icons.outlined.ToggleOn
import androidx.compose.material.icons.outlined.Traffic
import androidx.compose.material.icons.outlined.Update
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import fr.uha.hassenforder.easy.android.database.model.ListMode
import fr.uha.hassenforder.easy.android.ui.UIConverter
import fr.uha.hassenforder.easy.android.ui.ExpandableFABItem
import fr.uha.hassenforder.easy.android.ui.ExpandableFloatingActionButton
import fr.uha.hassenforder.easy.android.ui.field.management.ManagementStateText
import fr.uha.hassenforder.easy.android.ui.app.AppTopBar
import fr.uha.hassenforder.easy.android.ui.lazy.SwipeableItem
import fr.uha.hassenforder.easy.android.ui.field.management.managementListModeMenuEntries
import fr.uha.hassenforder.easy.android.ui.screen.StateScreen
import fr.uha.hassenforder.easy.sudoku.R
import fr.uha.hassenforder.easy.sudoku.dto.PuzzleWithDetails
import fr.uha.hassenforder.easy.sudoku.model.PuzzleLevel
import fr.uha.hassenforder.easy.sudoku.ui.dialog.ConfirmationDeleteDialog
import fr.uha.hassenforder.easy.sudoku.ui.dialog.ConfirmationEditDialog
import fr.uha.hassenforder.easy.sudoku.ui.puzzle.ListPuzzlesViewModel.UIState

@Composable
fun ListPuzzlesScreen(
    vm: ListPuzzlesViewModel = hiltViewModel(),
) {
    val uiState by vm.uiState.collectAsStateWithLifecycle()
    val defaultDocumentName = stringResource(id = R.string.title_puzzles)

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
    class WarningEdit(val puzzle : PuzzleWithDetails) : ShowDialog()
    class WarningDelete(val puzzle : PuzzleWithDetails) : ShowDialog()
}

@Composable
private fun SuccessListScreen(
    uiState: UIState,
    send : (ListPuzzlesViewModel.UIEvent) -> Unit,
) {
    val showDialog : MutableState<ShowDialog> = remember { mutableStateOf(ShowDialog.None) }

    when (showDialog.value) {
        is ShowDialog.None -> { }
        is ShowDialog.WarningEdit -> {
            val puzzle = (showDialog.value as ShowDialog.WarningEdit).puzzle
            ConfirmationEditDialog(
                onSelect = {
                    showDialog.value = ShowDialog.None
                    when (it) {
                        null -> {}
                        0 -> send(ListPuzzlesViewModel.UIEvent.OnEdit(puzzle))
                        1 -> send(ListPuzzlesViewModel.UIEvent.OnClone(puzzle))
                        2 -> { }
                    }
                }
            )
        }
        is ShowDialog.WarningDelete -> {
            val puzzle = (showDialog.value as ShowDialog.WarningDelete).puzzle
            ConfirmationDeleteDialog(
                onSelect = {
                    showDialog.value = ShowDialog.None
                    when (it) {
                        null -> {}
                        0 -> send(ListPuzzlesViewModel.UIEvent.OnDelete(puzzle))
                        1 -> {}
                    }
                }
            )
        }
    }
    val menuEntries = managementListModeMenuEntries(
        uiState.listMode,
        { send(ListPuzzlesViewModel.UIEvent.OnList(it)) }
    )
    val createTitle = stringResource(id = R.string.title_create)
    val importTitle = stringResource(id = R.string.title_import)
    Scaffold(
        topBar = { AppTopBar(uiState.uiTitle, { send(ListPuzzlesViewModel.UIEvent.OnBack) }, menuEntries ) },
        floatingActionButton = {
            val itemList = listOf(
                ExpandableFABItem(Icons.Filled.Add, createTitle, { send(ListPuzzlesViewModel.UIEvent.OnCreate) }),
                ExpandableFABItem(Icons.Filled.Upload, importTitle, { send(ListPuzzlesViewModel.UIEvent.OnImport) }),
            )
            ExpandableFloatingActionButton(itemList)
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.padding(innerPadding)
        ) {
            items(
                items = uiState.puzzles,
                key = { puzzle -> puzzle.puzzle.pid }
            ) { item ->
                SwipeableItem(
                    onEdit = {
                        if (item.gameInProgress()) {
                            showDialog.value = ShowDialog.WarningEdit(item)
                        } else send(ListPuzzlesViewModel.UIEvent.OnEdit(item))
                    },
                    onDelete = {
                        if (item.gameInProgress()) {
                            showDialog.value = ShowDialog.WarningDelete(item)
                        } else send(ListPuzzlesViewModel.UIEvent.OnDelete(item))
                    }
                ) {
                    PuzzleItem(item, uiState.listMode)
                }
            }
        }
    }
}

@Composable
private fun PuzzleItem (
    puzzle : PuzzleWithDetails,
    listMode : ListMode,
) {
    val showDates : MutableState<Boolean> = remember { mutableStateOf(false) }
    val level : Color =
        when (puzzle.puzzle.level) {
            PuzzleLevel.NO -> Color.White
            PuzzleLevel.EASY -> Color.Green
            PuzzleLevel.MEDIUM -> Color.Yellow
            PuzzleLevel.HARD -> Color.Red
            PuzzleLevel.DIABOLIC -> Color.Red
            PuzzleLevel.EXTREM -> Color.Black
        }
    ListItem(
        modifier = Modifier.fillMaxWidth(),
        leadingContent = {
            Icon(imageVector = Icons.Filled.Star, contentDescription = "level", modifier = Modifier.size(48.dp), tint = level )
        },
        supportingContent = {
            if (showDates.value) {
                Column {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Create,
                            contentDescription = "created date"
                        )
                        Text(UIConverter.convert(puzzle.puzzle.puzzleCreatedDate))
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(
                            imageVector = Icons.Outlined.Update,
                            contentDescription = "updated date"
                        )
                        Text(UIConverter.convert(puzzle.puzzle.puzzleUpdatedDate))
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Outlined.Traffic, contentDescription = "startedCount")
                        Text(puzzle.startedCount.toString())
                        Spacer(modifier = Modifier.width(16.dp))
                        Icon(imageVector = Icons.Outlined.ThumbDown, contentDescription = "lostCount")
                        Text(puzzle.lostCount.toString())
                        Spacer(modifier = Modifier.width(16.dp))
                        Icon(imageVector = Icons.Outlined.ThumbUp, contentDescription = "wonCount")
                        Text(puzzle.wonCount.toString())
                    }
                }
            }
        },
        content = {
            Row (
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(puzzle.puzzle.description, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.weight(1f))
                ManagementStateText(puzzle.puzzle.managementState, listMode)
                Icon(
                    imageVector = if (showDates.value) Icons.Outlined.ToggleOff else Icons.Outlined.ToggleOn,
                    contentDescription = "toggle dates",
                    modifier = Modifier.clickable { showDates.value = !showDates.value }
                )
            }
        },
    )
}

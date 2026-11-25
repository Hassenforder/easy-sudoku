package fr.uha.hassenforder.easy.sudoku.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import fr.uha.hassenforder.easy.sudoku.ui.puzzle.CreatePuzzleScreen
import fr.uha.hassenforder.easy.sudoku.ui.puzzle.EditPuzzleScreen
import fr.uha.hassenforder.easy.sudoku.ui.puzzle.importer.ImportPuzzleScreen
import fr.uha.hassenforder.easy.sudoku.ui.puzzle.ListPuzzlesScreen
import kotlinx.serialization.Serializable

sealed interface PuzzlesDestination : NavKey {
    @Serializable
    data object ListScreen: NavKey

    @Serializable
    data object CreateScreen: NavKey

    @Serializable
    data object ImportScreen: NavKey

    @Serializable
    data class EditScreen(val pid: Long): NavKey
}

fun EntryProviderScope<NavKey>.puzzlesNavGraph () {
    entry<PuzzlesDestination.ListScreen> {
        ListPuzzlesScreen()
    }
    entry<PuzzlesDestination.CreateScreen> {
        CreatePuzzleScreen()
    }
    entry<PuzzlesDestination.ImportScreen> {
        ImportPuzzleScreen()
    }
    entry<PuzzlesDestination.EditScreen> {
        EditPuzzleScreen(pid = it.pid)
    }
}
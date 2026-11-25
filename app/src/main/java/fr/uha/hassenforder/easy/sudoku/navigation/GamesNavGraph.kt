package fr.uha.hassenforder.easy.sudoku.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import fr.uha.hassenforder.easy.sudoku.ui.game.ListGamesScreen
import fr.uha.hassenforder.easy.sudoku.ui.game.PlayGameScreen
import kotlinx.serialization.Serializable

sealed interface GamesDestination : NavKey {
    @Serializable
    data object ListScreen: NavKey

    @Serializable
    data class PlayScreen(val gid: Long): NavKey
}

fun EntryProviderScope<NavKey>.gamesNavGraph () {
    entry<GamesDestination.ListScreen> {
        ListGamesScreen()
    }
    entry<GamesDestination.PlayScreen> {
        PlayGameScreen(gid = it.gid)
    }
}
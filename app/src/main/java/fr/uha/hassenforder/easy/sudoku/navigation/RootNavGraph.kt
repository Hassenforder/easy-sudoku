package fr.uha.hassenforder.easy.sudoku.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey

fun EntryProviderScope<NavKey>.rootNavGraph () {
    greetingNavGraph()
    puzzlesNavGraph()
    gamesNavGraph()
    settingsNavGraph()
}
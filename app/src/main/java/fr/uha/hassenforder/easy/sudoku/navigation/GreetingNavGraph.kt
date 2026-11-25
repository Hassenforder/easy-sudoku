package fr.uha.hassenforder.easy.sudoku.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import fr.uha.hassenforder.easy.sudoku.ui.GreetingScreen
import kotlinx.serialization.Serializable

sealed interface GreetingDestination : NavKey {
    @Serializable
    data object GreetingScreen: NavKey
}

fun EntryProviderScope<NavKey>.greetingNavGraph () {
    entry<GreetingDestination.GreetingScreen> {
        GreetingScreen()
    }
}
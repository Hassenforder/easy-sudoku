package fr.uha.hassenforder.easy.sudoku.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import fr.uha.hassenforder.easy.sudoku.ui.settings.SettingsScreen
import kotlinx.serialization.Serializable

sealed interface SettingsDestination : NavKey {

    @Serializable
    data object SettingsScreen: NavKey
}

fun EntryProviderScope<NavKey>.settingsNavGraph () {
    entry<SettingsDestination.SettingsScreen> {
        SettingsScreen()
    }
}
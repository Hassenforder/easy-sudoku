package fr.uha.hassenforder.easy.sudoku.ui.app

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.GridOn
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.runtime.Composable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import fr.uha.hassenforder.easy.android.model.FlexibleIcon
import fr.uha.hassenforder.easy.android.model.IconRender
import fr.uha.hassenforder.easy.android.ui.app.AppNavigatorScreen
import fr.uha.hassenforder.easy.android.ui.app.PrimaryNavigation
import fr.uha.hassenforder.easy.sudoku.R
import fr.uha.hassenforder.easy.sudoku.navigation.GamesDestination
import fr.uha.hassenforder.easy.sudoku.navigation.GreetingDestination
import fr.uha.hassenforder.easy.sudoku.navigation.PuzzlesDestination
import fr.uha.hassenforder.easy.sudoku.navigation.SettingsDestination

@Composable
fun SudokuAppScreen(
    vm: AppViewModel = hiltViewModel(),
) {
    AppNavigatorScreen(vm.navigator, primaryNavigations)
}

private val primaryNavigations = listOf (
    PrimaryNavigation(
        destination = GreetingDestination.GreetingScreen,
        icon = IconRender(
            focused = FlexibleIcon(vector = Icons.Filled.Home),
            unfocused = FlexibleIcon(vector = Icons.Outlined.Home)
        ),
        labelId = R.string.app_name
    ),
    /*
    BottomBarNavKey(
        destination = PuzzlesDestination.ImportScreen,
        icon = IconRender(focused = FlexibleIcon(vector= Icons.Filled.QrCode), unfocused = FlexibleIcon(vector= Icons.Outlined.QrCode)),
        labelId = R.string.action_import
    ),
     */
    PrimaryNavigation(
        destination = PuzzlesDestination.ListScreen,
        icon = IconRender(focused = FlexibleIcon(vector= Icons.Filled.Download), unfocused = FlexibleIcon(vector= Icons.Outlined.Download)),
        labelId = R.string.action_read
    ),
    PrimaryNavigation(
        destination = GamesDestination.ListScreen,
        icon = IconRender(focused = FlexibleIcon(vector= Icons.Filled.GridOn), unfocused = FlexibleIcon(vector= Icons.Outlined.GridOn)),
        labelId = R.string.action_solve
    ),
    PrimaryNavigation(
        destination = SettingsDestination.SettingsScreen,
        icon = IconRender(focused = FlexibleIcon(vector= Icons.Filled.Settings), unfocused = FlexibleIcon(vector= Icons.Outlined.Settings)),
        labelId = R.string.action_settings
    ),
)

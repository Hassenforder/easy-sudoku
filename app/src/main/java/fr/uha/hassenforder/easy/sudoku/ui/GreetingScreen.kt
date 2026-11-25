package fr.uha.hassenforder.easy.sudoku.ui

import androidx.compose.runtime.Composable
import fr.uha.hassenforder.easy.android.ui.screen.greeting.EasyGreetingScreen
import fr.uha.hassenforder.easy.android.ui.screen.greeting.PortraitGreeting
import fr.uha.hassenforder.easy.sudoku.R

@Composable
fun GreetingScreen() {
    EasyGreetingScreen (
        titleId = R.string.app_short,
        launch = null,
    ) {
        PortraitGreeting()
    }
}

package fr.uha.hassenforder.easy.sudoku.ui.app

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.uha.hassenforder.easy.android.navigation.Navigator
import javax.inject.Inject

@HiltViewModel
class AppViewModel @Inject constructor (
    val navigator: Navigator
): ViewModel()

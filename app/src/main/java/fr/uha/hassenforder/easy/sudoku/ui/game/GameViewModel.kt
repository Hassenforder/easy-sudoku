package fr.uha.hassenforder.easy.sudoku.ui.game

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.uha.hassenforder.easy.android.flow.Result
import fr.uha.hassenforder.easy.android.navigation.Navigator
import fr.uha.hassenforder.easy.android.ui.app.UITitle
import fr.uha.hassenforder.easy.android.ui.app.UITitleBuilder
import fr.uha.hassenforder.easy.sudoku.database.dao.GameDao
import fr.uha.hassenforder.easy.sudoku.database.dao.GameDao.GameUpdate.*
import fr.uha.hassenforder.easy.sudoku.dto.FullGame
import fr.uha.hassenforder.easy.sudoku.model.Cell
import fr.uha.hassenforder.easy.sudoku.model.CellState
import fr.uha.hassenforder.easy.sudoku.model.GameState
import fr.uha.hassenforder.easy.sudoku.repository.GameRepository
import fr.uha.hassenforder.easy.sudoku.ui.field.GridAction
import fr.uha.hassenforder.easy.sudoku.model.HelpersBuilder
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GameViewModel @Inject constructor (
    private val repository: GameRepository,
    private val navigator: Navigator
): ViewModel() {

    val uiTitleBuilder = UITitleBuilder()

    private val _uiTitle : StateFlow<UITitle> = uiTitleBuilder.uiTitle.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = UITitle()
    )

    private val _id: MutableStateFlow<Long> = MutableStateFlow(0)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val _game: StateFlow<Result<FullGame>> = _id
        .flatMapLatest { id -> repository.getGameById(id) }
        .map { game ->
            if (game != null) {
                Result.Success(content = game)
            } else {
                Result.Error()
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Result.Loading())

    private val _actionChecked: MutableStateFlow<List<GridAction>> = MutableStateFlow(listOf(GridAction.EDIT))

    private fun toggleAction (action : GridAction) {
        val newActions = if (_actionChecked.value.contains(action)) {
            _actionChecked.value - action
        } else {
            _actionChecked.value + action
        }
        _actionChecked.value = newActions
    }

    private fun toggleAction (actionToAdd : GridAction, actionToRemove : GridAction) {
        val newActions = _actionChecked.value + actionToAdd - actionToRemove
        _actionChecked.value = newActions
    }

    @Immutable
    data class UIState(
        val uiTitle : UITitle,
        val game : FullGame,
        val actionChecked : List<GridAction>,
        val helpers : Map<Int, List<Int>>,
    ) {
        companion object {
            fun create(
                uiTitle: UITitle,
                game : FullGame,
                actionChecked: List<GridAction>
            ): UIState {
                val helpers = if (actionChecked.contains(GridAction.HELPER)) {
                    val gridHelpers = HelpersBuilder(game.cells)
                    gridHelpers.buildHelpers()
                } else {
                    mapOf<Int, List<Int>>()
                }
                return UIState(uiTitle, game, actionChecked, helpers)
            }
        }
    }

    val uiState : StateFlow<Result<UIState>> = combine (_uiTitle, _game, _actionChecked)
        { t, p, a ->
            when (p) {
                is Result.Success -> Result.Success(content = UIState.create(t, p.content, a))
                is Result.Error -> Result.Error(t)
                is Result.Loading -> Result.Loading(t)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Result.Loading())

    sealed class UIEvent {
        data object OnBack: UIEvent()
        data class CellChanged(val newValue: Cell): UIEvent()
        data class UseAction (val action : GridAction): UIEvent()
    }

    fun send (uiEvent : UIEvent) = viewModelScope.launch {
        if (uiState.value !is Result.Success) return@launch
        val uiState = (uiState.value as Result.Success).content
        when (uiEvent) {
            is UIEvent.OnBack -> navigator.navigateUp()
            is UIEvent.UseAction -> {
                when (uiEvent.action) {
                    GridAction.EDIT -> toggleAction(GridAction.EDIT, GridAction.HELPER)
                    GridAction.HELPER -> toggleAction(GridAction.HELPER)
                    GridAction.ERASE_ALL -> repository.erase (uiState.game)
                    GridAction.CLONE -> repository.clone (uiState.game)
                    GridAction.SUGGEST -> { } // gives a cell
                    GridAction.VALID -> {
                        val gridHelpers = HelpersBuilder(uiState.game.cells)
                        val state = gridHelpers.checker()
                        if (uiState.game.game.state != state) {
                            repository.update(State(uiState.game.game.gid, state))
                        }
                    }
                    GridAction.AUTO_VALID -> toggleAction(GridAction.AUTO_VALID)
                }
            }
            is UIEvent.CellChanged -> {
                if (uiState.actionChecked.contains(GridAction.AUTO_VALID)) {
                    val gridHelpers = HelpersBuilder(uiState.game.cells)
                    val state = gridHelpers.checker(uiEvent.newValue)
                    if (uiState.game.game.state != state) {
                        repository.update(GameDao.GameUpdate.State(uiState.game.game.gid, state))
                    }
                    if (state == GameState.LOST) {
                        repository.updateCell(cell = uiEvent.newValue.copy (state = CellState.ERROR))
                    } else {
                        repository.updateCell(cell = uiEvent.newValue)
                    }
                } else {
                    repository.updateCell(cell = uiEvent.newValue)
                }
            }
        }
    }

    fun load (gid : Long) = viewModelScope.launch {
        val gid : Long = repository.load(gid)
        _id.value = gid
    }

}

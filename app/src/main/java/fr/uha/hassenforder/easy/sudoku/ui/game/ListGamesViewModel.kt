package fr.uha.hassenforder.easy.sudoku.ui.game

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.uha.hassenforder.easy.android.flow.Result
import fr.uha.hassenforder.easy.android.navigation.Navigator
import fr.uha.hassenforder.easy.android.ui.app.UITitle
import fr.uha.hassenforder.easy.android.ui.app.UITitleBuilder
import fr.uha.hassenforder.easy.sudoku.dto.PuzzleWithGames
import fr.uha.hassenforder.easy.sudoku.model.Game
import fr.uha.hassenforder.easy.sudoku.navigation.GamesDestination
import fr.uha.hassenforder.easy.sudoku.repository.GameRepository
import fr.uha.hassenforder.easy.sudoku.repository.GameRepository.ListMode
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
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
class ListGamesViewModel @Inject constructor (
    private val repository: GameRepository,
    private val navigator: Navigator
): ViewModel() {

    val titleBuilder = UITitleBuilder()

    private val _uiTitle : StateFlow<UITitle> = titleBuilder.uiTitle.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = UITitle()
    )

    private val _listMode = MutableStateFlow(ListMode.ALL)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val _puzzles: Flow<Result<List<PuzzleWithGames>>> = _listMode
        .flatMapLatest { m -> repository.getAllFiltered(m) }
        .map { content -> Result.Success(content) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = Result.Loading()
        )

    /*
     * key is unique across all content (* 10 means an internal offset filled by a key of the type
     *    key(puzzle) = 1
     *    key(game) = 2
     */
    sealed class MixedContent (val key : Long) {
        data class PuzzleContent(val puzzle: PuzzleWithGames) : MixedContent(key = puzzle.puzzle.pid * 10 + 1)
        data class GameContent(val game : Game) : MixedContent(key = game.gid * 10 + 2)
    }

    @Immutable
    data class UIState (
        val uiTitle: UITitle,
        val listMode : ListMode,
        val content : List<MixedContent>,
    ) {
        companion object {
            fun create (uiTitle: UITitle, listMode : ListMode, puzzles : List<PuzzleWithGames>) : UIState {
                val content = mutableListOf<MixedContent>()
                puzzles.forEach { p ->
                    content.add(MixedContent.PuzzleContent(p))
                    p.games.forEach { g ->
                        content.add(MixedContent.GameContent(g))
                    }
                }
                return UIState (uiTitle, listMode, content)
            }
        }
    }

    val uiState : StateFlow<Result<UIState>> = combine(_uiTitle, _listMode, _puzzles)
        { t, m, l ->
            when (l) {
                is Result.Success -> Result.Success(content = UIState.create(t, m, l.content))
                is Result.Error -> Result.Error(t)
                is Result.Loading -> Result.Loading(t)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = Result.Loading()
        )

    sealed class UIEvent {
        data object OnBack: UIEvent()
        data class OnList(val newValue: ListMode): UIEvent()
        data class OnCreate(val newValue: PuzzleWithGames): UIEvent()
        data class OnDeleteAll(val newValue: PuzzleWithGames): UIEvent()
        data class OnResume(val newValue: Game): UIEvent()
        data class OnDelete(val newValue: Game): UIEvent()
    }

    fun send (uiEvent : UIEvent) = viewModelScope.launch {
        when (uiEvent) {
            is UIEvent.OnBack -> navigator.navigateUp()
            is UIEvent.OnList -> _listMode.value = uiEvent.newValue
            is UIEvent.OnCreate -> {
                val gid = repository.create(uiEvent.newValue)
                navigator.navigate(
                    destination = GamesDestination.PlayScreen(gid),
                )
            }
            is UIEvent.OnDeleteAll -> repository.deleteAll(uiEvent.newValue)
            is UIEvent.OnResume -> {
                navigator.navigate(
                    destination = GamesDestination.PlayScreen(uiEvent.newValue.gid),
                )
            }
            is UIEvent.OnDelete -> repository.delete(uiEvent.newValue)
        }
    }

}
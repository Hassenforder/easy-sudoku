package fr.uha.hassenforder.easy.sudoku.ui.puzzle

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.uha.hassenforder.easy.android.database.model.ListMode
import fr.uha.hassenforder.easy.android.flow.Result
import fr.uha.hassenforder.easy.android.navigation.Navigator
import fr.uha.hassenforder.easy.android.ui.app.UITitle
import fr.uha.hassenforder.easy.android.ui.app.UITitleBuilder
import fr.uha.hassenforder.easy.sudoku.dto.PuzzleWithDetails
import fr.uha.hassenforder.easy.sudoku.navigation.PuzzlesDestination
import fr.uha.hassenforder.easy.sudoku.repository.PuzzleRepository
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
class ListPuzzlesViewModel @Inject constructor (
    private val repository: PuzzleRepository,
    private val navigator: Navigator
): ViewModel() {

    val titleBuilder = UITitleBuilder()

    private val _uiTitle : StateFlow<UITitle> = titleBuilder.uiTitle.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = UITitle()
    )

    private val _listMode = MutableStateFlow(ListMode.FILTERED)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val _puzzles: Flow<Result<List<PuzzleWithDetails>>> = _listMode
        .flatMapLatest { m ->
            when(m) {
                ListMode.ALL -> repository.getAllWithDetails()
                ListMode.FILTERED -> repository.getFilteredWithDetails(m)
            }
        }
        .map { content -> Result.Success(content) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = Result.Loading()
        )

    @Immutable
    data class UIState (
        val uiTitle: UITitle,
        val listMode : ListMode,
        val puzzles : List<PuzzleWithDetails>,
    )

    val uiState : StateFlow<Result<UIState>> = combine(_uiTitle, _listMode, _puzzles)
        { t, m, l ->
            when (l) {
                is Result.Success -> Result.Success(content = UIState(t, m, l.content))
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
        data object OnCreate: UIEvent()
        data object OnImport: UIEvent()
        data class OnEdit(val newValue: PuzzleWithDetails): UIEvent()
        data class OnClone(val newValue: PuzzleWithDetails): UIEvent()
        data class OnDelete(val newValue: PuzzleWithDetails): UIEvent()
    }

    fun send (uiEvent : UIEvent) = viewModelScope.launch {
        when (uiEvent) {
            is UIEvent.OnBack -> navigator.navigateUp()
            is UIEvent.OnList -> _listMode.value = uiEvent.newValue
            is UIEvent.OnCreate -> navigator.navigate(
                destination = PuzzlesDestination.CreateScreen,
            )
            is UIEvent.OnImport -> navigator.navigate(
                destination = PuzzlesDestination.ImportScreen,
            )
            is UIEvent.OnEdit -> navigator.navigate(
                destination = PuzzlesDestination.EditScreen(uiEvent.newValue.puzzle.pid),
            )
            is UIEvent.OnClone -> {
                val cid : Long = repository.clone(uiEvent.newValue.puzzle)
                navigator.navigate(
                    destination = PuzzlesDestination.EditScreen(cid),
                )
            }
            is UIEvent.OnDelete -> repository.delete(uiEvent.newValue.puzzle)
        }
    }

}
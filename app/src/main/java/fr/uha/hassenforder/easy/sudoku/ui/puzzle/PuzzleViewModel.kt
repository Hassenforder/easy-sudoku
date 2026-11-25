package fr.uha.hassenforder.easy.sudoku.ui.puzzle

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.uha.hassenforder.easy.android.flow.Result
import fr.uha.hassenforder.easy.android.navigation.Navigator
import fr.uha.hassenforder.easy.android.ui.app.UITitle
import fr.uha.hassenforder.easy.android.ui.app.UITitleBuilder
import fr.uha.hassenforder.easy.sudoku.database.dao.PuzzleDao.PuzzleUpdate.CellContent
import fr.uha.hassenforder.easy.sudoku.database.dao.PuzzleDao.PuzzleUpdate.Description
import fr.uha.hassenforder.easy.sudoku.database.dao.PuzzleDao.PuzzleUpdate.Level
import fr.uha.hassenforder.easy.sudoku.dto.FullPuzzle
import fr.uha.hassenforder.easy.sudoku.model.Cell
import fr.uha.hassenforder.easy.sudoku.model.Puzzle
import fr.uha.hassenforder.easy.sudoku.model.PuzzleLevel
import fr.uha.hassenforder.easy.sudoku.repository.PuzzleRepository
import fr.uha.hassenforder.easy.sudoku.validation.PuzzleValidationErrors
import fr.uha.hassenforder.easy.sudoku.validation.PuzzleValidator
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
class PuzzleViewModel @Inject constructor (
    private val repository: PuzzleRepository,
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
    private val _puzzle: StateFlow<Result<FullPuzzle>> = _id
        .flatMapLatest { id -> repository.getPuzzleById(id) }
        .map { puzzle ->
            if (puzzle != null) {
                Result.Success(content = puzzle)
            } else {
                Result.Error()
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Result.Loading())

    @Immutable
    data class UIState(
        val uiTitle : UITitle,
        val puzzle : FullPuzzle,
        val validation : PuzzleValidationErrors,
    ) {
        companion object {
            fun create(
                uiTitle: UITitle,
                puzzle: FullPuzzle,
            ): UIState {
                val validation = PuzzleValidator(puzzle).validateAll()
                return UIState(uiTitle, puzzle, validation)
            }
        }
    }

    val uiState : StateFlow<Result<UIState>> = combine (_uiTitle, _puzzle)
        { t, p, ->
            when (p) {
                is Result.Success -> Result.Success(content = UIState.create(t, p.content))
                is Result.Error -> Result.Error(t)
                is Result.Loading -> Result.Loading(t)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Result.Loading())

    sealed class UIEvent {
        data object OnBack: UIEvent()
        data class DescriptionChanged(val newValue: String): UIEvent()
        data class LevelChanged(val newValue: PuzzleLevel): UIEvent()
        data class CellChanged(val newValue: Cell): UIEvent()
    }

    private fun replaceCell (list : List<Cell>, cell : Cell) : List<Cell> {
        return list.filter { it.cid != cell.cid } + cell
    }

    private fun validate (oldPuzzle : FullPuzzle, uiEvent: UIEvent) : Boolean {
        val newPuzzle : FullPuzzle = when (uiEvent) {
            is UIEvent.DescriptionChanged -> oldPuzzle.copy(puzzle = oldPuzzle.puzzle.copy(description = uiEvent.newValue))
            is UIEvent.LevelChanged -> oldPuzzle.copy(puzzle = oldPuzzle.puzzle.copy(level = uiEvent.newValue))
            is UIEvent.CellChanged -> oldPuzzle.copy(cells = replaceCell(oldPuzzle.cells, uiEvent.newValue))
            is UIEvent.OnBack -> oldPuzzle
        }
        val validation = PuzzleValidator(newPuzzle).validateAll()
        val isValid = ! validation.hasError()
        return isValid
    }

    private suspend fun update (puzzle: FullPuzzle, uiEvent: UIEvent) {
        when (uiEvent) {
            is UIEvent.DescriptionChanged -> repository.update(Description(pid = puzzle.puzzle.pid, description = uiEvent.newValue))
            is UIEvent.LevelChanged -> repository.update(Level(pid = puzzle.puzzle.pid, level = uiEvent.newValue))
            is UIEvent.CellChanged -> repository.update (CellContent(cell = uiEvent.newValue))
            is UIEvent.OnBack -> {}
        }
    }

    fun send (uiEvent : UIEvent) = viewModelScope.launch {
        if (uiState.value !is Result.Success) return@launch
        val puzzle : FullPuzzle = (uiState.value as Result.Success<UIState>).content.puzzle
        when (uiEvent) {
            is UIEvent.OnBack -> navigator.navigateUp()
            else -> {
                update(puzzle, uiEvent)
                val isValid = validate(puzzle, uiEvent)
                repository.valid(puzzle.puzzle.pid, isValid)
            }
        }
    }

    fun edit (pid : Long) = viewModelScope.launch {
        val pid : Long = repository.edit(pid)
        _id.value = pid
    }

    fun create(puzzle: Puzzle) = viewModelScope.launch {
        val pid : Long = repository.create(puzzle)
        _id.value = pid
    }

}

package fr.uha.hassenforder.easy.sudoku.validation

import androidx.compose.runtime.Immutable
import fr.uha.hassenforder.easy.sudoku.R
import fr.uha.hassenforder.easy.sudoku.dto.FullPuzzle
import fr.uha.hassenforder.easy.sudoku.model.Cell
import fr.uha.hassenforder.easy.sudoku.model.PuzzleLevel

@Immutable
data class PuzzleValidationErrors(
    val description: Int? = null,
    val size: Int? = null,
    val level: Int? = null,
    val cells: Int? = null,
) {
    fun hasError() : Boolean {
        if (description != null) return true
        if (size != null) return true
        if (level != null) return true
        if (cells != null) return true
        return false
    }
}

class PuzzleValidator(
    private val puzzle: FullPuzzle
) {

    private fun validateDescription(newValue: String) : Int? {
        return when {
            newValue.isEmpty()  ->  R.string.value_empty
            newValue.isBlank()  ->  R.string.value_blank
            newValue.length < 3 ->  R.string.value_too_short
            newValue.length > 32 ->  R.string.value_too_long
            else -> null
        }
    }

    private fun validateSize(newValue: Int) : Int? {
        if (newValue == 9) return null
        return R.string.size_must_be_9
    }

    private fun validateLevel(newValue: PuzzleLevel?) : Int? {
        return when {
            newValue == null ->  R.string.level_must_set
            else -> null
        }
    }

    fun validateCells(newValue: List<Cell>) : Int? {
        val size = newValue.size
        if (size != puzzle.puzzle.size * puzzle.puzzle.size)
            return R.string.size_must_be_squared
        return null
    }

    fun validateAll(): PuzzleValidationErrors {
        return PuzzleValidationErrors(
            description = validateDescription(puzzle.puzzle.description),
            level = validateLevel(puzzle.puzzle.level),
            size = validateSize(puzzle.puzzle.size),
            cells = validateCells(puzzle.cells),
        )
    }

}

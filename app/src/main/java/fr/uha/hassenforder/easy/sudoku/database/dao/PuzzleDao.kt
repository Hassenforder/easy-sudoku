package fr.uha.hassenforder.easy.sudoku.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import fr.uha.hassenforder.easy.android.database.dao.SmartDao
import fr.uha.hassenforder.easy.android.database.model.ManagementState
import fr.uha.hassenforder.easy.sudoku.dto.FullPuzzle
import fr.uha.hassenforder.easy.sudoku.dto.PuzzleWithDetails
import fr.uha.hassenforder.easy.sudoku.model.Cell
import fr.uha.hassenforder.easy.sudoku.model.Puzzle
import fr.uha.hassenforder.easy.sudoku.model.PuzzleLevel
import kotlinx.coroutines.flow.Flow

@Dao
abstract class PuzzleDao : SmartDao<Puzzle>() {

    sealed class PuzzleUpdate {
        data class Description(val pid : Long, val description: String) : PuzzleUpdate()
        data class Size(val pid : Long, val size : Int) : PuzzleUpdate()
        data class Level(val pid : Long, val level : PuzzleLevel) : PuzzleUpdate()
        data class State(val pid : Long, val managementState : ManagementState) : PuzzleUpdate()
        data class CellContent(val cell : Cell) : PuzzleUpdate()
    }

    @Query("SELECT *  FROM puzzles")
    abstract fun getAll(): Flow<List<Puzzle>>

    @Transaction
    @Query("SELECT P.* " +
            ", (SELECT COUNT(*) FROM games G WHERE G.pid = P.pid AND G.state = 'STARTED') AS startedCount " +
            ", (SELECT COUNT(*) FROM games G WHERE G.pid = P.pid AND G.state = 'LOST') AS lostCount " +
            ", (SELECT COUNT(*) FROM games G WHERE G.pid = P.pid AND G.state = 'WON') AS wonCount " +
            "FROM puzzles AS P " +
            "ORDER BY P.puzzleUpdatedDate DESC "
    )
    abstract fun getAllWithDetails(): Flow<List<PuzzleWithDetails>>

    @Transaction
    @Query("SELECT P.* " +
            ", (SELECT COUNT(*) FROM games G WHERE G.pid = P.pid AND G.state = 'STARTED') AS startedCount " +
            ", (SELECT COUNT(*) FROM games G WHERE G.pid = P.pid AND G.state = 'LOST') AS lostCount " +
            ", (SELECT COUNT(*) FROM games G WHERE G.pid = P.pid AND G.state = 'WON') AS wonCount " +
            "FROM puzzles AS P " +
            "WHERE P.managementState = 'VALID' OR P.managementState = 'INVALID' " +
            "ORDER BY P.puzzleUpdatedDate DESC "
    )
    abstract fun getFilteredWithDetails(): Flow<List<PuzzleWithDetails>>

    @Transaction
    @Query("SELECT P.* " +
            "FROM puzzles AS P " +
            "WHERE P.pid = :id " +
            "ORDER BY P.puzzleCreatedDate DESC "
    )
    abstract fun getById(id: Long): Flow<FullPuzzle?>

    @Update(entity = Puzzle::class)
    abstract suspend fun update(content : PuzzleUpdate.Description)

    @Update(entity = Puzzle::class)
    abstract suspend fun update(content : PuzzleUpdate.Size)

    @Update(entity = Puzzle::class)
    abstract suspend fun update(content : PuzzleUpdate.Level)

    @Update(entity = Puzzle::class)
    abstract suspend fun update(content : PuzzleUpdate.State)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    abstract suspend fun createCell(cell : Cell): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    abstract suspend fun createCells(cells : List<Cell>): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun updateCell(cell : Cell): Long

}

package fr.uha.hassenforder.easy.sudoku.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import fr.uha.hassenforder.easy.android.database.dao.SmartDao
import fr.uha.hassenforder.easy.sudoku.dto.FullGame
import fr.uha.hassenforder.easy.sudoku.dto.PuzzleWithGames
import fr.uha.hassenforder.easy.sudoku.model.Cell
import fr.uha.hassenforder.easy.sudoku.model.Game
import fr.uha.hassenforder.easy.sudoku.model.GameState
import kotlinx.coroutines.flow.Flow

@Dao
abstract class GameDao : SmartDao<Game>() {

    sealed class GameUpdate {
        data class State(val gid : Long, val state : GameState) : GameUpdate()
    }

    @Transaction
    @Query("SELECT P.* " +
            "FROM puzzles AS P " +
            "WHERE P.managementState = 'VALID' " +
            "ORDER BY P.puzzleUpdatedDate DESC "
    )
    abstract fun getAll(): Flow<List<PuzzleWithGames>>

    @Transaction
    @Query("SELECT P.* " +
            "FROM puzzles AS P " +
            "JOIN games AS G ON G.pid = P.pid AND G.state != 'WON' " +
            "WHERE P.managementState = 'VALID' " +
            "ORDER BY P.puzzleUpdatedDate DESC "
    )
    abstract fun getAllNotWon(): Flow<List<PuzzleWithGames>>

    @Transaction
    @Query("SELECT P.* " +
            "FROM puzzles AS P " +
            "JOIN games AS G ON G.pid = P.pid AND G.state == 'STARTED' " +
            "WHERE P.managementState = 'VALID' " +
            "ORDER BY P.puzzleUpdatedDate DESC "
    )
    abstract fun getAllNotFinished(): Flow<List<PuzzleWithGames>>

    @Transaction
    @Query("SELECT P.* " +
            "FROM puzzles AS P " +
            "JOIN games AS G ON G.pid = P.pid AND G.pid IS NULL " +
            "WHERE P.managementState = 'VALID' " +
            "ORDER BY P.puzzleUpdatedDate DESC "
    )
    abstract fun getAllNotStarted(): Flow<List<PuzzleWithGames>>

    @Transaction
    @Query("SELECT G.* " +
            "FROM games AS G " +
            "WHERE G.gid = :id "
    )
    abstract fun getById(id: Long): Flow<FullGame?>

    @Update(entity = Game::class)
    abstract suspend fun update(content : GameUpdate.State)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    abstract suspend fun createCell(cell : Cell): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    abstract suspend fun createCells(cells : List<Cell>): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun updateCells(cells : List<Cell>): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun updateCell(cell : Cell): Long

}

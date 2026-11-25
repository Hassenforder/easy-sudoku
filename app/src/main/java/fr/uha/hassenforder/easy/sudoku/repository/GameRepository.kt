package fr.uha.hassenforder.easy.sudoku.repository

import android.content.Context
import androidx.annotation.WorkerThread
import fr.uha.hassenforder.easy.sudoku.database.dao.GameDao
import fr.uha.hassenforder.easy.sudoku.dto.FullGame
import fr.uha.hassenforder.easy.sudoku.dto.FullPuzzle
import fr.uha.hassenforder.easy.sudoku.dto.PuzzleWithGames
import fr.uha.hassenforder.easy.sudoku.model.Cell
import fr.uha.hassenforder.easy.sudoku.model.CellState
import fr.uha.hassenforder.easy.sudoku.model.Game
import fr.uha.hassenforder.easy.sudoku.model.GameState
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext


class GameRepository(
    private val context: Context,
    private val dispatcher: CoroutineDispatcher,
    private val gameDao: GameDao,
) {

    enum class ListMode {
        ALL,
        NOT_WON,
        NOT_FINISHED,
        NOT_STARTED
    }

    fun getAllFiltered(mode : ListMode): Flow<List<PuzzleWithGames>> {
        return when (mode) {
            ListMode.ALL -> gameDao.getAll()
            ListMode.NOT_WON -> gameDao.getAllNotWon()
            ListMode.NOT_FINISHED -> gameDao.getAllNotFinished()
            ListMode.NOT_STARTED -> gameDao.getAllNotStarted()
        }
    }

    fun getGameById(id: Long): Flow<FullGame?> {
        return gameDao.getById(id)
    }

    @WorkerThread
    suspend fun update(content : GameDao.GameUpdate) = withContext(dispatcher) {
        when (content) {
            is GameDao.GameUpdate.State -> gameDao.update(content)
        }
    }

    @WorkerThread
    suspend fun create(puzzle: PuzzleWithGames): Long = withContext(dispatcher) {
        val game = Game(gid=0, pid=puzzle.puzzle.pid, state = GameState.STARTED)
        val gid : Long = gameDao.create(game)
        for (x in 0 until puzzle.puzzle.size) {
            for (y in 0 until puzzle.puzzle.size) {
                gameDao.createCell (Cell(0, gid, x, y, 0, CellState.GUESS))
            }
        }
        gid
    }

    @WorkerThread
    suspend fun erase(game: FullGame) = withContext(dispatcher) {
        gameDao.update(GameDao.GameUpdate.State(gid = game.game.gid, state = GameState.STARTED))
        gameDao.updateCells(game.gameCells.map { it.copy(value = 0)})
    }

    @WorkerThread
    suspend fun clone(game: FullGame): Long = withContext(dispatcher) {
        val gid : Long = gameDao.create(Game(gid = 0, pid = game.puzzle.pid, state = game.game.state))
        gameDao.createCells(game.gameCells.map { it.copy(cid = 0, pid = gid)})
        return@withContext gid
    }

    @WorkerThread
    suspend fun load(gid : Long): Long = gid

    @WorkerThread
    suspend fun deleteAll(puzzle: PuzzleWithGames) = gameDao.delete(puzzle.games)

    @WorkerThread
    suspend fun delete(game : Game) = gameDao.delete(game)

    @WorkerThread
    suspend fun updateCell(cell : Cell): Long = withContext(dispatcher) {
        gameDao.updateCell (cell)
    }

}

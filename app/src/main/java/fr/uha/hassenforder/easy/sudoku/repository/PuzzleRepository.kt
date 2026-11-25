package fr.uha.hassenforder.easy.sudoku.repository

import android.content.Context
import androidx.annotation.WorkerThread
import fr.uha.hassenforder.easy.android.database.model.ListMode
import fr.uha.hassenforder.easy.android.database.model.ManagementState
import fr.uha.hassenforder.easy.sudoku.database.dao.PuzzleDao
import fr.uha.hassenforder.easy.sudoku.database.dao.PuzzleDao.PuzzleUpdate
import fr.uha.hassenforder.easy.sudoku.dto.FullPuzzle
import fr.uha.hassenforder.easy.sudoku.dto.PuzzleWithDetails
import fr.uha.hassenforder.easy.sudoku.model.Cell
import fr.uha.hassenforder.easy.sudoku.model.CellState
import fr.uha.hassenforder.easy.sudoku.model.Puzzle
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


class PuzzleRepository(
    private val context: Context,
    private val dispatcher: CoroutineDispatcher,
    private val puzzleDao: PuzzleDao,
) {

    fun getAll(): Flow<List<Puzzle>> {
        return puzzleDao.getAll()
    }

    fun getAllWithDetails(): Flow<List<PuzzleWithDetails>> {
        return puzzleDao.getAllWithDetails()
    }

    fun getFilteredWithDetails(mode : ListMode): Flow<List<PuzzleWithDetails>> {
        return puzzleDao.getFilteredWithDetails()
    }

    fun getPuzzleById(id: Long): Flow<FullPuzzle?> {
        return puzzleDao.getById(id)
    }

    @WorkerThread
    suspend fun update(content : PuzzleUpdate) = withContext(dispatcher) {
        when (content) {
            is PuzzleUpdate.Description -> puzzleDao.update(content)
            is PuzzleUpdate.Size -> puzzleDao.update(content)
            is PuzzleUpdate.Level -> puzzleDao.update(content)
            is PuzzleUpdate.State -> puzzleDao.update(content)
            is PuzzleUpdate.CellContent -> puzzleDao.updateCell (content.cell)
        }
    }

    @WorkerThread
    suspend fun valid(pid: Long, valid: Boolean) = withContext(dispatcher) {
        puzzleDao.update(PuzzleUpdate.State(pid, if (valid) ManagementState.VALID else ManagementState.INVALID ))
    }

    @WorkerThread
    suspend fun create(puzzle: Puzzle): Long = withContext(dispatcher) {
        val pid : Long = puzzleDao.create(puzzle)
        for (x in 0 until puzzle.size) {
            for (y in 0 until puzzle.size) {
                puzzleDao.createCell (Cell(0, pid, x, y, 0, CellState.INITIAL))
            }
        }
        pid
    }

    @WorkerThread
    suspend fun create(puzzle: FullPuzzle): Long = withContext(dispatcher) {
        val pid : Long = puzzleDao.create(puzzle.puzzle)
        puzzleDao.createCells(puzzle.cells.map { it.copy(pid = pid)})
        return@withContext pid
    }

    @WorkerThread
    suspend fun clone(initial: Puzzle): Long = withContext(dispatcher) {
        val puzzle : FullPuzzle? = puzzleDao.getById(initial.pid).first()
        if (puzzle == null) return@withContext 0
        val pid = puzzleDao.create(puzzle.puzzle.copy(pid = 0))
        for (cell in puzzle.cells) {
            puzzleDao.createCell(cell.copy(pid = pid))
        }
        pid
    }

    @WorkerThread
    suspend fun edit(pid : Long): Long = pid

    @WorkerThread
    suspend fun delete(puzzle: Puzzle) = puzzleDao.delete(puzzle)

    @WorkerThread
    suspend fun update(puzzle: Puzzle): Long = puzzleDao.upsert(puzzle)

    fun installTessDataFile(
        tessDataFolder: String,
        tessDataFile: String,
    ) : String {
        val folder = File(context.filesDir, tessDataFolder)
        if (!folder.exists() && !folder.mkdir()) {
            throw RuntimeException("Can't create directory $folder")
        }
        val targetFile = File(folder, tessDataFile)
        if (!targetFile.exists()) {
            try {
                context.assets.open(tessDataFile).use { input ->
                    FileOutputStream(targetFile).use { out ->
                        val buffer = ByteArray(1024)
                        var read: Int
                        while ((input.read(buffer).also { read = it }) != -1) {
                            out.write(buffer, 0, read)
                        }
                    }
                }
            } catch (e: IOException) {
                throw RuntimeException("Can't copy asset file ")
            }
        }
        return context.filesDir.absolutePath
    }

}

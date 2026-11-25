package fr.uha.hassenforder.easy.sudoku.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import fr.uha.hassenforder.easy.android.database.DatabaseTypeConverters
import fr.uha.hassenforder.easy.sudoku.database.dao.GameDao
import fr.uha.hassenforder.easy.sudoku.database.dao.PuzzleDao
import fr.uha.hassenforder.easy.sudoku.model.Puzzle
import fr.uha.hassenforder.easy.sudoku.model.Cell
import fr.uha.hassenforder.easy.sudoku.model.Game

@TypeConverters(DatabaseTypeConverters::class)
@Database(
    entities = [
        Puzzle::class,
        Cell::class,
        Game::class,
    ],
    version = 1,
    exportSchema = true
)
abstract class SudokuDatabase : RoomDatabase() {

    abstract fun getPuzzleDao () : PuzzleDao

    abstract fun getGameDao () : GameDao

    companion object {
        private lateinit var instance : SudokuDatabase

        @Synchronized
        fun create (context : Context) : SudokuDatabase {
            instance = Room.databaseBuilder(context, SudokuDatabase::class.java, "sudoku.db").build()
            return instance
        }

        @Synchronized
        fun get () : SudokuDatabase {
            return instance
        }
    }

}

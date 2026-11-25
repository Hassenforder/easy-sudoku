package fr.uha.hassenforder.easy.sudoku.model

import androidx.compose.runtime.Immutable
import androidx.room.Entity
import androidx.room.PrimaryKey
import fr.uha.hassenforder.easy.android.database.model.ManagementState
import fr.uha.hassenforder.easy.android.database.model.Timable
import java.time.Instant

@Entity(tableName = "puzzles")
@Immutable
data class Puzzle (
    @PrimaryKey(autoGenerate = true)
    val pid : Long = 0,
    val description: String = "",
    val size : Int = 0,
    val level: PuzzleLevel = PuzzleLevel.NO,
    val managementState: ManagementState = ManagementState.CREATED,
    var puzzleCreatedDate : Instant? = null,
    var puzzleUpdatedDate : Instant? = null,
) : Timable {

    override fun getCreatedDate(): Instant? {
        return puzzleCreatedDate
    }

    override fun setCreatedDate(date: Instant) {
        puzzleCreatedDate = date
    }

    override fun getUpdatedDate(): Instant? {
        return puzzleUpdatedDate
    }

    override fun setUpdatedDate(date: Instant) {
        puzzleUpdatedDate = date
    }

}
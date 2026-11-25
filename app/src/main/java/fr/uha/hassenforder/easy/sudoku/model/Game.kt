package fr.uha.hassenforder.easy.sudoku.model

import androidx.compose.runtime.Immutable
import androidx.room.Entity
import androidx.room.PrimaryKey
import fr.uha.hassenforder.easy.android.database.model.Timable
import java.time.Instant

@Entity(tableName = "games")
@Immutable
data class Game (
    @PrimaryKey(autoGenerate = true)
    val gid : Long = 0,
    val pid : Long = 0,
    val state : GameState = GameState.STARTED,
    var gameCreatedDate : Instant? = null,
    var gameUpdatedDate : Instant? = null,
) : Timable {

    override fun getCreatedDate(): Instant? {
        return gameCreatedDate
    }

    override fun setCreatedDate(date: Instant) {
        gameCreatedDate = date
    }

    override fun getUpdatedDate(): Instant? {
        return gameUpdatedDate
    }

    override fun setUpdatedDate(date: Instant) {
        gameUpdatedDate = date
    }

}
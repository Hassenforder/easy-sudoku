package fr.uha.hassenforder.easy.sudoku.ui.field

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.AutoMode
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.uha.hassenforder.easy.android.icons.outlined.Eraser
import fr.uha.hassenforder.easy.android.ui.DecoratorProperties
import fr.uha.hassenforder.easy.android.ui.field.OutlinedDecorator
import fr.uha.hassenforder.easy.sudoku.R
import fr.uha.hassenforder.easy.sudoku.model.Cell
import fr.uha.hassenforder.easy.sudoku.model.CellState

enum class GridAction {
    EDIT,
    HELPER,
    ERASE_ALL,
    CLONE,
    SUGGEST,
    VALID,

    AUTO_VALID,
}

data class PuzzleProperties (
    val count : Int = 0,
)

private val cellSize : Int = 36
private val fontSize : Int = 32

private val values = listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 0)

private val actions = listOf(
    GridAction.EDIT,
    GridAction.HELPER,
    GridAction.CLONE,
    GridAction.ERASE_ALL,
    GridAction.SUGGEST,
    GridAction.VALID,
    GridAction.AUTO_VALID
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun InternalPuzzleField (
    values : List<Cell>,
    helpers : Map<Int, List<Int>>,
    selected : Cell?,
    onSelected : (cell : Cell) -> Unit,
    actionChecked : List<GridAction>,
    count : Int,
    modifier : Modifier = Modifier,
) {
    val lightenInversePrimary = MaterialTheme.colorScheme.inversePrimary.copy(alpha = 0.4f)
    val colorLine = MaterialTheme.colorScheme.onBackground
    LazyVerticalGrid(
        modifier = modifier
            .height((count*cellSize).dp)
            .width((count*cellSize).dp)
            .drawBehind {
                val cellPX = cellSize.dp.toPx()
                val widthPX = cellPX * count
                val heightPX = cellPX * count
                for (i in 0 .. count) {
                    val strokeWidth = if (i % 3 == 0) 4.dp.toPx() else 2.dp.toPx()
                    drawLine(
                        color = colorLine,
                        start = Offset(0f, i*cellPX),
                        end = Offset(widthPX, i*cellPX),
                        strokeWidth = strokeWidth
                    )
                    drawLine(
                        color = colorLine,
                        start = Offset(i*cellPX, 0f),
                        end = Offset( i*cellPX, heightPX),
                        strokeWidth = strokeWidth
                    )
                }
            }
        ,
        columns = GridCells.Fixed(count),
        verticalArrangement = Arrangement.spacedBy(0.dp, Alignment.Top),
        horizontalArrangement = Arrangement.spacedBy(0.dp, Alignment.CenterHorizontally)
    ) {
        items(
            items = values,
            key = { it.key }
        ) { item ->
            val background = when {
                selected == null -> Color.Transparent
                item.key == selected.key -> MaterialTheme.colorScheme.inversePrimary
                selected.value == 0 -> Color.Transparent
                item.value == selected.value -> lightenInversePrimary
                else -> Color.Transparent
            }
            Box(
                modifier = Modifier
                    .size(cellSize.dp)
                    .background(background)
                    .clickable( onClick = { onSelected(item) } )
                ,
                contentAlignment = Alignment.Center
            ) {
                if (actionChecked.contains(GridAction.HELPER) && item.value == 0) {
                    val allowed = helpers.get(item.key)
                    val text = if (allowed == null) "" else {
                        val tmp = StringBuilder()
                        (1..9).forEach {
                            if (allowed.contains(it)) tmp.append(it)
                            else tmp.append("  ")
                            if (it % 3 == 0) tmp.append("\n")
                            else tmp.append(" ")
                        }
                        tmp.toString()
                    }
                    Text(text, fontSize = (fontSize / 3).sp, lineHeight= (fontSize / 3).sp, maxLines = 3)
                } else if (item.value != 0) {
                    val foreground : Color = when (item.state) {
                        CellState.INITIAL -> Color.Black
                        CellState.GUESS -> Color.Blue
                        CellState.ERROR -> Color.Red
                        CellState.SUGGESTED -> Color.Magenta
                    }
                    Text(item.value.toString(), color = foreground, fontSize = fontSize.sp)
                }
            }
        }
    }

}

@Composable
private fun InternalActionField (
    actions : List<GridAction>,
    checked : List<GridAction>,
    onSelected : (gridAction : GridAction) -> Unit,
    modifier : Modifier = Modifier,
) {
    val colorLine = MaterialTheme.colorScheme.onBackground
    LazyHorizontalGrid (
        modifier = modifier
            .height(((actions.size)*cellSize).dp)
            .width((cellSize).dp)
            .drawBehind {
                val cellPX = cellSize.dp.toPx()
                val widthInPX = cellPX
                val heightInPX = cellPX * actions.size
                val internalStrokeWidth = 2.dp.toPx()
                (1 until actions.size).forEach {
                    drawLine(
                        color = colorLine,
                        start = Offset(0f, it*cellPX),
                        end = Offset(widthInPX, it*cellPX),
                        strokeWidth = internalStrokeWidth
                    )
                }
                val externalStrokeWidth = 4.dp.toPx()
                drawRect(
                    color = colorLine,
                    topLeft = Offset.Zero,
                    size = Size( widthInPX, heightInPX),
                    style = Stroke(externalStrokeWidth)
                )
            }
        ,
        rows = GridCells.Fixed(actions.size),
        verticalArrangement = Arrangement.spacedBy(0.dp, Alignment.Top),
        horizontalArrangement = Arrangement.spacedBy(0.dp, Alignment.CenterHorizontally)
    ) {
        items(
            items = actions,
        ) { action ->
            val background = if (checked.contains(action)) MaterialTheme.colorScheme.inversePrimary else Color.Transparent
            Box(
                modifier = Modifier
                    .size(cellSize.dp)
                    .background(background)
                    .clickable( onClick = { onSelected(action) } )
                ,
                contentAlignment = Alignment.Center
            ) {
                val icon = when (action) {
                    GridAction.EDIT -> Icons.Filled.Edit
                    GridAction.HELPER -> Icons.AutoMirrored.Filled.Help
                    GridAction.SUGGEST -> Icons.Filled.Search
                    GridAction.VALID -> Icons.Filled.Check
                    GridAction.AUTO_VALID -> Icons.Filled.AutoMode
                    GridAction.ERASE_ALL -> Icons.Outlined.Eraser
                    GridAction.CLONE -> Icons.Filled.ContentCopy
                }
                Icon(icon, contentDescription = action.name)
            }
        }
    }

}

@Composable
private fun InternalPaletteField (
    valueCounts : Map<Int, Int>,
    onSelected : (value : Int) -> Unit,
    count : Int,
    modifier : Modifier = Modifier,
) {
    val colorLine = MaterialTheme.colorScheme.onBackground
    LazyVerticalGrid (
        modifier = modifier
            .height((cellSize).dp)
            .width(((count+1)*cellSize).dp)
            .drawBehind {
                val cellPX = cellSize.dp.toPx()
                val widthInPX = cellPX * (count + 1)
                val heightInPX = cellPX
                val internalStrokeWidth = 2.dp.toPx()
                for (i in 1 .. count) {
                    drawLine(
                        color = colorLine,
                        start = Offset(i*cellPX, 0f),
                        end = Offset( i*cellPX, heightInPX),
                        strokeWidth = internalStrokeWidth
                    )
                }
                val externalStrokeWidth = 4.dp.toPx()
                drawRect(
                    color = colorLine,
                    topLeft = Offset.Zero,
                    size = Size( widthInPX, heightInPX),
                    style = Stroke(externalStrokeWidth)
                )
            }
        ,
        columns = GridCells.Fixed(count+1),
        verticalArrangement = Arrangement.spacedBy(0.dp, Alignment.Top),
        horizontalArrangement = Arrangement.spacedBy(0.dp, Alignment.CenterHorizontally)
    ) {
        items(
            items = values.filter { it <= count },
        ) { value ->
            val background = Color.Transparent
            Box(
                modifier = Modifier
                    .size(cellSize.dp)
                    .background(background)
                    .clickable( onClick = { onSelected(value) } )
                ,
                contentAlignment = Alignment.Center
            ) {
                if (value != 0) {
                    val valueCount = valueCounts[value] ?: 0
                    when {
                        valueCount < count ->
                            Text(value.toString(), fontSize = fontSize.sp)
                        valueCount == count ->
                            Icon(Icons.Outlined.Check, contentDescription = null)
                        valueCount > count ->
                            Text(value.toString(), color = Color.Red, fontSize = fontSize.sp)
                    }
                } else {
                    Icon(Icons.Outlined.Eraser, contentDescription = null)
                }
            }
        }
    }

}

@Composable
private fun InternalEmpty (
) {
    Text (stringResource(id = R.string.empty), color = Color.Red)
}

@Composable
fun PuzzleBuilderField(
    values : List<Cell>?,
    onChange: (cell : Cell, value : Int) -> Unit,
    config : PuzzleProperties,
    modifier : Modifier = Modifier,
    decorator: DecoratorProperties = DecoratorProperties(),
) {
    OutlinedDecorator(
        modifier = modifier,
        decorator = decorator,
    ) {
        if (config.count != 0 && values != null) {
            val valueCounts = remember(values) { values.groupingBy { it.value }.eachCount() }
            val selectedCell : MutableState<Cell?> = remember { mutableStateOf(null) }
            InternalPuzzleField(
                values = values,
                helpers = emptyMap(),
                selected =  selectedCell.value,
                onSelected = { selectedCell.value = it; },
                actionChecked = listOf(GridAction.EDIT),
                count = config.count
            )
            Spacer(modifier = Modifier.height(16.dp))
            InternalPaletteField(valueCounts, onSelected = { value -> selectedCell.value?.let { selectedCell.value = it.copy(value = value) ; onChange(it, value) } }, config.count)
            Spacer(modifier = Modifier.height(16.dp))
        } else {
            InternalEmpty()
        }
    }
}

@Composable
fun PuzzlePlayField(
    values : List<Cell>?,
    helpers : Map<Int, List<Int>>,
    onChange: (cell : Cell, value : Int) -> Unit,
    actionChecked : List<GridAction>,
    onAction: (action : GridAction) -> Unit,
    config : PuzzleProperties,
    modifier : Modifier = Modifier,
    decorator: DecoratorProperties = DecoratorProperties(),
) {
    OutlinedDecorator(
        modifier = modifier,
        decorator = decorator,
    ) {
        if (config.count != 0 && values != null) {
            val valueCounts = remember(values) { values.groupingBy { it.value }.eachCount() }
            val selectedCell : MutableState<Cell?> = remember { mutableStateOf(null) }
            Row () {
                InternalPuzzleField(
                    values, helpers,
                    selectedCell.value,
                    onSelected = { selectedCell.value = it; },
                    actionChecked,
                    config.count
                )
                Spacer(modifier = Modifier.weight(1f))
                InternalActionField(actions = actions, checked = actionChecked, onSelected = { onAction(it) })
                Spacer(modifier = Modifier.width(16.dp))
            }
            Spacer(modifier = Modifier.height(16.dp))
            InternalPaletteField(valueCounts, onSelected = { value -> selectedCell.value?.let { selectedCell.value = it.copy(value = value) ; onChange(it, value) } }, config.count)
            Spacer(modifier = Modifier.height(16.dp))
        } else {
            InternalEmpty()
        }
    }
}

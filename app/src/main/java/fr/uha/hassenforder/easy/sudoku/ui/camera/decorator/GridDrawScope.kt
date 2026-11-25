package fr.uha.hassenforder.easy.sudoku.ui.camera.decorator

import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.layout.ContentScale

@DslMarker
annotation class DecoratorDrawScopeScopeMarker

@DecoratorDrawScopeScopeMarker
interface GridDrawScope : DrawScope {
    /*
     * xOffset is the offset of the surface from the left edge of the screen
     * yOffset is the offset of the surface from the top edge of the screen
     * padding is the internal top and left padding of the grid in the surface
     * width is the width of the grid : cellCount * cellSize
     * height is the height of the grid : cellCount * cellSize
     *
     * top = yOffset + padding
     * left = xOffset + padding
     * right = xOffset + padding + width
     * bottom = yOffset + padding + height
     */
    val innerRect : Rect
    val outerRect : Rect
    val cellSize : Float
    val cellCount : Int
}

// Internal implementation of the scope
private class GridDrawScopeImpl(
    private val drawScope: DrawScope,
    override val innerRect: Rect,
    override val outerRect: Rect,
    override val cellSize: Float,
    override val cellCount: Int,
) : GridDrawScope, DrawScope by drawScope

private fun guessGridSize (viewSize : Size) : Size {
    val viewAspectRatio = viewSize.width / viewSize.height
    val previewAspectRatio = 4f / 3f
    val gridWidth: Float
    val gridHeight: Float
    if (viewAspectRatio > previewAspectRatio) {
        gridHeight = viewSize.height
        gridWidth = gridHeight / previewAspectRatio
    } else {
        gridWidth = viewSize.width
        gridHeight = gridWidth * previewAspectRatio
    }
    return Size(gridWidth, gridHeight)
}

/*
 * function to guess the xOffset of the grid.
 * the view must not be scaled by compose due to lack of space
 * you must give enough space in width and height
 * as counterexample : in a custom dialog the height was smaller than needed. Compose will add extra white strips on the side.
 * It is not possible to take those into account
 * tested for registered entries
 */
private fun guessXOffset (alignment : Alignment, viewSize : Size, gridSize : Size) : Float {
    return when (alignment) {
        Alignment.TopCenter -> {
            // xOffset is what it should be
            (viewSize.width - gridSize.width) / 2
        }
        else -> {
            // no study yet
            0f
        }
    }
}

/*
 * function to guess the xOffset of the grid.
 * the view must not be scaled by compose due to lack of space
 * you must give enough space in width and height
 * as counterexample : in a custom dialog the height was smaller than needed. Compose will add extra white strips on the side.
 * It is not possible to take those into account
 * tested for registered entries
 */
private fun guessYOffset (alignment : Alignment, viewSize : Size, gridSize : Size) : Float {
    return when (alignment) {
        Alignment.TopCenter -> {
            // yOffset based on several screenshots
            // guessed after regression on an excel sheet
            //    + it seems that the picture will have the width as height
            //    + the width*ratio will be the height
            //    + as the picture is not height enough black strip on top and bottom (near half remaining size)
            (gridSize.height - gridSize.width) / 2
        }
        else -> {
            0f
        }
    }
}

fun DrawScope.computeGridDrawScope (
    alignment : Alignment,
    contentScale : ContentScale,
    cellCount : Int
) : GridDrawScope {

    val gridSize = guessGridSize(size)
    var xOffset = guessXOffset(alignment, size, gridSize)
    var yOffset = guessYOffset(alignment, size, gridSize)

    // add an additional offset half cellSize before and after grid
    val cellSize = minOf(gridSize.width, gridSize.height) / (cellCount + 1)
    val offset = cellSize / 2
    val innerRect = Rect(xOffset + offset, yOffset + offset, xOffset + offset + cellSize * cellCount, yOffset + offset + cellSize * cellCount)
    val outerRect = Rect(xOffset, yOffset, xOffset + gridSize.width, gridSize.height - yOffset)
    return GridDrawScopeImpl(this, innerRect, outerRect, cellSize, cellCount)
}

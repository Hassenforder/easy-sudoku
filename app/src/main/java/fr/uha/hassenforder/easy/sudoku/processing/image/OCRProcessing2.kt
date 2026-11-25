package fr.uha.hassenforder.easy.sudoku.processing.image

import android.graphics.Bitmap
import android.graphics.Rect
import com.googlecode.tesseract.android.TessBaseAPI
import kotlin.collections.forEach
import kotlin.math.min
import kotlin.math.max

class OCRProcessing2 (
    private val tessApi: TessBaseAPI
) {
    private fun preProcess (bitmap : Bitmap) : List<Rect> {
        tessApi.setImage(bitmap)
        tessApi.setPageSegMode(TessBaseAPI.PageSegMode.PSM_SINGLE_CHAR)
        val result: MutableList<Rect> = mutableListOf()
        val w = bitmap.width
        val h = bitmap.height
        val cellSize = min (w, h) / 10
        val boardMargin = cellSize * 50 / 100
        val cellMargin = cellSize * 25 / 100
        val boardStart = boardMargin
        val boardSize = cellSize * 9
        (0 ..< 9).forEach { x ->
            (0 ..< 9).forEach { y ->
                val cellLeft = boardStart + x * cellSize
                val cellTop = boardStart + y * cellSize
                val rect = Rect(
                    max(0, cellLeft - cellMargin),
                    max(0, cellTop - cellMargin),
                    min(w, cellLeft + cellSize + cellMargin),
                    min(h, cellTop +  cellSize + cellMargin),
                )
                result.add(rect)
            }
        }
        return result
    }

    private fun applyOCROnBoxes (boxes : List<Rect>) : List<BoxedText> {
        val result: MutableList<BoxedText> = mutableListOf()
        for (box in boxes) {
            tessApi.setRectangle(box)
            val text: String = tessApi.getUTF8Text()
            if (text.isEmpty()) continue
            if (text.isBlank()) continue
            if (text == "oo") continue
            //            if (! pattern.matcher(text).matches()) continue;
            result.add(BoxedText(box, text, 0.0))
        }
        return result
    }

    private fun postProcess (rawBoxedText: List<BoxedText>) : List<BoxedText> {
        return rawBoxedText
    }

    fun process(bitmap: Bitmap): List<BoxedText> {
        val boxes = preProcess(bitmap)
        val rawBoxedText = applyOCROnBoxes(boxes)
        val bestBoxedText = postProcess(rawBoxedText)
        return bestBoxedText
    }

}
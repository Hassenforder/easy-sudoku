package fr.uha.hassenforder.easy.sudoku.processing.image

import android.graphics.Bitmap
import android.graphics.Rect
import com.googlecode.tesseract.android.TessBaseAPI

class OCRProcessing (
    private val tessApi: TessBaseAPI
) {
    private fun preProcess (bitmap : Bitmap, houghLines: Lines) : List<Rect> {
        tessApi.setImage(bitmap)
        tessApi.setPageSegMode(TessBaseAPI.PageSegMode.PSM_SINGLE_CHAR)
        val processor = LineProcessor(0.0, 0.0, 9)
        val boxes = processor.buildBoxes(houghLines)
        return boxes
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

    fun process(bitmap: Bitmap, houghLines: Lines): List<BoxedText> {
        val boxes = preProcess(bitmap, houghLines)
        val rawBoxedText = applyOCROnBoxes(boxes)
        val bestBoxedText = postProcess(rawBoxedText)
        return bestBoxedText
    }

}
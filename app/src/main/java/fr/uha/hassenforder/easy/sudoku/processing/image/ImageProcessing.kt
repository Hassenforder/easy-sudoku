package fr.uha.hassenforder.easy.sudoku.processing.image

import android.graphics.Bitmap
import android.util.Log
import org.opencv.android.Utils
import org.opencv.core.Core
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.MatOfPoint
import org.opencv.core.MatOfPoint2f
import org.opencv.core.Point
import org.opencv.core.Size
import org.opencv.core.times
import org.opencv.geometry.Geometry.approxPolyDP
import org.opencv.geometry.Geometry.arcLength
import org.opencv.geometry.Geometry.contourArea
import org.opencv.geometry.Geometry.getPerspectiveTransform
import org.opencv.imgproc.Imgproc
import org.opencv.imgproc.Imgproc.CHAIN_APPROX_SIMPLE
import org.opencv.imgproc.Imgproc.RETR_TREE
import org.opencv.imgproc.Imgproc.findContours
import org.opencv.imgproc.Imgproc.warpPerspective

class ImageProcessing (
    val exportedBitmap: List<ExportedBitmap> = emptyList()
) {
    var intermediateBitmaps = mutableMapOf<ExportedBitmap, Bitmap>()

    private fun exportMat (mat : Mat) : Bitmap {
        val bmp = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(mat, bmp)
        return bmp
    }

    private fun preProcessAsBilateral (bitmap : Bitmap) : Mat {
        val originalMat = Mat()
        val bmp32: Bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, false)
        Utils.bitmapToMat(bmp32, originalMat)
        if (originalMat.empty()) {
            Log.d("ImageProcessing", "Error opening image!")
        }
        if (exportedBitmap.contains(ExportedBitmap.original))
            intermediateBitmaps[ExportedBitmap.original] = exportMat(originalMat)
        val grayMat = Mat()
        Imgproc.cvtColor(originalMat, grayMat,Imgproc.COLOR_RGBA2GRAY)
        if (exportedBitmap.contains(ExportedBitmap.gray))
            intermediateBitmaps[ExportedBitmap.gray] = exportMat(grayMat)
        val denoisedMat = Mat()
        Imgproc.bilateralFilter(grayMat, denoisedMat, 15, 75.0, 75.0)
        if (exportedBitmap.contains(ExportedBitmap.denoised))
            intermediateBitmaps[ExportedBitmap.denoised] = exportMat(denoisedMat)
/*
        val binaryMat = Mat()
        Imgproc.threshold(denoisedMat, binaryMat, threshold, 255.0, Imgproc.THRESH_BINARY_INV)
        if (exportedBitmap.contains(ExportedBitmap.binary))
            intermediateBitmaps[ExportedBitmap.binary] = exportMat(binaryMat)
        return binaryMat
 */
        return denoisedMat
    }
/*
    private fun preProcessAsErodeDilate (bitmap : Bitmap) : Mat {
        val originalMat = Mat()
        val bmp32: Bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, false)
        Utils.bitmapToMat(bmp32, originalMat)
        if (originalMat.empty()) {
            Log.d("Hough", "Error opening image!")
        }
        if (exportedBitmap.contains(ExportedBitmap.original))
            intermediateBitmaps[ExportedBitmap.original] = exportMat(originalMat)
        val grayMat = Mat()
        Imgproc.cvtColor(originalMat, grayMat,Imgproc.COLOR_RGBA2GRAY)
        Imgproc.adaptiveThreshold(
            grayMat,
            grayMat,
            255.0,
            Imgproc.ADAPTIVE_THRESH_MEAN_C,
            Imgproc.THRESH_BINARY,
            7,
            5.0
        )
        if (exportedBitmap.contains(ExportedBitmap.gray))
            intermediateBitmaps[ExportedBitmap.gray] = exportMat(grayMat)
        val kernel = Imgproc.getStructuringElement(Imgproc.MORPH_ERODE, Size(2.0, 2.0))
        Imgproc.erode(grayMat, grayMat, kernel)
        if (exportedBitmap.contains(ExportedBitmap.erode))
            intermediateBitmaps[ExportedBitmap.erode] = exportMat(grayMat)
        val kernelDil = Imgproc.getStructuringElement(Imgproc.MORPH_DILATE, Size(2.0, 2.0))
        Imgproc.dilate(grayMat, grayMat, kernelDil)
        if (exportedBitmap.contains(ExportedBitmap.dilate))
            intermediateBitmaps[ExportedBitmap.dilate] = exportMat(grayMat)
        Core.bitwise_not(grayMat, grayMat)
        if (exportedBitmap.contains(ExportedBitmap.binary))
            intermediateBitmaps[ExportedBitmap.binary] = exportMat(grayMat)
        return grayMat
    }

    private fun preProcess (bitmap : Bitmap) : Mat {
        return when (preProcessor) {
            PreProcessor.bilateral -> preProcessAsBilateral(bitmap)
            PreProcessor.erode_dilate -> preProcessAsErodeDilate(bitmap)
        }
    }

    private fun applyHoughTransform (binaryMat : Mat) : Mat {
        val oneDegree = Math.PI / 180
        val fiveDegrees = 5 * oneDegree
        val linesMat = Mat()
        Imgproc.HoughLinesP(binaryMat, linesMat, 1.0, oneDegree, 80, 30.0, 10.0)
        return linesMat
    }

    private fun postProcess (width : Int, height : Int, linesMat : Mat) : Lines {
        val processor = LineProcessor(width.toDouble(), height.toDouble(), 9)
        val initialHoughLines = processor.convert(linesMat)
        val reducedHoughLines = processor.reduce(initialHoughLines)
        return reducedHoughLines
    }
*/

    private fun findLargestContour(processingMat: Mat): MatOfPoint {
        val hierarchy = Mat()
        val contourList = mutableListOf<MatOfPoint>()
        findContours(
            processingMat,
            contourList,
            hierarchy,
            RETR_TREE,
            CHAIN_APPROX_SIMPLE
        )
        return contourList.maxBy { contourArea(it) }
    }

    private fun findCornerListOfContour(largestContour: MatOfPoint): List<Point> {
        val src = MatOfPoint2f()
        val approxPoly = MatOfPoint2f()
        largestContour.convertTo(src, CvType.CV_32FC2)
        val arcLength = arcLength(src, true)
        approxPolyDP(src, approxPoly, 0.02 * arcLength, true)
        return approxPoly.toList()
    }

    private fun createPerspectiveMat(originalMat: Mat, largestContourCornerList: List<Point>): Mat {
        val perspectiveMat = Mat()
        val perspectiveSrc = largestContourCornerList.run { MatOfPoint2f(topLeft, topRight, bottomLeft, bottomRight) }
        val perspectiveDst = MatOfPoint2f(Point(0.0, 0.0), Point(BOARD_SIZE_IN_PX, 0.0), Point(0.0, BOARD_SIZE_IN_PX), Point(BOARD_SIZE_IN_PX, BOARD_SIZE_IN_PX))
        val perspectiveTransform = getPerspectiveTransform(perspectiveSrc, perspectiveDst)

        warpPerspective(
            originalMat,
            perspectiveMat,
            perspectiveTransform,
            Size(BOARD_SIZE_IN_PX, BOARD_SIZE_IN_PX)
        )

        return perspectiveMat
    }

    fun process(bitmap : Bitmap): Bitmap {
        val denoisedMap = preProcessAsBilateral(bitmap)
        val largestContour = findLargestContour(denoisedMap)
        val largestContourCornerList = findCornerListOfContour(largestContour)
        val perspectiveMat = createPerspectiveMat(denoisedMap, largestContourCornerList)
        if (exportedBitmap.contains(ExportedBitmap.binary))
            intermediateBitmaps[ExportedBitmap.binary] = exportMat(perspectiveMat)
        return exportMat(perspectiveMat)
    }

    companion object {

        private const val BOARD_SIZE_IN_PX = 9 * 111.0

        private val List<Point>.topLeft: Point
            get() = sortedBy { it.x }.take(2).minBy { it.y }

        private val List<Point>.bottomLeft: Point
            get() = sortedBy { it.x }.take(2).maxBy { it.y }

        private val List<Point>.topRight: Point
            get() = sortedByDescending { it.x }.take(2).minBy { it.y }

        private val List<Point>.bottomRight: Point
            get() = sortedByDescending { it.x }.take(2).maxBy { it.y }

    }

}
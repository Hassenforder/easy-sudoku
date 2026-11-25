package fr.uha.hassenforder.easy.sudoku.processing.image

import java.util.Locale
import kotlin.math.abs

data class Line (
    val startX: Double,
    val endX: Double,

    val startY: Double,
    val endY: Double,

) {
    enum class Orientation {
        OTHER,
        HORIZONTAL,
        VERTICAL,
    }

    constructor(data: DoubleArray) :
            this(data[0], data[2], data[1], data[3])
    {
        if (straightThreshold == 0) throw RuntimeException("straight Threshold is not set")
        if (quantizerLevel == 0) throw RuntimeException("level of quantizer is not set")
    }

    val key: String by lazy {
        val tmp = StringBuilder()
        var k = "?"
        var v = 0.0
        when (this.orientation) {
            Orientation.VERTICAL -> {
                k = "V"
                v = this.x
            }
            Orientation.HORIZONTAL -> {
                k = "H"
                v = this.y
            }
            Orientation.OTHER -> {
                k = "?"
                v = 0.0
            }
        }
        tmp.append(k)
        tmp.append(
            String.format(
                Locale.ENGLISH,
                "%04d",
                normalize(v).toInt()
            )
        )
        tmp.toString()
    }

    val orientation: Orientation by lazy {
        when {
            this.isHorizontal() ->
                Orientation.HORIZONTAL
            this.isVertical() ->
                Orientation.VERTICAL
            else ->
                Orientation.OTHER
        }
    }

    val x: Double by lazy {
        if (this.orientation != Orientation.VERTICAL) throw Error(
            "not vertical"
        )
        abs(endX + startX) / 2
    }

    val y: Double by lazy {
        if (this.orientation != Orientation.HORIZONTAL) throw Error(
            "not horizontal"
        )
        abs(endY + startY) / 2
    }

    fun squaredLength(): Double {
        val dx = endX - startX
        val dy = endY - startY
        return dx * dx + dy * dy
    }

    override fun toString(): String {
        val tmp = StringBuilder()
        when (this.orientation) {
            Orientation.VERTICAL -> {
                tmp.append("X=")
                tmp.append(this.x.toInt())
                tmp.append(" [ ")
                tmp.append(startY.toInt())
                tmp.append(" - ")
                tmp.append(endY.toInt())
                tmp.append(" ]")
            }
            Orientation.HORIZONTAL -> {
                tmp.append("Y=")
                tmp.append(this.y.toInt())
                tmp.append(" [ ")
                tmp.append(startX.toInt())
                tmp.append(" - ")
                tmp.append(endX.toInt())
                tmp.append(" ]")
            }
            Orientation.OTHER -> {
                tmp.append(startX.toInt())
                tmp.append(" ")
                tmp.append(startY.toInt())
                tmp.append(" - ")
                tmp.append(endX.toInt())
                tmp.append(" ")
                tmp.append(endY.toInt())
            }
        }
        return tmp.toString()
    }

    private fun isVertical(): Boolean {
        val dx = abs(endX - startX)
        if (dx < straightThreshold) return true
        return false
    }

    private fun isHorizontal(): Boolean {
        val dy = abs(endY - startY)
        if (dy < straightThreshold) return true
        return false
    }

    companion object {
        fun normalize(value: Double): Double {
            return (((value.toInt() / quantizerLevel).toFloat()) * quantizerLevel).toDouble()
        }

        private var straightThreshold = 0
        private var quantizerLevel = 0

        fun setStraightThreshold(straightThreshold: Int) {
            Companion.straightThreshold = straightThreshold
        }

        fun setQuantizerLevel(quantizerLevel: Int) {
            Companion.quantizerLevel = quantizerLevel
        }
    }
}

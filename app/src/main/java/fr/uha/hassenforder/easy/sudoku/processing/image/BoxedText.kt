package fr.uha.hassenforder.easy.sudoku.processing.image

import android.graphics.Rect
import android.util.Pair

data class BoxedText (
    val rect: Rect,
    val text: String? = null,
    val confidence: Double = 0.0,
    val textAndConfidences: MutableList<Pair<String, Double>>? = null,
) {
    constructor(rect: Rect, text: String, confidence: Double)
            : this(rect, text, confidence, null) { }

    constructor(rect: Rect, textAndConfidences: MutableList<Pair<String, Double>>)
            : this(rect, null, 0.0, textAndConfidences) { }

    fun isEmpty() : Boolean {
        if (text != null) {
            if (text.isEmpty()) return true
            if (text.isBlank()) return true
            return false
        }
        if (textAndConfidences != null) {
            if (textAndConfidences.isEmpty()) return true
            if (textAndConfidences[0].first!!.isEmpty()) return true
            if (textAndConfidences[0].first!!.isBlank()) return true
            return false
        }
        return true
    }

    fun getBestText(defaultText: String?): String? {
        if (text != null) return text
        if (textAndConfidences == null) return defaultText
        if (textAndConfidences.isEmpty()) return defaultText
        return textAndConfidences[0].first
    }

    override fun toString(): String {
        val tmp = StringBuilder()
        tmp.append(rect.left)
        tmp.append(" ")
        tmp.append(rect.top)
        tmp.append(" - ")
        tmp.append(rect.right)
        tmp.append(" ")
        tmp.append(rect.bottom)
        tmp.append(" : ")
        if (text != null) {
            tmp.append("'")
            tmp.append(text)
            tmp.append("'")
            tmp.append(" / ")
            tmp.append(confidence.toInt())
        } else if (textAndConfidences == null) {
            tmp.append("null")
        } else if (textAndConfidences.isEmpty()) {
            tmp.append("empty")
        } else {
            var first = true
            for (choice in textAndConfidences) {
                if (first) first = false
                else tmp.append(", ")
                tmp.append("'")
                tmp.append(choice.first)
                tmp.append("'")
                tmp.append(" / ")
                tmp.append(choice.second)
            }
        }
        return tmp.toString()
    }

    companion object {
        fun isNullEmptyOrBlank(text: String?): Boolean {
            if (text == null) return true
            if (text.isEmpty()) return true
            if (text.isBlank()) return true
            return false
        }
    }
}

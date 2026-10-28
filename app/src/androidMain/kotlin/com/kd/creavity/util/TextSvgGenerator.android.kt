package com.kd.creavity.util

import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import android.os.Build

actual object TextSvgGenerator {

    actual fun generateSvgData(text: String, fontName: String, fontSize: Float): String? {
        if (text.isBlank()) return null
        return try {
            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                textSize = fontSize
                typeface = when (fontName.lowercase()) {
                    "serif", "merriweather", "georgia" -> Typeface.SERIF
                    "monospace", "mono", "jetbrains mono" -> Typeface.MONOSPACE
                    "cursive", "handwriting", "script", "caveat", "kalam" -> Typeface.create("cursive", Typeface.NORMAL)
                    "google sans flex", "sans-serif", "sans" -> Typeface.SANS_SERIF
                    else -> Typeface.create(fontName, Typeface.NORMAL)
                }
            }

            val path = Path()
            paint.getTextPath(text, 0, text.length, 0f, fontSize, path)
            val bounds = RectF()
            path.computeBounds(bounds, true)

            // Offset path so bounds start at (0, 0)
            path.offset(-bounds.left, -bounds.top)

            if (Build.VERSION.SDK_INT >= 34) {
                val iterator = path.pathIterator
                val points = FloatArray(8)
                val sb = StringBuilder()
                while (iterator.hasNext()) {
                    val verb = iterator.next(points, 0)
                    when (verb) {
                        android.graphics.PathIterator.VERB_MOVE ->
                            sb.append("M ").append(points[0]).append(" ").append(points[1]).append(" ")
                        android.graphics.PathIterator.VERB_LINE ->
                            sb.append("L ").append(points[2]).append(" ").append(points[3]).append(" ")
                        android.graphics.PathIterator.VERB_QUAD ->
                            sb.append("Q ").append(points[2]).append(" ").append(points[3]).append(" ")
                                .append(points[4]).append(" ").append(points[5]).append(" ")
                        android.graphics.PathIterator.VERB_CUBIC ->
                            sb.append("C ").append(points[2]).append(" ").append(points[3]).append(" ")
                                .append(points[4]).append(" ").append(points[5]).append(" ")
                                .append(points[6]).append(" ").append(points[7]).append(" ")
                        android.graphics.PathIterator.VERB_CLOSE ->
                            sb.append("Z ")
                    }
                }
                val result = sb.toString().trim()
                if (result.isNotBlank()) result else null
            } else {
                null
            }
        } catch (e: Throwable) {
            null
        }
    }

    actual fun isFontAvailable(fontName: String): Boolean {
        val standard = listOf("google sans flex", "sans-serif", "sans", "serif", "monospace", "mono", "cursive", "handwriting", "script")
        return standard.any { it.equals(fontName, ignoreCase = true) }
    }
}

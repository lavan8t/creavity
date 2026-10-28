package com.kd.creavity.util

import java.awt.Font
import java.awt.GraphicsEnvironment
import java.awt.font.FontRenderContext
import java.awt.geom.AffineTransform
import java.awt.geom.PathIterator

actual object TextSvgGenerator {

    actual fun generateSvgData(text: String, fontName: String, fontSize: Float): String? {
        if (text.isBlank()) return null
        return try {
            val awtFont = when (fontName.lowercase()) {
                "serif", "merriweather", "georgia" -> Font(Font.SERIF, Font.PLAIN, fontSize.toInt().coerceAtLeast(1))
                "monospace", "mono", "jetbrains mono" -> Font(Font.MONOSPACED, Font.PLAIN, fontSize.toInt().coerceAtLeast(1))
                "cursive", "handwriting", "script", "caveat", "kalam" -> {
                    val available = GraphicsEnvironment.getLocalGraphicsEnvironment().availableFontFamilyNames
                    val matched = available.firstOrNull {
                        it.contains("caveat", ignoreCase = true) ||
                        it.contains("script", ignoreCase = true) ||
                        it.contains("cursive", ignoreCase = true) ||
                        it.contains("comic", ignoreCase = true)
                    }
                    if (matched != null) Font(matched, Font.PLAIN, fontSize.toInt().coerceAtLeast(1))
                    else Font(Font.SANS_SERIF, Font.ITALIC, fontSize.toInt().coerceAtLeast(1))
                }
                "google sans flex", "sans-serif", "sans" -> Font(Font.SANS_SERIF, Font.PLAIN, fontSize.toInt().coerceAtLeast(1))
                else -> Font(fontName, Font.PLAIN, fontSize.toInt().coerceAtLeast(1))
            }

            val frc = FontRenderContext(null, true, true)
            val gv = awtFont.createGlyphVector(frc, text)
            val outline = gv.outline
            val bounds = outline.bounds2D

            // Normalize so top-left bounds start at (0, 0)
            val tx = AffineTransform.getTranslateInstance(-bounds.minX, -bounds.minY)
            val normalizedShape = tx.createTransformedShape(outline)
            val pi = normalizedShape.getPathIterator(null)
            val coords = FloatArray(6)
            val sb = StringBuilder()

            while (!pi.isDone) {
                when (pi.currentSegment(coords)) {
                    PathIterator.SEG_MOVETO ->
                        sb.append("M ").append(coords[0]).append(" ").append(coords[1]).append(" ")
                    PathIterator.SEG_LINETO ->
                        sb.append("L ").append(coords[0]).append(" ").append(coords[1]).append(" ")
                    PathIterator.SEG_QUADTO ->
                        sb.append("Q ").append(coords[0]).append(" ").append(coords[1]).append(" ")
                            .append(coords[2]).append(" ").append(coords[3]).append(" ")
                    PathIterator.SEG_CUBICTO ->
                        sb.append("C ").append(coords[0]).append(" ").append(coords[1]).append(" ")
                            .append(coords[2]).append(" ").append(coords[3]).append(" ")
                            .append(coords[4]).append(" ").append(coords[5]).append(" ")
                    PathIterator.SEG_CLOSE ->
                        sb.append("Z ")
                }
                pi.next()
            }

            val result = sb.toString().trim()
            if (result.isNotBlank()) result else null
        } catch (e: Throwable) {
            null
        }
    }

    actual fun isFontAvailable(fontName: String): Boolean {
        val standard = listOf("google sans flex", "sans-serif", "sans", "serif", "monospace", "mono", "cursive", "handwriting", "script")
        if (standard.any { it.equals(fontName, ignoreCase = true) }) return true
        return try {
            val ge = GraphicsEnvironment.getLocalGraphicsEnvironment()
            ge.availableFontFamilyNames.any { it.equals(fontName, ignoreCase = true) }
        } catch (e: Throwable) {
            false
        }
    }
}

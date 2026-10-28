package com.kd.creavity.presentation.canvas

import com.kd.creavity.domain.model.DrawingTool
import com.kd.creavity.domain.model.FillStyle
import com.kd.creavity.domain.model.Point
import com.kd.creavity.domain.model.StickyNote
import com.kd.creavity.domain.model.Stroke
import com.kd.creavity.domain.model.TextElement
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

data class ExportBounds(
    val minX: Float,
    val minY: Float,
    val maxX: Float,
    val maxY: Float
) {
    val width: Float get() = (maxX - minX).coerceAtLeast(100f)
    val height: Float get() = (maxY - minY).coerceAtLeast(100f)
}

object CanvasExportManager {

    fun computeBounds(
        strokes: List<Stroke>,
        texts: List<TextElement>,
        stickyNotes: List<StickyNote>,
        padding: Float = 40f
    ): ExportBounds {
        var minX = Float.MAX_VALUE
        var minY = Float.MAX_VALUE
        var maxX = Float.MIN_VALUE
        var maxY = Float.MIN_VALUE

        strokes.forEach { stroke ->
            if (stroke.points.isNotEmpty() && !stroke.isEraser) {
                stroke.points.forEach { pt ->
                    minX = min(minX, pt.x)
                    minY = min(minY, pt.y)
                    maxX = max(maxX, pt.x)
                    maxY = max(maxY, pt.y)
                }
            }
        }

        texts.forEach { t ->
            minX = min(minX, t.x)
            minY = min(minY, t.y)
            maxX = max(maxX, t.x + 200f)
            maxY = max(maxY, t.y + 60f)
        }

        stickyNotes.forEach { n ->
            minX = min(minX, n.x)
            minY = min(minY, n.y)
            maxX = max(maxX, n.x + n.width)
            maxY = max(maxY, n.y + n.height)
        }

        if (minX == Float.MAX_VALUE) {
            return ExportBounds(0f, 0f, 1920f, 1080f)
        }

        return ExportBounds(
            minX = minX - padding,
            minY = minY - padding,
            maxX = maxX + padding,
            maxY = maxY + padding
        )
    }

    fun exportToSvg(
        strokes: List<Stroke>,
        texts: List<TextElement> = emptyList(),
        stickyNotes: List<StickyNote> = emptyList(),
        isTransparentBg: Boolean = false,
        backgroundColor: Long = 0xFFFFFFFF
    ): String {
        val bounds = computeBounds(strokes, texts, stickyNotes)
        val sb = StringBuilder()

        val bgHex = String.format("#%06X", 0xFFFFFF and backgroundColor.toInt())

        sb.append("""<svg xmlns="http://www.w3.org/2000/svg" viewBox="${bounds.minX} ${bounds.minY} ${bounds.width} ${bounds.height}" width="${bounds.width}" height="${bounds.height}">""").append("\n")

        if (!isTransparentBg) {
            sb.append("""  <rect x="${bounds.minX}" y="${bounds.minY}" width="${bounds.width}" height="${bounds.height}" fill="$bgHex" />""").append("\n")
        }

        // Render Strokes
        strokes.forEach { stroke ->
            if (stroke.points.size < 2 || stroke.isEraser) return@forEach
            val colorHex = String.format("#%06X", 0xFFFFFF and stroke.color.toInt())
            val sw = stroke.strokeWidth
            val opacity = when (stroke.tool) {
                DrawingTool.HIGHLIGHTER -> "0.35"
                else -> "1.0"
            }

            when (stroke.tool) {
                DrawingTool.RECTANGLE -> {
                    val p1 = stroke.points.first()
                    val p2 = stroke.points.last()
                    val left = min(p1.x, p2.x)
                    val top = min(p1.y, p2.y)
                    val w = (max(p1.x, p2.x) - left).coerceAtLeast(1f)
                    val h = (max(p1.y, p2.y) - top).coerceAtLeast(1f)
                    val fill = when (stroke.fillStyle) {
                        FillStyle.SOLID -> colorHex
                        FillStyle.SEMI_TRANSPARENT -> colorHex
                        FillStyle.HACHURE, FillStyle.TRANSPARENT -> "none"
                    }
                    val fillOpacity = if (stroke.fillStyle == FillStyle.SEMI_TRANSPARENT) "0.22" else opacity
                    sb.append("""  <rect x="$left" y="$top" width="$w" height="$h" rx="6" fill="$fill" fill-opacity="$fillOpacity" stroke="$colorHex" stroke-width="$sw" stroke-linejoin="round" />""").append("\n")
                }
                DrawingTool.CIRCLE -> {
                    val p1 = stroke.points.first()
                    val p2 = stroke.points.last()
                    val cx = (p1.x + p2.x) / 2f
                    val cy = (p1.y + p2.y) / 2f
                    val rx = kotlin.math.abs(p2.x - p1.x) / 2f
                    val ry = kotlin.math.abs(p2.y - p1.y) / 2f
                    val fill = when (stroke.fillStyle) {
                        FillStyle.SOLID -> colorHex
                        FillStyle.SEMI_TRANSPARENT -> colorHex
                        FillStyle.HACHURE, FillStyle.TRANSPARENT -> "none"
                    }
                    val fillOpacity = if (stroke.fillStyle == FillStyle.SEMI_TRANSPARENT) "0.22" else opacity
                    sb.append("""  <ellipse cx="$cx" cy="$cy" rx="$rx" ry="$ry" fill="$fill" fill-opacity="$fillOpacity" stroke="$colorHex" stroke-width="$sw" />""").append("\n")
                }
                DrawingTool.DIAMOND -> {
                    val p1 = stroke.points.first()
                    val p2 = stroke.points.last()
                    val left = min(p1.x, p2.x)
                    val top = min(p1.y, p2.y)
                    val right = max(p1.x, p2.x)
                    val bottom = max(p1.y, p2.y)
                    val midX = (left + right) / 2f
                    val midY = (top + bottom) / 2f
                    val d = "M $midX $top L $right $midY L $midX $bottom L $left $midY Z"
                    val fill = when (stroke.fillStyle) {
                        FillStyle.SOLID -> colorHex
                        FillStyle.SEMI_TRANSPARENT -> colorHex
                        FillStyle.HACHURE, FillStyle.TRANSPARENT -> "none"
                    }
                    val fillOpacity = if (stroke.fillStyle == FillStyle.SEMI_TRANSPARENT) "0.22" else opacity
                    sb.append("""  <path d="$d" fill="$fill" fill-opacity="$fillOpacity" stroke="$colorHex" stroke-width="$sw" stroke-linejoin="round" />""").append("\n")
                }
                DrawingTool.STAR -> {
                    val p1 = stroke.points.first()
                    val p2 = stroke.points.last()
                    val left = min(p1.x, p2.x)
                    val top = min(p1.y, p2.y)
                    val right = max(p1.x, p2.x)
                    val bottom = max(p1.y, p2.y)
                    val cx = (left + right) / 2f
                    val cy = (top + bottom) / 2f
                    val rOuter = min(right - left, bottom - top) / 2f
                    val rInner = rOuter * 0.40f
                    val step = Math.PI / 5.0
                    val starPoints = (0 until 10).map { i ->
                        val r = if (i % 2 == 0) rOuter else rInner
                        val angle = -Math.PI / 2.0 + i * step
                        val x = (cx + r * cos(angle)).toFloat()
                        val y = (cy + r * sin(angle)).toFloat()
                        "$x,$y"
                    }.joinToString(" ")
                    val fill = when (stroke.fillStyle) {
                        FillStyle.SOLID -> colorHex
                        FillStyle.SEMI_TRANSPARENT -> colorHex
                        FillStyle.HACHURE, FillStyle.TRANSPARENT -> "none"
                    }
                    val fillOpacity = if (stroke.fillStyle == FillStyle.SEMI_TRANSPARENT) "0.22" else opacity
                    sb.append("""  <polygon points="$starPoints" fill="$fill" fill-opacity="$fillOpacity" stroke="$colorHex" stroke-width="$sw" stroke-linejoin="round" />""").append("\n")
                }
                DrawingTool.ARROW -> {
                    val start = stroke.points.first()
                    val end = stroke.points.last()
                    val d = "M ${start.x} ${start.y} L ${end.x} ${end.y}"
                    sb.append("""  <path d="$d" fill="none" stroke="$colorHex" stroke-width="$sw" stroke-linecap="round" />""").append("\n")
                }
                else -> {
                    val d = buildSvgPathData(stroke.points)
                    sb.append("""  <path d="$d" fill="none" stroke="$colorHex" stroke-width="$sw" stroke-linecap="round" stroke-linejoin="round" opacity="$opacity" />""").append("\n")
                }
            }

            // In-shape embedded text export
            if (!stroke.embeddedText.isNullOrBlank() && stroke.points.size >= 2) {
                val minX = stroke.points.minOf { it.x }
                val maxX = stroke.points.maxOf { it.x }
                val minY = stroke.points.minOf { it.y }
                val maxY = stroke.points.maxOf { it.y }
                val centerX = (minX + maxX) / 2f
                val centerY = (minY + maxY) / 2f + 5f
                val txtColorHex = if (stroke.embeddedTextColor != 0L) String.format("#%06X", 0xFFFFFF and stroke.embeddedTextColor.toInt()) else "#1C1B1F"
                sb.append("""  <text x="$centerX" y="$centerY" font-size="16" font-weight="500" fill="$txtColorHex" font-family="sans-serif" text-anchor="middle" dominant-baseline="middle">${escapeXml(stroke.embeddedText)}</text>""").append("\n")
            }
        }

        // Render Sticky Notes
        stickyNotes.forEach { note ->
            val noteHex = String.format("#%06X", 0xFFFFFF and note.color.toInt())
            sb.append("""  <rect x="${note.x}" y="${note.y}" width="${note.width}" height="${note.height}" rx="16" fill="$noteHex" />""").append("\n")
            if (note.text.isNotBlank()) {
                val lines = note.text.lines()
                var yOffset = note.y + 36f
                lines.forEach { line ->
                    if (yOffset < note.y + note.height - 12f) {
                        sb.append("""  <text x="${note.x + 16}" y="$yOffset" font-size="18" font-weight="500" fill="#1C1B1F" font-family="sans-serif">${escapeXml(line)}</text>""").append("\n")
                        yOffset += 24f
                    }
                }
            }
        }

        // Render Text Elements
        texts.forEach { txt ->
            val colorHex = String.format("#%06X", 0xFFFFFF and txt.color.toInt())
            if (!txt.svgData.isNullOrBlank()) {
                val cleanPath = if (txt.svgData.startsWith("<")) {
                    Regex("""\bd=["']([^"']+)["']""").find(txt.svgData)?.groupValues?.get(1) ?: txt.svgData
                } else txt.svgData
                sb.append("""  <g transform="translate(${txt.x}, ${txt.y})">""").append("\n")
                sb.append("""    <path d="$cleanPath" fill="$colorHex" />""").append("\n")
                sb.append("""  </g>""").append("\n")
            } else {
                sb.append("""  <text x="${txt.x}" y="${txt.y + txt.fontSize}" font-size="${txt.fontSize}" fill="$colorHex" font-family="${escapeXml(txt.fontName)}">${escapeXml(txt.text)}</text>""").append("\n")
            }
        }

        sb.append("</svg>\n")
        return sb.toString()
    }

    private fun buildSvgPathData(points: List<Point>): String {
        if (points.isEmpty()) return ""
        val sb = StringBuilder()
        sb.append("M ${points[0].x} ${points[0].y}")
        for (i in 1 until points.size) {
            val p0 = points[i - 1]
            val p1 = points[i]
            val midX = (p0.x + p1.x) / 2f
            val midY = (p0.y + p1.y) / 2f
            sb.append(" Q ${p0.x} ${p0.y} $midX $midY")
        }
        sb.append(" L ${points.last().x} ${points.last().y}")
        return sb.toString()
    }

    fun escapeXml(str: String): String {
        return str.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;")
    }
}

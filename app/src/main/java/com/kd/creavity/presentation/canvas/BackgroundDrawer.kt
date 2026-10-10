package com.kd.creavity.presentation.canvas

import android.graphics.Paint
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import kotlin.math.floor

enum class CanvasBackgroundPattern {
    DOTS,
    LINES,
    ISOMETRIC_DOTS,
    BLANK
}

object BackgroundDrawer {
    private const val DOT_SPACING = 48f
    private const val LINE_SPACING = 50f
    private const val LINE_WIDTH = 1.2f

    private val dotPaint = Paint().apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        isAntiAlias = true
    }

    private val linePaint = Paint().apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.BUTT
        isAntiAlias = false
    }

    private var ptsCache = FloatArray(8192)

    fun DrawScope.drawCanvasPattern(
        pattern: CanvasBackgroundPattern,
        backgroundColor: Color,
        zoom: Float,
        panOffset: Offset
    ) {
        when (pattern) {
            CanvasBackgroundPattern.DOTS -> drawDots(backgroundColor, zoom, panOffset)
            CanvasBackgroundPattern.LINES -> drawLines(backgroundColor, zoom, panOffset)
            CanvasBackgroundPattern.ISOMETRIC_DOTS -> drawIsometricDots(backgroundColor, zoom, panOffset)
            CanvasBackgroundPattern.BLANK -> { /* No pattern */ }
        }
    }

    private fun DrawScope.drawDots(
        backgroundColor: Color,
        zoom: Float,
        panOffset: Offset
    ) {
        var spacing = DOT_SPACING
        while (spacing * zoom < 18f) {
            spacing *= 2f
        }

        val dotColor = if (backgroundColor.luminance() < 0.5f) 0x24FFFFFF else 0x1A000000
        val strokeW = (2.2f / zoom).coerceAtLeast(1.2f)

        dotPaint.color = dotColor
        dotPaint.strokeWidth = strokeW

        val startX = -panOffset.x / zoom
        val startY = -panOffset.y / zoom
        val endX = startX + size.width / zoom
        val endY = startY + size.height / zoom

        val gridStartX = floor(startX / spacing) * spacing
        val gridStartY = floor(startY / spacing) * spacing

        var idx = 0
        var y = gridStartY
        val native = drawContext.canvas.nativeCanvas

        while (y <= endY) {
            var x = gridStartX
            while (x <= endX) {
                if (idx + 2 > ptsCache.size) {
                    native.drawPoints(ptsCache, 0, idx, dotPaint)
                    idx = 0
                }
                ptsCache[idx++] = x
                ptsCache[idx++] = y
                x += spacing
            }
            y += spacing
        }

        if (idx > 0) {
            native.drawPoints(ptsCache, 0, idx, dotPaint)
        }
    }

    private fun DrawScope.drawLines(
        backgroundColor: Color,
        zoom: Float,
        panOffset: Offset
    ) {
        var spacing = LINE_SPACING
        while (spacing * zoom < 20f) {
            spacing *= 2f
        }

        val lineColor = if (backgroundColor.luminance() < 0.5f) 0x18FFFFFF else 0x12000000
        val strokeW = (LINE_WIDTH / zoom).coerceAtLeast(0.8f)

        linePaint.color = lineColor
        linePaint.strokeWidth = strokeW

        val startX = -panOffset.x / zoom
        val startY = -panOffset.y / zoom
        val endX = startX + size.width / zoom
        val endY = startY + size.height / zoom

        val gridStartY = floor(startY / spacing) * spacing

        var idx = 0
        var y = gridStartY
        val native = drawContext.canvas.nativeCanvas

        while (y <= endY) {
            if (idx + 4 > ptsCache.size) {
                native.drawLines(ptsCache, 0, idx, linePaint)
                idx = 0
            }
            ptsCache[idx++] = startX
            ptsCache[idx++] = y
            ptsCache[idx++] = endX
            ptsCache[idx++] = y
            y += spacing
        }

        if (idx > 0) {
            native.drawLines(ptsCache, 0, idx, linePaint)
        }
    }

    private fun DrawScope.drawIsometricDots(
        backgroundColor: Color,
        zoom: Float,
        panOffset: Offset
    ) {
        var dotSpacing = 42f
        while (dotSpacing * zoom < 18f) {
            dotSpacing *= 2f
        }
        val rowHeight = dotSpacing * 0.8660254f

        val dotColor = if (backgroundColor.luminance() < 0.5f) 0x22FFFFFF else 0x16000000
        val strokeW = (2.0f / zoom).coerceAtLeast(1.0f)

        dotPaint.color = dotColor
        dotPaint.strokeWidth = strokeW

        val leftBound = (-panOffset.x / zoom) - dotSpacing
        val topBound = (-panOffset.y / zoom) - dotSpacing
        val rightBound = (size.width - panOffset.x) / zoom + dotSpacing
        val bottomBound = (size.height - panOffset.y) / zoom + dotSpacing

        val startRow = (topBound / rowHeight).toInt() - 1
        val endRow = (bottomBound / rowHeight).toInt() + 1
        val startCol = (leftBound / dotSpacing).toInt() - 1
        val endCol = (rightBound / dotSpacing).toInt() + 1

        var idx = 0
        val native = drawContext.canvas.nativeCanvas

        for (rowIndex in startRow..endRow) {
            val y = rowIndex * rowHeight
            val xOffset = if (rowIndex % 2 != 0) dotSpacing / 2f else 0f
            for (colIndex in startCol..endCol) {
                if (idx + 2 > ptsCache.size) {
                    native.drawPoints(ptsCache, 0, idx, dotPaint)
                    idx = 0
                }
                ptsCache[idx++] = colIndex * dotSpacing + xOffset
                ptsCache[idx++] = y
            }
        }

        if (idx > 0) {
            native.drawPoints(ptsCache, 0, idx, dotPaint)
        }
    }
}

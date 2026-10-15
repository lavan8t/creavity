package com.kd.creavity.presentation.canvas

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.luminance
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

        val dotColor = if (backgroundColor.luminance() < 0.5f) Color(0x35FFFFFF) else Color(0x28000000)
        val dotRadius = (1.2f / zoom).coerceIn(0.6f, 2.5f)

        val startX = -panOffset.x / zoom
        val startY = -panOffset.y / zoom
        val endX = startX + size.width / zoom
        val endY = startY + size.height / zoom

        val gridStartX = floor(startX / spacing) * spacing
        val gridStartY = floor(startY / spacing) * spacing

        var y = gridStartY
        while (y <= endY) {
            var x = gridStartX
            while (x <= endX) {
                drawCircle(color = dotColor, radius = dotRadius, center = Offset(x, y))
                x += spacing
            }
            y += spacing
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

        val lineColor = if (backgroundColor.luminance() < 0.5f) Color(0x18FFFFFF) else Color(0x12000000)
        val strokeW = (LINE_WIDTH / zoom).coerceAtLeast(0.8f)

        val startX = -panOffset.x / zoom
        val startY = -panOffset.y / zoom
        val endX = startX + size.width / zoom
        val endY = startY + size.height / zoom

        val gridStartY = floor(startY / spacing) * spacing

        var y = gridStartY
        while (y <= endY) {
            drawLine(color = lineColor, start = Offset(startX, y), end = Offset(endX, y), strokeWidth = strokeW)
            y += spacing
        }
    }

    private fun DrawScope.drawIsometricDots(
        backgroundColor: Color,
        zoom: Float,
        panOffset: Offset
    ) {
        var spacing = DOT_SPACING
        while (spacing * zoom < 18f) {
            spacing *= 2f
        }

        val dotColor = if (backgroundColor.luminance() < 0.5f) Color(0x35FFFFFF) else Color(0x28000000)
        val dotRadius = (1.2f / zoom).coerceIn(0.6f, 2.5f)

        val startX = -panOffset.x / zoom
        val startY = -panOffset.y / zoom
        val endX = startX + size.width / zoom
        val endY = startY + size.height / zoom

        val rowHeight = spacing * 0.8660254f // sqrt(3)/2
        val gridStartY = floor(startY / rowHeight) * rowHeight
        val gridStartX = floor(startX / spacing) * spacing

        var y = gridStartY
        var rowIndex = 0
        while (y <= endY) {
            val offsetX = if (rowIndex % 2 != 0) spacing * 0.5f else 0f
            var x = gridStartX + offsetX
            while (x <= endX) {
                drawCircle(color = dotColor, radius = dotRadius, center = Offset(x, y))
                x += spacing
            }
            y += rowHeight
            rowIndex++
        }
    }
}
